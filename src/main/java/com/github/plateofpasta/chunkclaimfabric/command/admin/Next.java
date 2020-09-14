/*
   ChunkClaim Plugin for Minecraft Bukkit Servers
   Copyright (C) 2012 Felix Schmidt
   Copyright (C) 2020 PlateOfPasta: Notice of modification for ChunkClaimFabric

   This file is part of ChunkClaim.

   ChunkClaim is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   ChunkClaim is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with ChunkClaim.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.github.plateofpasta.chunkclaimfabric.command.admin;

import com.github.plateofpasta.chunkclaimfabric.*;
import com.github.plateofpasta.chunkclaimfabric.command.ChunkCommands;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimPrompt;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.server.Scheduler;
import com.github.plateofpasta.chunkclaimfabric.util.TimeDateUtil;
import com.github.plateofpasta.chunkclaimfabric.visual.Visualization;
import com.github.plateofpasta.chunkclaimfabric.visual.VisualizationType;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.*;

/** Next admin command teleports to the target player's next chunk. */
public class Next implements Command<ServerCommandSource> {
  public static final String NAMESPACE = "next";
  public static final String ARG0_NAMESPACE = "player";

  /**
   * Command execution logic.
   *
   * @param context Context for the command.
   * @return {@code 0} if success, else {@code -1}.
   * @throws CommandSyntaxException Throws if an error occurred parsing the context.
   */
  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    final DataStore dataStore = ChunkClaimFabric.getPlugin().getDataStore();
    final ChunkClaimPlayer player = new ChunkClaimPlayer(context.getSource().getPlayer());
    final EdgestitchLocation location = player.getLocation();

    if (!ChunkClaimFabric.isConfiguredWorld(location.getWorld().getName())) {
      player.sendMessages(ChunkClaimPrompt.get("prompt.chunkclaim.not_configured_world"));
      return 0;
    }

    // Parse args
    final ChunkClaimPlayer targetPlayer = ChunkCommands.parsePlayerArg(context, ARG0_NAMESPACE);
    final PlayerData targetPlayerData = dataStore.getPlayerData(targetPlayer.getName());
    if (null == targetPlayerData) {
      player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.player_not_found"));
      return 0;
    }

    Chunk chunk = NextCache.getInstance().getNextChunk(targetPlayer.getName());
    if (chunk == null) {
      player.sendMessages(ChunkClaimPrompt.get("prompt.chunkclaim.no_chunks_found"));
      return 0;
    }

    int x = (chunk.getStartX()) + 8;
    int z = (chunk.getStartZ()) + 8;
    int y = player.getWorld().getHighestY(x, z) + 15;

    player.teleport(new EdgestitchLocation(player.getWorld(), x, y, z));

    MutableText adminString =
        new LiteralText(
            String.format(
                "ID: %s, %s, %s",
                chunk.getChunkCoordString(),
                chunk.getOwnerName(),
                ChunkClaimPrompt.daysSinceLastLogin(
                        dataStore.getPlayerData(chunk.getOwnerName()).getLastLoginInDays())
                    .asString()));
    Visualization.applyWithMessage(
        player,
        Visualization.fromChunk(chunk, location.getY(), VisualizationType.CHUNK, location),
        adminString);

