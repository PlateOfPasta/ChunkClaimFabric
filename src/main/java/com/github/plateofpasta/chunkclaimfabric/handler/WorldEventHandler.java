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

package com.github.plateofpasta.chunkclaimfabric.handler;

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.edgestitch.world.EdgestitchWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

/** Handler for world-related callbacks. */
public class WorldEventHandler {

  private final DataStore dataStore;

  /**
   * The handler requires a reference to the datastore.
   *
   * @param dataStore Chunk claim datastore of the plugin's server.
   */
  private WorldEventHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  /**
   * Bootstrap static method for initializing the callbacks for world events.
   *
   * @param dataStore Chunk claim datastore of the plugin's server.
   */
  public static void initHandlers(DataStore dataStore) {
    WorldEventHandler handler = new WorldEventHandler(dataStore);
    ServerWorldEvents.LOAD.register(handler::onWorldLoad);
    ServerWorldEvents.UNLOAD.register(handler::onWorldClose);
  }

  /**
   * When a world gets loaded.
   *
   * @param server Server loading the world.
   * @param world World being loaded.
   */
  private void onWorldLoad(MinecraftServer server, ServerWorld world) {
    String worldName = EdgestitchWorld.Companion.getName(world);
    if (ChunkClaimFabric.isConfiguredWorld(worldName)) {
      try {
        this.dataStore.loadWorldData(worldName);
        int claimedChunks = this.dataStore.getWorlds().get(worldName).chunkTable.size();
        ChunkClaimFabric.logInfo(
            "Loaded " + claimedChunks + " claimed chunks for world \"" + worldName + "\".");
        System.gc();
      } catch (Exception e) {
        ChunkClaimFabric.logInfo(
            "Unable to load data for world \"" + worldName + "\": " + e.getMessage());
      }
    }
  }

  /**
   * When a world gets unloaded.
   *
   * @param server Server loading the world.
   * @param world World being loaded.
   */
  private void onWorldClose(MinecraftServer server, ServerWorld world) {
    String worldName = EdgestitchWorld.Companion.getName(world);
    if (ChunkClaimFabric.isConfiguredWorld(worldName)) {
      this.dataStore.unloadWorldData(worldName);
      System.gc();
    }
  }
}
