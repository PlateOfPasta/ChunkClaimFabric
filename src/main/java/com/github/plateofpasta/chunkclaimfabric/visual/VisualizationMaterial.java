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

package com.github.plateofpasta.chunkclaimfabric.visual;

import net.minecraft.block.BlockState;

/** Abstracts the exact implementation of ChunkClaim visualization material. */
public class VisualizationMaterial {
  protected final BlockState visualBlockState;
  /**
   * Constructor implementation for worldedit-core block types.
   *
   * @param blockState BlockType object from worldedit-core.
   */
  protected VisualizationMaterial(BlockState blockState) {
    this.visualBlockState = blockState;
  }

  /** @return {@link BlockState} that will be used for the visualization. */
  public BlockState getVisualizedBlock() {
    return this.visualBlockState;
  }
}
