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

import com.github.plateofpasta.chunkclaimfabric.event.SaplingGrowCallback;
import com.github.plateofpasta.chunkclaimfabric.util.arborist.GrowthType;
import com.github.plateofpasta.chunkclaimfabric.util.arborist.SaplingGrowthStyle;
import net.minecraft.block.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

/**
 * Modifies Minecraft tree generation so that we can edit the exact placement of logs and leaves.
 */
@Mixin(TreeFeature.class)
public abstract class MixinTreeFeature {
  /**
   * Mixin that redirects the tree generation function.
   *
   * @param world World generation is occurring in.
   * @param chunkGenerator Unused, required for mixin signature.
   * @param random Unused, required for mixin signature.
   * @param rootPos Position of the "root" of generation. This is from the {@code
   *     TreeFeature;generate} parameter list.
   * @param treeFeatureConfig Tree generation config. This is from the {@code TreeFeature;generate}
   *     parameter list.
   * @param info Mixin callback info.
   * @param logs Contains all coordinate positions of the tree's log blocks **after** generation.
   * @param leaves Contains all coordinate positions of the tree's leaf blocks **after** generation.
   * @param decorations Contains all coordinate positions of the tree's decoration blocks **after**
   *     generation.
   */
  @Inject(
      method =
          "Lnet/minecraft/world/gen/feature/TreeFeature;generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)Z",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/gen/feature/TreeFeature;placeLogsAndLeaves(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockBox;Ljava/util/Set;Ljava/util/Set;)Lnet/minecraft/util/shape/VoxelSet;",
              ordinal = 0),
      locals = LocalCapture.CAPTURE_FAILHARD)
  private void abstractTreeFeatureGenerationMixinV2(
      StructureWorldAccess world,
      ChunkGenerator chunkGenerator,
      Random random,
      BlockPos rootPos,
      TreeFeatureConfig treeFeatureConfig,
      CallbackInfoReturnable<Boolean> info,
      Set<BlockPos> logs,
      Set<BlockPos> leaves,
      Set<BlockPos> decorations) {
    TreeFeature feature = (TreeFeature) (Object) this;
    // Early return if this generation occurred due to world generation.
    GrowthType growthType = ((SaplingGrowthStyle) treeFeatureConfig).getGrowthType();
    if (GrowthType.WORLD_GEN == growthType) {
      return;
    }
    // Invoke sapling growth event callback.
    ActionResult result =
        SaplingGrowCallback.EVENT
            .invoker()
            .grow(growthType, feature, (World) world, rootPos, logs, leaves, decorations);
    if (ActionResult.PASS != result) {
      this.removeGeneratedBlock(world, logs, leaves, decorations);
    }
  }

  /**
   * Helper for removing generated blocks.
   *
   * @param world World generation is occurring in.
   * @param logPositions Collection of coordinates for the generated tree's logs.
   * @param leavesPositions Collection of coordinates for the generated tree's leaves.
   */
  private void removeGeneratedBlock(
      ModifiableTestableWorld world,
      Set<BlockPos> logPositions,
      Set<BlockPos> leavesPositions,
      Set<BlockPos> decoratorPositions) {
    for (Set<BlockPos> posSet : Arrays.asList(logPositions, leavesPositions, decoratorPositions)) {
      for (BlockPos blockPos : posSet) {
        TreeFeature.setBlockStateWithoutUpdatingNeighbors(
            world, blockPos, Blocks.AIR.getDefaultState());
      }
    }
  }
}
