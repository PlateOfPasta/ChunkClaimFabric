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

import com.github.plateofpasta.chunkclaimfabric.event.UseBedCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mixin dedicated for checking bed usage. */
@Mixin(net.minecraft.block.BedBlock.class)
public abstract class MixinBedBlock {

  /**
   * Inject into the head of the BedBlock#onUse.
   *
   * @param state BlockState of the bed. Ignored.
   * @param world World the bed is in.
   * @param pos Position of the bed block.
   * @param player Player attempting to use the bed block.
   * @param hand Player hand using the block. Ignored.
   * @param hit Hit result. Ignored.
   * @param info Mixin returnable info, used for cancellation of onUse.
   */
  @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
  public void onUse(
      BlockState state,
      World world,
      BlockPos pos,
      PlayerEntity player,
      Hand hand,
      BlockHitResult hit,
      CallbackInfoReturnable<ActionResult> info) {
    ActionResult result = UseBedCallback.EVENT.invoker().use(player, world, pos);

    if (result != ActionResult.PASS) {
      info.setReturnValue(result);
      info.cancel();
      return;
    }
  }
}
