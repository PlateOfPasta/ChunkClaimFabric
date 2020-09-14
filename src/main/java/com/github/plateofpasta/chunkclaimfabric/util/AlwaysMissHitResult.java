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

package com.github.plateofpasta.chunkclaimfabric.util;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

/** Utility class for always returning a missed hit result. */
public class AlwaysMissHitResult extends HitResult {

  /**
   * Constructor built from a {@link net.minecraft.util.hit.HitResult} position.
   *
   * @param pos Hit result position.
   */
  public AlwaysMissHitResult(Vec3d pos) {
    super(pos);
  }

  /**
   * Overrides parent interface, always returns MiSS.
   *
   * @return MISS always.
   */
  @Override
  public Type getType() {
    return Type.MISS;
  }
}
