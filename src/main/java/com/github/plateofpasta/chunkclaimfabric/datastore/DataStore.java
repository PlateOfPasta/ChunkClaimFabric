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

package com.github.plateofpasta.chunkclaimfabric.datastore;

import com.github.plateofpasta.chunkclaimfabric.Chunk;
import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.ChunkWorld;
import com.github.plateofpasta.chunkclaimfabric.PlayerData;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/** Abstract class for ChunkClaim datastores. */
public abstract class DataStore {

  protected static final String DATA_LAYER_FOLDER_PATH =
      "plugins" + File.separator + "ChunkClaimFabric";
  static final int MIN_MODIFIED_BLOCKS = ChunkClaimFabric.getClaimConfig().getMinModBlocks();
  static final double CHUNK_PRICE = ChunkClaimFabric.getClaimConfig().getChunkPrice();
  protected HashMap<String, PlayerData> playerNameToPlayerDataMap =
      new HashMap<String, PlayerData>();
  ArrayList<Chunk> chunks = new ArrayList<Chunk>();
  ArrayList<Chunk> unusedChunks = new ArrayList<Chunk>();
  HashMap<String, ChunkWorld> worlds = new HashMap<String, ChunkWorld>();

  /**
   * Initializes the datastore.
   *
   * @throws Exception
   */
  void initialize() throws Exception {
    ChunkClaimFabric.logInfo(this.chunks.size() + " total claimed chunks loaded.");

    TreeSet<String> playerNames = new TreeSet<String>();
    for (Chunk chunk : this.chunks) {
      playerNames.add(chunk.getOwnerName());
    }
    ChunkClaimFabric.logInfo(playerNames.size() + " players have claimed chunks in loaded worlds.");

    System.gc();
  }

  /**
   * Gets the primary key as a string for chunk data.
   *
   * @param chunk Chunk to get the primary key of.
   * @return String representing the chunk's primary key in the datastore.
   */
  public abstract String getChunkPrimaryKey(Chunk chunk);

  /**
   * Gets the primary key as a string for player data.
   *
   * @param playerName Player name to get the key of.
   * @return String representing the player data's primary key in the datastore.
   */
  public abstract String getPlayerDataPrimaryKey(String playerName);

  /**
   * Interface for loading all chunk data for a given world.
   *
   * @param worldName World name to load the data of.
   * @throws Exception
   */
  public abstract void loadWorldData(String worldName);

  /**
   * Interface for implementing the chunk data store operation.
   *
   * @param chunk Chunk to write to storage.
   */
  public abstract void writeChunkToStorage(Chunk chunk);

  /**
   * Interface for deleting a single chunk from the datastore.
   *
   * @param chunk Chunk to delete.
   * @return {@code true} a chunk was deleted, else false.
   */
  abstract boolean deleteChunkFromSecondaryStorage(Chunk chunk);

  /**
   * Interface for implementing the player data load operation.
   *
   * @param playerName Name of the player.
   * @return Player data.
   */
  abstract PlayerData getPlayerDataFromStorage(String playerName);

  /**
   * Interface for implementing the player data save operation.
   *
   * @param playerName Name of player whose data we're saving.
   * @param playerData Data to save.
   */
  public abstract void savePlayerData(String playerName, PlayerData playerData);

  /** Interface for shutting closing the datastore. */
  public abstract void close();

  /**
   * Remove from runtime memory all chunk data for a world.
   *
   * @param worldName Name of world to unload.
   */
  public synchronized void unloadWorldData(String worldName) {
    this.worlds.remove(worldName);
    this.chunks.removeIf(chunk -> chunk.getWorldName().equals(worldName));
  }

