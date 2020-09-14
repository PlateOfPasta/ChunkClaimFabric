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
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/**
 * Fabric API event interface for when a {@link net.minecraft.block.SpreadableBlock} is spreading to
 * another block.
 *
 * @see com.github.plateofpasta.chunkclaimfabric.mixin.MixinSpreadableBlock
 */
@FunctionalInterface
public interface BlockSpreadCallback {
  Event<BlockSpreadCallback> EVENT =
      EventFactory.createArrayBacked(
          BlockSpreadCallback.class,
          (listeners) ->
              (world, fromBlockPos, toBlockPos, blockState) -> {
                for (BlockSpreadCallback event : listeners) {
                  ActionResult result = event.spread(world, fromBlockPos, toBlockPos, blockState);

                  if (result != ActionResult.PASS) {
                    return result;
                  }
                }
                return ActionResult.PASS;
              });

  /**
   * Callback for this interface.
   *
   * @param world World in which the block spread event is occurring.
   * @param fromBlockPos Block position in the world of the spreading block.
   * @param toBlockPos Block position in the world of the block being spread to.
   * @param blockState New state of the block if the spread is successful.
   * @return PASS if the spread should be allowed to occur normally, else FAIL if the block spread
   *     should not occur.
   */
  ActionResult spread(
      ServerWorld world, BlockPos fromBlockPos, BlockPos toBlockPos, BlockState blockState);
}
