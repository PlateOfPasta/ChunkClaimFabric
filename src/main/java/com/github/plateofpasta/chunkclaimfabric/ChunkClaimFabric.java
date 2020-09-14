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
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.server.Scheduler;
import com.github.plateofpasta.chunkclaimfabric.server.Server;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import java.util.logging.Level;
import java.util.logging.Logger;

// todo Refactor as ChunkClaimFabric.
public class ChunkClaimFabric implements DedicatedServerModInitializer {
  public static final Logger logger = Logger.getLogger("Minecraft");
  public static final String MOD_ID = "chunkclaimfabric";
  protected static final ChunkClaimConfig CONFIG =
      AutoConfig.register(ChunkClaimConfig.class, JanksonConfigSerializer::new).getConfig();
  protected static final Scheduler scheduler = new Scheduler();
  protected static ChunkClaimFabric plugin;
  protected DataStore dataStore;
  protected Server server;
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
   * Getter for the plugin configuration.
   *
   * @return Configuration object.
   */
  public static ChunkClaimConfig getClaimConfig() {
    return CONFIG;
  }

  /** @return Scheduler associate with this mod. */
  public static Scheduler getScheduler() {
    return scheduler;
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
      logger.log(
          Level.SEVERE,
          String.format(
              "Mod %s flagged for server shutdown due to unrecoverable error. Shutting down...",
              ChunkClaimFabric.MOD_ID));
      server.close();
    }

    // Perform remaining init if previous initialization hasn't failed.
    if (!this.initializationFailed) {
      // Initialize handlers.
      // Deliver credits every 5 minutes, which is a frequency of 12 runs per hour.
      getScheduler()
          .repeating(
              new DeliverCreditsHandler(
                  this.getDataStore(), ChunkClaimFabric.getClaimConfig().getCreditsPerHour(), 12),
              0,
              6000);
      // Run cleanup on 50 random chunks every hour.
      getScheduler()
          .repeating(
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
      this.dataStore.close();
    }
  }

  /** @return Server object associated with this mod. */
  public Server getServer() {
    return this.server;
  }

  /**
   * Getter for the data store.
   *
   * @return Data store member.
   */
  public DataStore getDataStore() {
    return this.dataStore;
  }
}
