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

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimPrompt;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.visual.Visualization;
import com.github.plateofpasta.chunkclaimfabric.visual.VisualizationType;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.Date;

public class BaseCommand implements Command<ServerCommandSource> {
  /**
   * Helper method for printing admin information.
   *
   * @param chunk Chunk targeted by the command.
   * @param location Location targeted by the command.
   * @param player Player performing the command.
   */
  public static void printAdminInfo(
      DataStore dataStore, Chunk chunk, EdgestitchLocation location, ChunkClaimPlayer player) {
    StringBuilder adminStringBuilder = new StringBuilder();
    String chunkID = String.format("%d|%d", location.getChunkX(), location.getChunkZ());
    MutableText message = new LiteralText(String.format("ID: %s, ", chunkID));
    if (chunk != null) {
      message.append(
          String.format(
              "Permanent: %s, ",
              (chunk.hasMetMinimum() ? "true" : ("false (" + chunk.getModifiedBlocks() + ")"))));
      long loginDays =
          ((new Date()).getTime()
                  - dataStore.getPlayerData(chunk.getOwnerName()).getLastLogin().getTime())
              / (1000 * 60 * 60 * 24);
      message.append(ChunkClaimPrompt.daysSinceLastLogin(loginDays));
    }

    // Print the builder list and apply a visualization.
    if (chunk != null && !chunk.getOwnerName().equals(player.getName())) {
      message.append("\n");

      message.append(ChunkClaimPrompt.trustedBuilderList(chunk.getBuilderNamesString(", ")));

      Visualization visualization =
          Visualization.fromChunk(
              new Chunk(location), location.getY(), VisualizationType.CHUNK, location);
      Visualization.apply(player, visualization);
    }
    player.sendMessage(message);
  }

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

    if (player.hasModPermission()) {
      printAdminInfo(dataStore, chunk, location, player);
    }

    VisualizationType visualizationType;
    MutableText message;
    if (chunk == null) {
      // Information for public chunks.
      chunk = new Chunk(location);
      message = ChunkClaimPrompt.get("prompt.chunkclaim.chunk_is_public");
      visualizationType = VisualizationType.PUBLIC;
    } else if (chunk.getOwnerName().equals(player.getName())) {
      // Information for owner of the chunk, prints trusted builders (if any).
      if (chunk.getBuilderNames().size() > 0) {
        message =
            ChunkClaimPrompt.joinText(
                " ",
                ChunkClaimPrompt.get("prompt.chunkclaim.you_own_chunk"),
                ChunkClaimPrompt.trustedBuilderList(chunk.getBuilderNamesString(", ")));
      } else {
        message =
            ChunkClaimPrompt.joinText(
                " ",
                ChunkClaimPrompt.get("prompt.chunkclaim.you_own_chunk"),
                ChunkClaimPrompt.get("prompt.chunkclaim.cmd.usage.trust"));
      }
      visualizationType = VisualizationType.CHUNK;
    } else {
      // Information for a player with build permissions.
      if (chunk.canModify(player.getName())) {
        message = ChunkClaimPrompt.buildPermissionFrom(chunk.getOwnerName());
        visualizationType = VisualizationType.CHUNK;
      } else {
        // Information for a player with no build permission.
        message = ChunkClaimPrompt.noBuildPermissionFrom(chunk.getOwnerName());
        visualizationType = VisualizationType.ERROR_CHUNK;
      }
    }
    // Update visual
    Visualization.applyWithMessage(
        player,
        Visualization.fromChunk(chunk, location.getY(), visualizationType, location),
        message);
    return 0;
  }
}
