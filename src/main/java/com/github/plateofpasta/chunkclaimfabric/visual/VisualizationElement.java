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

import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import net.minecraft.util.math.Vec3d;

/** Represents a "fake" block sent to the player client. Used to visualize chunk boundaries. */
public class VisualizationElement {

  private final VisualizationMaterial visualizedMaterial;
  public EdgestitchLocation location;

  /**
   * Constructor for a VisualizationElement.
   *
   * @param location Location in the game world of the element.
   * @param visualizedMaterial Block material used as the visual.
   */
  public VisualizationElement(
      EdgestitchLocation location, VisualizationMaterial visualizedMaterial) {
    this.location = location;
    this.visualizedMaterial = visualizedMaterial;
  }

  /**
   * Get the visualized material.
   *
   * @return Visualized material.
   */
  public VisualizationMaterial getVisualizedMaterial() {
    return this.visualizedMaterial;
  }

  /**
   * Constructs a 3D location vector from the location coordinates.
   *
   * @return 3D coordinate representing the location's block.
   */
  public Vec3d getBlockVector3() {
    return new Vec3d(location.getX(), location.getY(), location.getZ());
  }
}
