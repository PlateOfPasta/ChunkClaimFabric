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

import com.github.plateofpasta.chunkclaimfabric.mixin.arborist.MixinTreeFeature;
import com.github.plateofpasta.chunkclaimfabric.util.arborist.GrowthType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.TreeFeature;

import java.util.Set;

/**
 * Fabric API event interface for when a tree is grown by a player. Note, this occurs after all of
 * the blocks have been generated in the game world. Thus, it is recommended to iterate over the
 * collection of block positions for the logs/leaves and utilize the interface {@link
 * TreeFeature#setBlockStateWithoutUpdatingNeighbors} to edit the blocks as necessary. \n Note: the
 * com.github.plateofpasta.chunkclaimfabric.util.arborist.GrowthType#WORLD_GEN} growth type is
 * excluded from this event.
 *
 * @see MixinTreeFeature
 */
@FunctionalInterface
public interface SaplingGrowCallback {
  Event<SaplingGrowCallback> EVENT =
      EventFactory.createArrayBacked(
          SaplingGrowCallback.class,
          (listeners) ->
              (growthType,
                  feature,
                  world,
                  rootBlockPos,
                  logPositions,
                  leavesPositions,
                  decoratorPositions) -> {
                for (SaplingGrowCallback event : listeners) {
                  ActionResult result =
                      event.grow(
                          growthType,
                          feature,
                          world,
                          rootBlockPos,
                          logPositions,
                          leavesPositions,
                          decoratorPositions);

                  if (result != ActionResult.PASS) {
                    return result;
                  }
                }
                return ActionResult.PASS;
              });

  /**
   * Callback for this interface.
   *
   * @param growthType Growth type of the sapling. Excludes WORLD_GEN growth type.
   * @param feature Feature used to generate the tree.
   * @param world World the tree growth is occurring in.
   * @param rootBlockPos The "root" position of the tree growth.
   * @param logPositions Collection of block positions for the tree's log blocks.
   * @param leavesPositions Collection of block positions for the tree's leaf blocks.
   * @param decoratorPositions Collection of block positions for the tree's decoration blocks.
   * @return PASS if the tree growth should proceed as normal, FAIL if the blocks in each set should
   *     be replaced with an air block.
   */
  ActionResult grow(
      GrowthType growthType,
      TreeFeature feature,
      World world,
      BlockPos rootBlockPos,
      Set<BlockPos> logPositions,
      Set<BlockPos> leavesPositions,
      Set<BlockPos> decoratorPositions);
}
