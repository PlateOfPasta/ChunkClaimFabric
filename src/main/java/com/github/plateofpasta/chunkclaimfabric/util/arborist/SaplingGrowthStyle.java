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

package com.github.plateofpasta.chunkclaimfabric.util.arborist;

/** Interface for determining if a Minecraft tree was grown by a player. */
public interface SaplingGrowthStyle {

  /**
   * Getter for the interface.
   *
   * @return {@link GrowthType} of the object.
   */
  GrowthType getGrowthType();

  /**
   * Setter for the interface.
   *
   * @param growthType Growth type.
   */
  void setGrowthType(GrowthType growthType);
}