    return 0;
  }

  /** Singleton class for caching Next command queries. */
  private static class NextCache {
    /**
     * Singleton instance. References plugin datastore because why not have singletons depend on
     * other singletons.
     */
    private static final NextCache nextCache =
        new NextCache(ChunkClaimFabric.getPlugin().getDataStore());
    /** Number of entries in the cache. */
    private static final int MAX_CACHE_SIZE = 20;
    /** Age in minutes that a cache entry must be to get auto-removed from the cache. */
    private static final long CLEANUP_TIME_MINUTES = 30;
    /**
     * Number of ticks between calls to {@link NextCache#cleanup(MinecraftServer)} by the {@link
     * Scheduler}. 54000 ticks == 45 minutes.
     */
    private static final int CLEANUP_TICKS = 54000;

    private final DataStore dataStore;
    /** For storing the lists from the datastore. */
    private final Map<String, List<Chunk>> playerChunkListCache = new TreeMap<>();
    /** For storing queries to a player's chunk list. */
    private final Map<String, NextCacheQuery> playerChunkListIteratorCache = new TreeMap<>();

    /**
     * Constructor requires datastore. Registers a {@link Scheduler} callback for automatic cleanup.
     *
     * @param dataStore Datastore for looking up player chunks.
     */
    private NextCache(DataStore dataStore) {
      this.dataStore = dataStore;
      ChunkClaimFabric.getPlugin()
          .getScheduler()
          .repeating(this::cleanup, NextCache.CLEANUP_TICKS, NextCache.CLEANUP_TICKS);
    }

    /** @return Singleton instance. */
    public static NextCache getInstance() {
      return nextCache;
    }

    /**
     * Gets the next chunk for the player. Assumes the player exists in the datastore and has chunks
     * to query. Loops back to the beginning of the list when you've reached the last chunk.
     *
     * @param playerName Target player of the next command.
     * @return Next {@link Chunk} or {@code null} if the player does not have any chunks.
     */
    public Chunk getNextChunk(String playerName) {
      NextCacheQuery cacheQuery = playerChunkListIteratorCache.get(playerName);
      if (null == cacheQuery) {
        // Doesn't exist in cache. Do house keeping and add it.
        if (MAX_CACHE_SIZE < playerChunkListIteratorCache.size()) {
          removeOldest();
        }
        cacheQuery = add(playerName);
        if (null == cacheQuery) {
          return null;
        }
      }
      // Return next chunk if there is one. Else loop back to the beginning of the list.
      if (cacheQuery.iter.hasNext()) {
        return cacheQuery.iter.next();
      } else {
        // Replace the current query with a fresh iterator.
        playerChunkListIteratorCache.put(
            playerName,
            new NextCacheQuery(playerChunkListCache.get(playerName).iterator(), new Date()));
        // Recursive call should be well defined if we've gotten to this point.
        return this.getNextChunk(playerName);
      }
    }

    /**
     * Adds a player to the next cache.
     *
     * @param playerName Player to add.
     * @return Query into the cache, else {@code null} if the player does not have any claimed
     *     chunks.
     */
    private NextCacheQuery add(String playerName) {
      List<Chunk> chunkList = dataStore.getAllChunksForPlayer(playerName);
      if (chunkList.isEmpty()) {
        return null;
      }
      NextCacheQuery addedQuery = new NextCacheQuery(chunkList.iterator(), new Date());
      playerChunkListCache.put(playerName, chunkList);
      playerChunkListIteratorCache.put(playerName, addedQuery);
      return addedQuery;
    }

    /**
     * Purges an entry from the cache based on the primary key (player name).
     *
     * @param playerName Player name used to purge an entry from the cache.
     */
    private void purgeEntry(String playerName) {
      playerChunkListCache.remove(playerName);
      playerChunkListIteratorCache.remove(playerName);
    }

    /** Removes all entries from the cache. */
    private void removeAll() {
      playerChunkListCache.clear();
      playerChunkListIteratorCache.clear();
    }

    /** Removes the oldest cache query. */
    private void removeOldest() {
      Iterator<Map.Entry<String, NextCacheQuery>> iter =
          playerChunkListIteratorCache.entrySet().iterator();
      if (!iter.hasNext()) {
        // Cache is empty.
        return;
      }
      Map.Entry<String, NextCacheQuery> oldest = iter.next();
      Map.Entry<String, NextCacheQuery> next;
      while (iter.hasNext()) {
        next = iter.next();
        if (next.getValue().age.getTime() > oldest.getValue().age.getTime()) {
          oldest = next;
        }
      }
      purgeEntry(oldest.getKey());
    }

    /**
     * Implements {@link Scheduler} Consumer type. Removes cache queries over time to free up server
     * memory.
     *
     * @param server Server provided by {@link Scheduler} callback.
     */
    private void cleanup(MinecraftServer server) {
      Date now = new Date();
      if (playerChunkListIteratorCache.isEmpty()) {
        // Just in case...
        if (playerChunkListCache.isEmpty()) {
          return;
        } else {
          playerChunkListCache.clear();
        }
        return;
      }
      playerChunkListIteratorCache
          .entrySet()
          .removeIf(
              next ->
                  CLEANUP_TIME_MINUTES
                      <= TimeDateUtil.differenceInMinutes(now, next.getValue().age));
    }

    /** Class for representing a "query" into the NextCache. Effectively a pair. */
    private class NextCacheQuery {
      public Iterator<Chunk> iter;
      public Date age;

      /**
       * Constructor.
       *
       * @param iter Iterator.
       * @param age Age.
       */
      NextCacheQuery(Iterator<Chunk> iter, Date age) {
        this.iter = iter;
        this.age = age;
      }
    }
  }
}
