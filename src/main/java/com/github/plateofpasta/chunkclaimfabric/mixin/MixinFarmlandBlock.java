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

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.edgestitch.world.EdgestitchWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mixin to prevent farmland from being trampled to dirt by players. */
@Mixin(FarmlandBlock.class)
public abstract class MixinFarmlandBlock {

  /**
   * Prevents the {@link FarmlandBlock#setToDirt} method from performing any actions if invoked in a
   * chunk claim world.
   *
   * @param info Inject mixin info used to cancel.
   * @param state Farmland block that is the target of the mixin target. Ignored.
   * @param world World block is in.
   * @param pos Position in the world of the block. Ignored.
   */
  @Inject(method = "setToDirt", at = @At("HEAD"), cancellable = true)
  private static void preventSetToDirt(
      BlockState state, World world, BlockPos pos, CallbackInfo info) {
    // Force early return of setToDirt if this a configured world.
    if (ChunkClaimFabric.isConfiguredWorld(EdgestitchWorld.Companion.getName(world))) {
      info.cancel();
    }
  }
}
