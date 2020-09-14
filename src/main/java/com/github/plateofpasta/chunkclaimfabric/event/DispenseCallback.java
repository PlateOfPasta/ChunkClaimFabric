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
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Fabric API event interface for when a {@link net.minecraft.block.DispenserBlock} or {@link
 * net.minecraft.block.DropperBlock} is dispensing/dropping to another block.
 *
 * @see com.github.plateofpasta.chunkclaimfabric.mixin.MixinDispenserBlock
 */
@FunctionalInterface
public interface DispenseCallback {
  Event<DispenseCallback> EVENT =
      EventFactory.createArrayBacked(
          DispenseCallback.class,
          (listeners) ->
              (world, fromBlockPos, toBlockPos) -> {
                for (DispenseCallback event : listeners) {
                  ActionResult result = event.dispense(world, fromBlockPos, toBlockPos);

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
   * @param dispenserPos Block position in the world of dispenser.
   * @param toBlockPos Block position in the world of the block being dispensed into.
   * @return PASS if the dispense action should be allowed to occur normally, else FAIL if it should
   *     not occur.
   */
  ActionResult dispense(World world, BlockPos dispenserPos, BlockPos toBlockPos);
}
