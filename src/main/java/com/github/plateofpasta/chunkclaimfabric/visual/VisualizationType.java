/*
   ChunkClaim Plugin for Minecraft Bukkit Servers
   Copyright (C) 2012 Felix Schmidt
   Copyright (C) 2020 PlateOfPasta: Notice of modification for ChunkClaimFabric

   This file is part of ChunkClaim.

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
import net.minecraft.block.Blocks;

/** Defines the types of chunk visualization. */
public enum VisualizationType {
  DEFAULT(Blocks.SNOW_BLOCK.getDefaultState(), Blocks.SNOW_BLOCK.getDefaultState()),
  CHUNK(Blocks.WHITE_WOOL.getDefaultState(), Blocks.WHITE_WOOL.getDefaultState()),
  ERROR_CHUNK(Blocks.REDSTONE_BLOCK.getDefaultState(), Blocks.REDSTONE_BLOCK.getDefaultState()),
  PUBLIC(Blocks.WHITE_WOOL.getDefaultState(), Blocks.WHITE_WOOL.getDefaultState());

  final VisualizationMaterial cornerMaterial;
  final VisualizationMaterial accentMaterial;

  /**
   * @param cornerMaterial Corner material for this type.
   * @param accentMaterial Accent material for this type.
   */
  VisualizationType(BlockState cornerMaterial, BlockState accentMaterial) {
    this.cornerMaterial = new VisualizationMaterial(cornerMaterial);
    this.accentMaterial = new VisualizationMaterial(accentMaterial);
  }

  /** @return Corner material for this type. */
  public VisualizationMaterial getCornerMaterial() {
    return this.cornerMaterial;
  }

  /** @return Accent material for this type. */
  public VisualizationMaterial getAccentMaterial() {
    return this.accentMaterial;
  }
}
