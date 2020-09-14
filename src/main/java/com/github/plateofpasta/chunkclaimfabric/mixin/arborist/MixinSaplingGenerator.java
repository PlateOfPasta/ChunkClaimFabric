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
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Mixin to implement and utilize the {@link SaplingGrowthStyle} interface. */
@Mixin(SaplingGenerator.class)
public abstract class MixinSaplingGenerator implements SaplingGrowthStyle {

  /** Added unique mixin variable for {@link SaplingGrowthStyle} interface implementation. */
  @Unique private GrowthType growthType = GrowthType.WORLD_GEN;

  /**
   * Implements the interface.
   *
   * @return {@link GrowthType} of the object.
   */
  @Override
  public GrowthType getGrowthType() {
    return this.growthType;
  }

  /**
   * Implements the interface.
   *
   * @param growthType Growth type.
   */
  public void setGrowthType(GrowthType growthType) {
    this.growthType = growthType;
  }

  /**
   * Modifies the configured feature to indicate growth style. Occurs after it is created by a
   * factory method.
   *
   * @param feature Feature to modify.
   * @return Modified feature.
   */
  @ModifyVariable(
      method = "generate",
      at =
          @At(
              value = "INVOKE_ASSIGN",
              target =
                  "net/minecraft/block/sapling/SaplingGenerator.createTreeFeature(Ljava/util/Random;Z)Lnet/minecraft/world/gen/feature/ConfiguredFeature;",
              ordinal = 0),
      name = "configuredFeature")
  ConfiguredFeature<TreeFeatureConfig, ?> modifyCreateTreeFeature(
      ConfiguredFeature<TreeFeatureConfig, ?> feature) {
    if (null != feature) {
      ((SaplingGrowthStyle) feature.config)
          .setGrowthType(((SaplingGrowthStyle) this).getGrowthType());
    }
    return feature;
  }
}
