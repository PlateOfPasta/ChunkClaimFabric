/*
   ChunkClaim Plugin for Minecraft Fabric Servers
   Copyright (C) 2020 PlateOfPasta

   This file is part of ChunkClaim and derivative work ChunkClaimFabric.

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

package com.github.plateofpasta.chunkclaimfabric.player;

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.permission.Permissible;
import com.github.plateofpasta.edgestitch.player.EdgestitchPlayer;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/** Wrapper class for abstracting how this mod interacts with players. */
public class ChunkClaimPlayer extends EdgestitchPlayer {
  public ChunkClaimPlayer(PlayerEntity player) {
    super(player);
  }

  /**
   * Wrapper for looking up player data in the datastore and checking the player's ignore
   * permission.
   *
   * @param playerName Player name to check.
   * @return {@code true} if the player can ignore, else false.
   */
  public static boolean hasIgnorePermission(String playerName) {
    return ChunkClaimFabric.getPlugin()
        .getDataStore()
        .getPlayerData(playerName)
        .canIgnoreChunkClaims();
  }

  /**
   * Checks if this player has an Edgestitch permission.
   *
   * @param permission Fully qualified name of the Edgestitch permission.
   * @return {@code true} if the player has been given the permission, else {@code false}.
   */
  public boolean hasPermission(String permission) {
    Permissible p = (Permissible) ((ServerPlayerEntity) this.getPlayer());
    return p.hasPermission(permission);
  }

  /**
   * Checks if this player has claim permission.
   *
   * @return {@code true} if the player has been given permission to claim chunks, else {@code
   *     false}.
   */
  public boolean hasClaimChunkPermission() {
    return this.hasPermission("chunkclaimfabric.claim");
  }

  /**
   * Checks if this player has moderator permission.
   *
   * @return {@code true} if the player has been given permission to be a chunk claim moderator,
   *     else {@code false}.
   */
  public boolean hasModPermission() {
    return this.hasPermission("chunkclaimfabric.mod");
  }

  /**
   * Helper function for checking if a player can modify the block at the location per claim rules.
   * Uses the singleton instance of the plugin.
   *
   * @param player Player modifying block.
   * @param location Location being modified.
   * @return true if modifiable by the player, else false.
   */
  public boolean canPlayerModifyAtLocation(EdgestitchLocation location) {
    final PlayerData playerData =
        ChunkClaimFabric.getPlugin().getDataStore().getPlayerData(this.getName());
    final Chunk chunk =
        ChunkClaimFabric.getPlugin().getDataStore().getChunkAt(location, playerData.getLastChunk());
    return (null != chunk)
        && ((playerData.canIgnoreChunkClaims()) || (chunk.canModify(this.getName())));
  }
}
