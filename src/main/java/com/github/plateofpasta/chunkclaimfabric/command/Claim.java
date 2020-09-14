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

package com.github.plateofpasta.chunkclaimfabric.command;

import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimPrompt;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.visual.Visualization;
import com.github.plateofpasta.chunkclaimfabric.visual.VisualizationType;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

/** Claim command. */
public class Claim implements Command<ServerCommandSource> {
  public static String NAMESPACE = "claim";

  /**
   * Command execution logic.
   *
   * @param context Context for the command.
   * @return {@code 0} if success, else {@code -1}.
   * @throws CommandSyntaxException Throws if an error occurred parsing the context.
   */
  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    DataStore dataStore = ChunkClaimFabric.getPlugin().getDataStore();
    ChunkClaimPlayer player = new ChunkClaimPlayer(context.getSource().getPlayer());
    EdgestitchLocation location = player.getLocation();

    if (!ChunkClaimFabric.isConfiguredWorld(location.getWorld().getName())) {
      return 0;
    }

    Chunk chunk = dataStore.getChunkAt(location, null);
    PlayerData playerData = dataStore.getPlayerData(player.getName());
    String playerName = player.getName();

    if (null == chunk) {
      // Prevent players without claim permissions.
      if (!player.hasClaimChunkPermission()) {
        player.sendMessage("You don't have permissions for claiming chunks.");
        return 0;
      }

      if (playerData.canAffordClaim()) {
        // Check if we force players to build near their existing claims.
        if (ChunkClaimFabric.getClaimConfig().getNextToForce() && !player.hasModPermission()) {
          List<Chunk> playerChunks = dataStore.getAllChunksForPlayer(playerName);

          if (playerChunks.size() > 0) {
            if (!dataStore.ownsNear(location, playerName)) {
              player.sendMessage("You can only claim a new chunk next to your existing chunks.");
              return 0;
            }
          }
        }

        // Else allow them to claim.
        Chunk newChunk = new Chunk(location, playerName, playerData.getBuilderNames());
        dataStore.claimChunk(playerName, newChunk);
        playerData.setLastChunk(newChunk);

        Visualization.applyWithMessage(
            player,
            Visualization.fromChunk(newChunk, location.getY(), VisualizationType.CHUNK, location),
            ChunkClaimPrompt.justClaimedChunk(playerData.getCredits()));
      } else {
        player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.not_enough_credits_claim"));
        playerData.setLastChunk(null);
        Visualization.apply(
            player,
            Visualization.fromChunk(
                new Chunk(location), location.getY(), VisualizationType.PUBLIC, location));
      }
    } else {
      player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.chunk_is_not_public"));
    }
    return 0;
  }
}
