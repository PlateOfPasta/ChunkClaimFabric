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

package com.github.plateofpasta.chunkclaimfabric;

import com.github.plateofpasta.chunkclaimfabric.command.ChunkCommands;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimConfig;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.datastore.FlatFileDataStore;
import com.github.plateofpasta.chunkclaimfabric.handler.*;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import com.github.plateofpasta.edgestitch.world.EdgestitchWorld;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// todo Refactor as ChunkClaimFabric.
public class ChunkClaimFabric implements DedicatedServerModInitializer {
  public static final Logger logger = Logger.getLogger("Minecraft");
  public static final String MOD_ID = "chunkclaimfabric";
  protected static final ChunkClaimConfig CONFIG =
      AutoConfig.register(ChunkClaimConfig.class, JanksonConfigSerializer::new).getConfig();
  protected static ChunkClaimFabric plugin;
  protected DataStore dataStore;
  protected Server server;
  protected Scheduler scheduler;
  private boolean initializationFailed = false;

  /**
   * Static getter for the singleton instance of the plugin.
   *
   * @return Singleton instance of the plugin.
   */
  public static ChunkClaimFabric getPlugin() {
    return plugin;
  }

  /**
   * Logs the string to the plugin's logger at "info" severity level.
   *
   * @param entry String to log.
   */
  public static void logInfo(String entry) {
    logger.info(String.format("%s: %s", ChunkClaimFabric.MOD_ID, entry));
  }

  /**
   * Helper function for checking if a player can modify the block at the location per claim rules.
   * Uses the singleton instance of the plugin.
   *
   * @param player Player modifying block.
   * @param location Location being modified.
   * @return true if modifiable by the player, else false.
   */
  public static boolean canPlayerModifyAtLocation(
  ChunkClaimPlayer player, EdgestitchLocation location) {
    final PlayerData playerData =
        ChunkClaimFabric.getPlugin().getDataStore().getPlayerData(player.getName());
    final Chunk chunk =
        ChunkClaimFabric.getPlugin().getDataStore().getChunkAt(location, playerData.getLastChunk());
    return (null != chunk)
        && ((playerData.canIgnoreChunkClaims()) || (chunk.canModify(player.getName())));
  }

  /**
   * Getter for the plugin configuration.
   *
   * @return Configuration object.
   */
  public static ChunkClaimConfig getClaimConfig() {
    return CONFIG;
  }

  /**
   * Checks if the given world is covered under the protection of ChunkClaim.
   *
   * @param worldName World to check.
   * @return true if the world is covered under ChunkClaim, else false.
   */
  public static boolean isConfiguredWorld(String worldName) {
    return getClaimConfig().getWorlds().contains(worldName);
  }

  /** Override for FabricMC initialization. */
  @Override
  public void onInitializeServer() {
    ChunkClaimFabric.plugin = this;
    // Initialize server events.
    ServerLifecycleEvents.SERVER_STARTED.register(this::initializeOnServerStart);
    ServerLifecycleEvents.SERVER_STOPPED.register(this::shutdownOnServerStop);
    // Initialize commands.
    ChunkCommands.init();
  }

  /**
   * Handler for fully initializing the mod when the server object has been instantiated.
   *
   * @param server Server tied to this mod.
   */
  public void initializeOnServerStart(MinecraftServer server) {
    this.server = new Server(server);
    logInfo(
        String.format(
            "List of worlds found in this server: %s",
            String.join(", ", this.server.getAvailableWorldNames())));
    try {
      this.dataStore = new FlatFileDataStore();
    } catch (Exception e) {
      logger.log(
          Level.SEVERE,
          String.format(
              "Fatal error when initializing the data store for mod %s. Message: %s",
              ChunkClaimFabric.MOD_ID, e.getMessage()));
      this.initializationFailed = true;
    }
    // Ensure the server shuts down if initialization failed.
    if (this.initializationFailed) {
      server.close();
      logger.log(
          Level.SEVERE,
          String.format(
              "Mod %s flagged for server shutdown due to unrecoverable error. Shutting down...",
              ChunkClaimFabric.MOD_ID));
    }

    // Perform remaining init if previous initialization hasn't failed.
    if (!this.initializationFailed) {
      // Make scheduler.
      this.scheduler = new Scheduler();

      // Initialize handlers.
      // Deliver credits every 5 minutes, which is a frequency of 12 runs per hour.
      this.scheduler.repeating(
          new DeliverCreditsHandler(
              this.getDataStore(), ChunkClaimFabric.getClaimConfig().getCreditsPerHour(), 12),
          0,
          6000);
      // Run cleanup on 50 random chunks every hour.
      this.scheduler.repeating(
          minecraftServer -> {
            this.getDataStore().cleanUp(50);
          },
          72000,
          72000);
      // Initialize remaining handlers.
      BlockEventHandler.initHandlers(this.getDataStore());
      EntityEventHandler.initHandlers(this.getDataStore());
      PlayerEventHandler.initHandlers(this.getDataStore());
      WorldEventHandler.initHandlers(this.getDataStore());
    }
  }

