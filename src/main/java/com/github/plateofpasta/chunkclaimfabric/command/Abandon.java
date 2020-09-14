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

import com.github.plateofpasta.chunkclaimfabric.Chunk;
import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimPrompt;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.visual.Visualization;
import com.github.plateofpasta.chunkclaimfabric.visual.VisualizationType;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

/** Abandon command. */
public class Abandon implements Command<ServerCommandSource> {

  public static final String NAMESPACE = "abandon";
  public static final String ARG0_NAMESPACE = "radius";
  private static final String NAMESPACE_PAST_TENSE = "abandoned";

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
    Chunk chunk = dataStore.getChunkAt(location, null);
    PlayerData playerData = dataStore.getPlayerData(player.getName());

    int radius;
    try {
      radius = context.getArgument(ARG0_NAMESPACE, Integer.class);
    } catch (IllegalArgumentException e) {
      radius = 0;
    }

    if (radius > 0) {
      int chunksDeleted =
          dataStore.deleteChunks(
              ChunkClaimFabric.getPlugin().getChunksInRadius(location, player.getName(), radius));
      player.sendMessage(
          ChunkClaimPrompt.joinText(
              " ",
              ChunkClaimPrompt.radiusOperation(Abandon.NAMESPACE_PAST_TENSE, radius, chunksDeleted),
              ChunkClaimPrompt.creditsAmount(playerData.getCredits())));
    } else {
      // Abandon the chunk we're currently in.
      if (chunk == null) {
        Visualization.applyWithMessage(
            player,
            Visualization.fromChunk(
                new Chunk(location), location.getY(), VisualizationType.PUBLIC, location),
            ChunkClaimPrompt.get("prompt.chunkclaim.chunk_is_public"));

      } else if (chunk.getOwnerName().equals(player.getName())) {
        dataStore.deleteChunk(chunk);
        Visualization.applyWithMessage(
            player,
            Visualization.fromChunk(chunk, location.getY(), VisualizationType.PUBLIC, location),
            ChunkClaimPrompt.joinText(
                " ",
                ChunkClaimPrompt.get("prompt.chunkclaim.chunk_abandoned"),
                ChunkClaimPrompt.creditsAmount(playerData.getCredits())));

      } else {
        if (playerData.getLastChunk() != chunk) {
          playerData.setLastChunk(chunk);
          Visualization.apply(
              player,
              Visualization.fromChunk(
                  chunk, location.getY(), VisualizationType.ERROR_CHUNK, location));
        }
        player.sendMessage(
            ChunkClaimPrompt.joinText(
                " ",
                ChunkClaimPrompt.get("prompt.chunkclaim.you_dont_own_chunk"),
                ChunkClaimPrompt.onlyOtherCanDelete(chunk.getOwnerName())));
      }
    }
    return 0;
  }
}
