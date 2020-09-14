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

package com.github.plateofpasta.chunkclaimfabric.command.admin;

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimPlayer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

/** Predicate for chunk claim admin commands. Player must be an admin to execute these commands. */
public class AdminPredicate implements Predicate<ServerCommandSource> {

  /**
   * Evaluates this predicate on the given argument.
   *
   * @param input The input argument.
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  @Override
  public boolean test(ServerCommandSource input) {
    ChunkClaimPlayer player;
    try {
      player = new ChunkClaimPlayer(input.getPlayer());
    } catch (CommandSyntaxException e) {
      return false;
    }

    return player.hasModPermission();
  }
}
