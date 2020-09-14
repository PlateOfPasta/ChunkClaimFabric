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
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;

public class Untrust implements Command<ServerCommandSource> {
  public static final String NAMESPACE = "untrust";
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
    DataStore dataStore = ChunkClaimFabric.getPlugin().getDataStore();
    ChunkClaimPlayer player = new ChunkClaimPlayer(context.getSource().getPlayer());
    PlayerData playerData = dataStore.getPlayerData(player.getName());

    // Parse args
    ChunkClaimPlayer targetPlayer = ChunkCommands.parsePlayerArg(context, ARG0_NAMESPACE);
    PlayerData targetPlayerData = dataStore.getPlayerData(targetPlayer.getName());
    if (null == targetPlayerData) {
      player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.player_not_found"));
      return 0;
    }

    String tName = targetPlayer.getName();
    if (tName.equals(player.getName())) {
      player.sendMessages(ChunkClaimPrompt.get("prompt.chunkclaim.dont_trust_self"));
      return 0;
    }

    // Remove the target player name from the builder lists.
    // todo refactor Chunk#builderNames, because it's horribly inefficient.
    MutableText message;
    if (playerData.getBuilderNames().contains(tName)) {
      for (Chunk inRadius : dataStore.getAllChunksForPlayer(player.getName())) {
        if (inRadius.canModify(tName)) {
          inRadius.removeBuilderName(tName);
          dataStore.writeChunkToStorage(inRadius);
        }
      }
      playerData.removeBuilderName(tName);
      dataStore.savePlayerData(player.getName(), playerData);
      message = ChunkClaimPrompt.untrustedBuilder(tName);
    } else {
      message = ChunkClaimPrompt.get("prompt.chunkclaim.already_untrusted");
    }
    player.sendMessages(message);
    return 0;
  }
}