  /**
   * Handler for shutting down the mod when the server stops.
   *
   * @param server Server tied to this mod that is stopping.
   */
  public void shutdownOnServerStop(MinecraftServer server) {
    // Guard shutdown in case initialization failed.
    if (null != this.dataStore) {
      // Save all online player data to the datastore.
      for (String playerName : this.server.getOnlinePlayerNames()) {
        PlayerData playerData = this.getDataStore().getPlayerData(playerName);
        this.getDataStore().savePlayerData(playerName, playerData);
      }
      // Close datastore.
      dataStore.close();
    }
  }

  /** @return Server object associated with this mod. */
  public Server getServer() {
    return this.server;
  }

  /** @return Scheduler associate with this mod. */
  public Scheduler getScheduler() {
    return this.scheduler;
  }

  /**
   * Writes a chunk to the plugin's datastore.
   *
   * @param chunk Chunk to write to the datastore.
   */
  public void writeChunkToStorage(Chunk chunk) {
    // todo maybe make writeChunkToStorage private and use AddChunk?
    getDataStore().writeChunkToStorage(chunk);
  }

  /**
   * Getter for the data store.
   *
   * @return Data store member.
   */
  public DataStore getDataStore() {
    return dataStore;
  }

  /**
   * Get all the chunks in within the radius (centered at the given chunk) that are owned by the
   * player.
   *
   * @param location Location in the center chunk.
   * @param targetPlayerName Player whose chunk's we're trying to find.
   * @param radius Search radius. Value of zero will return only the center chunk if valid.
   * @return List of chunks owned by the player.
   */
  public ArrayList<Chunk> getChunksInRadius(
      EdgestitchLocation location, String targetPlayerName, int radius) {
    Chunk chunk = new Chunk(location);
    ArrayList<Chunk> chunksInRadius = new ArrayList<Chunk>();

    for (int x = chunk.getCoordX() - radius; x <= chunk.getCoordX() + radius; x++) {
      for (int z = chunk.getCoordZ() - radius; z <= chunk.getCoordZ() + radius; z++) {

        Chunk foundChunk = this.getDataStore().getChunkAtPos(x, z, chunk.getWorldName());

        if (foundChunk != null && foundChunk.getOwnerName().equals(targetPlayerName)) {
          chunksInRadius.add(foundChunk);
        }
      }
    }
    return chunksInRadius;
  }

  /**
   * Regenerates the given chunk. Handles finding the correct world instance to perform
   * regeneration.
   *
   * @param chunk Chunk to regenerate.
   */
  public void regenerateChunk(Chunk chunk) {
    if (getClaimConfig().getRegenerateChunk()) {
      EdgestitchWorld world =
          ChunkClaimFabric.getPlugin().getServer().getWorld(chunk.getWorldName());
      world.regenerateChunk(chunk.getChunkPos());
    }
  }

  /**
   * Wrapper for looking up player data in the datastore and checking the player's ignore
   * permission.
   *
   * @param player Player name to check.
   * @return {@code true} if the player can ignore, else false.
   */
  public boolean hasIgnorePermission(String player) {
    return this.getDataStore().getPlayerData(player).canIgnoreChunkClaims();
  }
}
