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
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import com.github.plateofpasta.edgestitch.world.EdgestitchWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mixin to World class for preventing players from extinguishing fires. */
@Mixin(PotionEntity.class)
public abstract class MixinPotionEntity {
  /**
   * Prevents fire extinguishing if thrown by a player that cannot modify the chunk.
   *
   * @param blockPos Position of the block that is on fire.
   * @param direction Which face the fire is on the base block. Ignored.
   * @param info Returnable callback info.
   */
  @Inject(at = @At("HEAD"), method = "extinguishFire", cancellable = true)
  private void preventExtinguishFire(BlockPos blockPos, Direction direction, CallbackInfo info) {
    PotionEntity potionEntity = (PotionEntity) (Object) this;
    if (ChunkClaimFabric.isConfiguredWorld(
        EdgestitchWorld.Companion.getName(potionEntity.getEntityWorld()))) {
      Entity entityThrower = potionEntity.getOwner();
      boolean shouldCancel;
      if (!(entityThrower instanceof PlayerEntity)) {
        shouldCancel = true;
      } else {
        ChunkClaimPlayer player = new ChunkClaimPlayer((PlayerEntity) entityThrower);
        EdgestitchLocation location =
            new EdgestitchLocation(potionEntity.getEntityWorld(), blockPos);
        shouldCancel = !ChunkClaimFabric.canPlayerModifyAtLocation(player, location);
      }
      if (shouldCancel) {
        info.cancel();
      }
    }
  }
}
