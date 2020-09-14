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

import com.github.plateofpasta.chunkclaimfabric.event.DispenseCallback;
import net.minecraft.block.DispenserBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Mixin for changing the dispensing behavior of dispensers and droppers. */
@Mixin(DispenserBlock.class)
public abstract class MixinDispenserBlock {

  /**
   * Shadow mixin to make the dispense method accessible through this mixin.
   *
   * @param world World the dispenser block is in.
   * @param pos Position of the dispenser block.
   */
  @Shadow
  protected abstract void dispense(ServerWorld world, BlockPos pos);

  /**
   * Redirects a dispense operation (for dispensers or droppers) so that we can control the dispense
   * operation.
   *
   * @param block Dispenser or dropper block, referenced through the dispenser parent class.
   * @param world World the block is in.
   * @param pos Position of the block in the world.
   */
  @Redirect(
      method = "scheduledTick",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/block/DispenserBlock;dispense(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V",
              ordinal = 0))
  private void controlDispenseMixin(DispenserBlock block, ServerWorld world, BlockPos pos) {
    world.getBlockState(pos).get(DispenserBlock.FACING);
    ActionResult result =
        DispenseCallback.EVENT
            .invoker()
            .dispense(world, pos, pos.offset(world.getBlockState(pos).get(DispenserBlock.FACING)));
    if (ActionResult.PASS != result) {
      // Do not invoke invoke original logic.
      return;
    } else {
      // Invoke original logic.
      ((MixinDispenserBlock) (Object) block).dispense(world, pos);
    }
  }
}
