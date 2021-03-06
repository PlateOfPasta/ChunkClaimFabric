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

package com.github.plateofpasta.chunkclaimfabric.command.admin;

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.command.ChunkCommands;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimPrompt;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

/** Bonus command. */
public class Bonus implements Command<ServerCommandSource> {
  public static String NAMESPACE = "bonus";
  public static String ARG0_NAMESPACE = "player";
  public static String ARG1_NAMESPACE = "amount";

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
    // Parse args.
    ChunkClaimPlayer targetPlayer = ChunkCommands.parsePlayerArg(context, ARG0_NAMESPACE);
    int bonus = context.getArgument(ARG1_NAMESPACE, Integer.class);

    PlayerData targetPlayerData = dataStore.getPlayerData(targetPlayer.getName());

    if (null == targetPlayerData) {
      player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.player_not_found"));
    } else {
      targetPlayerData.addCredits(bonus);
      targetPlayerData.addBonus(bonus);
      dataStore.savePlayerData(player.getName(), targetPlayerData);

      player.sendMessage(
          ChunkClaimPrompt.joinText(
              " ",
              ChunkClaimPrompt.adjustedBonus(targetPlayer.getName(), bonus),
              ChunkClaimPrompt.creditsAmount(targetPlayerData.getCredits())));
    }

    return 0;
  }
}