  /**
   * Checks N-number of claimed chunks for cleanup and tries to reclaim at most 50.
   *
   * @param n Number of chunks to randomly check for clean up.
   */
  public void cleanUp(int n) {

    if (this.chunks.isEmpty()) {
      return;
    }

    long autoDeleteMillis = (long) ChunkClaimFabric.getClaimConfig().getAutoDeleteMillis();
    long now = new Date().getTime();
    Random random = new Random();

    for (int i = 0, reclaimCount = 0;
        (i < n) && (50 > reclaimCount) && !this.chunks.isEmpty();
        i++) {
      Chunk chunk = chunks.get(random.nextInt(this.chunks.size()));
      long claimAgeMillis = now - chunk.getClaimDate().getTime();
      // Cleanup the chunk.
      if (chunk.isMarked()
          || (!ChunkClaimFabric.getClaimConfig().isAutoDeleteDisabled()
              && (!chunk.hasMetMinimum() && (claimAgeMillis > autoDeleteMillis)))) {
        // Reclaim the chunk.
        this.deleteChunk(chunk);
        this.clearCachedPlayerData(chunk.getOwnerName());
        ChunkClaimFabric.logInfo(
            String.format(
                "Auto-deleted %s's chunk at %s.",
                chunk.getOwnerName(), chunk.getChunkCoordString()));
        reclaimCount++;
      }
    }
  }

  /**
   * Removes from runtime memory all chunk data for one player.
   *
   * @param playerName Name of player whose data we're unloading.
   */
  public synchronized void clearCachedPlayerData(String playerName) {
    this.playerNameToPlayerDataMap.remove(playerName);
  }

  /**
   * Transfers ownership of a chunk to a new player. todo This doesn't check if the new owner has
   * enough credits. todo The current code contains all original logic (but in a slightly refactored
   * form). I suspect the reason this function is not used is because of the wonky-ness of the
   * logic. For example, the previous owner loses credits and the new owner gains credits.
   *
   * @param chunk Chunk being transferred.
   * @param newOwnerName Name of player that will be the new owner.
   * @throws Exception
   */
  public synchronized void changeChunkOwner(Chunk chunk, String newOwnerName) throws Exception {
    PlayerData ownerData = this.getPlayerData(chunk.getOwnerName());
    PlayerData newOwnerData = this.getPlayerData(newOwnerName);

    // Modify chunk.
    chunk.setOwnerName(newOwnerName);
    this.writeChunkToStorage(chunk);

    // Modify previous owner data
    ownerData.removeCredits(CHUNK_PRICE);
    this.savePlayerData(chunk.getOwnerName(), ownerData);

    // modify new owner data
    newOwnerData.addCredits(CHUNK_PRICE);
    this.savePlayerData(newOwnerName, newOwnerData);
  }

  /**
   * Adds the claimed chunk to the datastore. Subtracts the claim cost from the
   *
   * @param playerName Name of player claiming chunk.
   * @param chunk New chunk that has just been claimed.
   */
  public synchronized void claimChunk(String playerName, Chunk chunk) {
    this.chunks.add(chunk);

    if (this.worlds.containsKey(chunk.getWorldName())) {
      this.worlds.get(chunk.getWorldName()).addChunk(chunk);
      chunk.setInDataStore(true);
      this.writeChunkToStorage(chunk);
      // Update player data.
      PlayerData targetPlayerData = this.getPlayerData(playerName);
      targetPlayerData.removeCredits(CHUNK_PRICE);
      this.savePlayerData(playerName, targetPlayerData);
    }
  }

  /**
   * Deletes the chunk from the datastore. Refunds the credits to the owner's player data.
   *
   * @param chunk Chunk to delete.
   * @return {@code true} a chunk was deleted, else false.
   */
  public synchronized boolean deleteChunk(Chunk chunk) {
    if (this.deleteChunkFromSecondaryStorage(chunk)) {
      if (this.chunks.removeIf(c -> c.equals(chunk))) {
        this.worlds.get(chunk.getWorldName()).removeChunk(chunk);
        chunk.setInDataStore(false);
      }
      PlayerData targetPlayerData = this.getPlayerData(chunk.getOwnerName());
      targetPlayerData.addCredits(CHUNK_PRICE);
      this.savePlayerData(chunk.getOwnerName(), targetPlayerData);
      ChunkClaimFabric.getPlugin().regenerateChunk(chunk);
      return true;
    }
    return false;
  }

