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

package com.github.plateofpasta.chunkclaimfabric.mixin.arborist;

import com.github.plateofpasta.chunkclaimfabric.util.arborist.GrowthType;
import com.github.plateofpasta.chunkclaimfabric.util.arborist.SaplingGrowthStyle;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to utilize the {@link SaplingGrowthStyle} interface at the "origin" of knowledge for
 * determining if a player grew the tree.
 */
@Mixin(SaplingBlock.class)
public abstract class MixinSaplingBlock {

  /** Shadow variable used with {@link SaplingGrowthStyle}. */
  @Shadow @Final private SaplingGenerator generator;

  /**
   * Mixin to determine if a sapling was grown naturally.
   *
   * @param info Callback info.
   */
  @Inject(method = "generate", at = @At("HEAD"))
  void mixinPlayerSaplingGenerate(CallbackInfo info) {
    SaplingGrowthStyle generatorGrowthStyle = (SaplingGrowthStyle) this.generator;
    if (GrowthType.PLAYER_GROWTH != generatorGrowthStyle.getGrowthType()) {
      // Natural growth has lower "precedence" than player growth.
      generatorGrowthStyle.setGrowthType(GrowthType.NATURAL_GROWTH);
    }
  }

  /**
   * Mixin to determine if a sapling was grown by a player.
   *
   * @param info Callback info.
   */
  @Inject(method = "grow", at = @At("HEAD"))
  void mixinPlayerSaplingGrow(CallbackInfo info) {
    ((SaplingGrowthStyle) this.generator).setGrowthType(GrowthType.PLAYER_GROWTH);
  }
}
