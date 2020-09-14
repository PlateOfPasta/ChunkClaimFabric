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

package com.github.plateofpasta.chunkclaimfabric.mixin;

import com.github.plateofpasta.chunkclaimfabric.event.BlockSpreadCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

/** Mixin for modifying the behavior of spreadable blocks like grass and mycelium. */
@Mixin(SpreadableBlock.class)
public abstract class MixinSpreadableBlock {
  /**
   * Mixin to change how spreadable blocks spread. This only effects spread and not death of
   * spreadable blocks.
   *
   * @param world Reference object whose method is being redirected - world object used to change
   *     the state of a block.
   * @param toBlockPos Block position of the redirect - position of the block being changed.
   * @param blockState BlockState of the redirect - what the block at the blockPos would've become.
   * @param fromState BlockState the spread is coming from.
   * @param fromWorld World where the spread is occurring from, same as other world parameter.
   *     Required due to mixin capture rules.
   * @param fromPos Block position the spread is coming from.
   * @param random Random generator used in the target original code. Ignored.
   * @return true if the
   */
  @Redirect(
      method = "randomTick",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z",
              ordinal = 1))
  private boolean setSpreadedBlockStateRedirect(
      ServerWorld world,
      BlockPos toBlockPos,
      BlockState blockState,
      BlockState fromState,
      ServerWorld fromWorld,
      BlockPos fromPos,
      Random random) {
    ActionResult result =
        BlockSpreadCallback.EVENT.invoker().spread(world, fromPos, toBlockPos, blockState);
    if (ActionResult.PASS == result) {
      // Perform the intended operation.
      return world.setBlockState(toBlockPos, blockState);
    } else {
      return false;
    }
  }
}