  /**
   * Variadic chunk delete.
   *
   * @param chunks Chunks to delete from the datastore.
   * @return Amount of chunks successfully deleted.
   */
  public int deleteChunks(List<Chunk> chunks) {
    int count = 0;
    for (Chunk chunk : chunks) {
      if (this.deleteChunk(chunk)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Gets the player's data from the datastore.
   *
   * @param playerName Name of the player.
   * @return Player data.
   */
  public synchronized PlayerData getPlayerData(String playerName) {
    PlayerData playerData = this.playerNameToPlayerDataMap.get(playerName);
    if (playerData == null) {
      playerData = this.getPlayerDataFromStorage(playerName);
      this.playerNameToPlayerDataMap.put(playerName, playerData);
    }

    return this.playerNameToPlayerDataMap.get(playerName);
  }

  /**
   * Tries to get the chunk at the location.
   *
   * @param location Game world location that we're searching for the chunk.
   * @param cachedChunk Chunk that might be at the location.
   * @return Cached chunk if it is in the datastore and contains the location, a chunk from the
   *     datastore, or null if an existing chunk cannot be found.
   */
  public synchronized Chunk getChunkAt(EdgestitchLocation location, Chunk cachedChunk) {
    if (cachedChunk != null && cachedChunk.isInDataStore() && cachedChunk.contains(location)) {
      return cachedChunk;
    }
    // Check if the location's world is loaded in the datastore.
    if (!worlds.containsKey(location.getWorld().getName())) {
      return null;
    }

    int x = location.getChunkX();
    int z = location.getChunkZ();
    return worlds.get(location.getWorld().getName()).getChunk(x, z);
  }

  /**
   * Tries to get a chunk at the given X,Z coordinates.
   *
   * @param x X-coordinate of chunk.
   * @param z Z-coordinate of chunk.
   * @param worldName Name of the world we're searching in.
   * @return Chunk at the coordinate location, or null if it cannot be found.
   */
  public synchronized Chunk getChunkAtPos(int x, int z, String worldName) {
    if (!worlds.containsKey(worldName)) {
      return null;
    }

    return worlds.get(worldName).getChunk(x, z);
  }

  /**
   * Gets a list of chunks claimed by a player.
   *
   * @param playerName Name of the player.
   * @return List of chunks of the player.
   */
  public synchronized List<Chunk> getAllChunksForPlayer(String playerName) {
    return this.chunks.stream()
        .filter(c -> c.getOwnerName().equals(playerName))
        .collect(Collectors.toList());
  }

  /**
   * Deletes all of a player's claimed chunks.
   *
   * @param playerName Name of the player.
   * @return Number of chunks deleted.
   */
  public synchronized int deleteChunksForPlayer(String playerName) {
    List<Chunk> playerChunks = getAllChunksForPlayer(playerName);
    playerChunks.forEach(this::deleteChunk);
    this.getPlayerData(playerName).addCredits(playerChunks.size() * CHUNK_PRICE);
    return playerChunks.size();
  }

  /**
   * Checks if the player owns a chunk near (cardinal direction) the current location.
   *
   * @param location Location to check near.
   * @param playerName Player we're checking the ownership permissions of.
   * @return true if the player can modify a chunk near the current location, else false.
   */
  public boolean ownsNear(EdgestitchLocation location, String playerName) {
    int x = location.getChunkX();
    int z = location.getChunkZ();
    String worldName = location.getWorld().getName();

    Chunk a = getChunkAtPos(x - 1, z, worldName);
    Chunk c = getChunkAtPos(x + 1, z, worldName);
    Chunk b = getChunkAtPos(x, z - 1, worldName);
    Chunk d = getChunkAtPos(x, z + 1, worldName);

    if (a == null && b == null && c == null && d == null) {
      return false;
    }
    if (a != null && a.canModify(playerName)) {
      return true;
    } else if (b != null && b.canModify(playerName)) {
      return true;
    } else if (c != null && c.canModify(playerName)) {
      return true;
    } else if (d != null && d.canModify(playerName)) {
      return true;
    } else {
      return false;
    }
  }

  /** @return Map worlds and their chunk data. */
  public Map<String, ChunkWorld> getWorlds() {
    return worlds;
  }
}
