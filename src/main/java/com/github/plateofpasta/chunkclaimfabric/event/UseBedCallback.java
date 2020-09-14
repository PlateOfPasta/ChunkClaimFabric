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

package com.github.plateofpasta.chunkclaimfabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Callback specifically for using (right-clicking) bed blocks. */
@FunctionalInterface
public interface UseBedCallback {
  Event<UseBedCallback> EVENT =
      EventFactory.createArrayBacked(
          UseBedCallback.class,
          (listeners) ->
              (playerEntity, world, blockPos) -> {
                for (UseBedCallback event : listeners) {
                  ActionResult result = event.use(playerEntity, world, blockPos);

                  if (result != ActionResult.PASS) {
                    return result;
                  }
                }
                return ActionResult.PASS;
              });

  /**
   * Callback for this interface. Provides the player entity and the world.
   *
   * @param playerEntity Player entity.
   * @param world World event occurred in.
   * @param blockPos Position of the block in the world.
   */
  ActionResult use(PlayerEntity playerEntity, World world, BlockPos blockPos);
}
