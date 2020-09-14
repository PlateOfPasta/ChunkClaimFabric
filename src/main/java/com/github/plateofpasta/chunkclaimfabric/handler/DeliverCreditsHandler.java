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
import com.github.plateofpasta.chunkclaimfabric.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.function.Consumer;

/**
 * Delivers a fractional amount of credits to online players. The fractional amount is determined by
 * the configured credits per hour and the frequency per hour of this handler.
 */
public class DeliverCreditsHandler implements Consumer<MinecraftServer> {
  private final DataStore dataStore;
  private final double CREDITS_PER_HOUR;
  private final int RUN_FREQUENCY_PER_HOUR;

  /**
   * Requires a datastore reference.
   *
   * @param dataStore Datastore for updating player data.
   * @param creditsPerHour Credits per hour to deliver.
   * @param runFrequency Frequency PER HOUR of this handler.
   */
  public DeliverCreditsHandler(DataStore dataStore, double creditsPerHour, int runFrequency) {
    this.dataStore = dataStore;
    CREDITS_PER_HOUR = creditsPerHour;
    this.RUN_FREQUENCY_PER_HOUR = runFrequency;
  }

  /**
   * Helper to check if the player is AFK.
   *
   * @param player Player.
   * @param lastLocation Their last AFK check location.
   * @return {@code true} if AFK, else {@code false}.
   */
  private static boolean isAFK(ChunkClaimPlayer player, EdgestitchLocation lastLocation) {
    // Vehicle check prevents naive attempts to get around distance check.
    // todo Players may be able to devise a more complex method because we do not check for world
    // sameness in the distance check.
    return !(!player.isInsideVehicle()
        && (lastLocation == null
            || lastLocation.distanceSquaredUnsafe(player.getLocation()) >= 25));
  }

  /**
   * Performs this operation on the given argument. Implements the {@link
   * com.github.plateofpasta.chunkclaimfabric.Scheduler#repeating(Consumer, int, int)} Consumer.
   *
   * @param minecraftServer Minecraft server we're running on.
   */
  @Override
  public void accept(MinecraftServer minecraftServer) {
    // Get list of online players.
    List<ServerPlayerEntity> players = minecraftServer.getPlayerManager().getPlayerList();

    for (ServerPlayerEntity player : players) {
      ChunkClaimPlayer EdgestitchPlayer = new ChunkClaimPlayer(player);
      PlayerData playerData = this.dataStore.getPlayerData(EdgestitchPlayer.getName());

      if (isAFK(EdgestitchPlayer, playerData.getLastAfkCheckLocation())) {
        continue;
      }

      // Remember current location for next time.
      playerData.setLastAfkCheckLocation(EdgestitchPlayer.getLocation());

      // If player is over accrued limit, accrued limit was probably reduced in config file
      // AFTER they accrued. In that case, leave his credits where they are.
      if (0
          <= Double.compare(
              playerData.getCredits(), ChunkClaimFabric.getClaimConfig().getMaxCredits())) {
        continue;
      }

      playerData.addCredits(CREDITS_PER_HOUR / RUN_FREQUENCY_PER_HOUR);

      // Intentionally NOT saving data here to reduce overall secondary storage access frequency
      // many other operations will cause this players data to save, including their eventual
      // logout.
    }
  }
}
