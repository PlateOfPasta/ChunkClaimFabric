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

package com.github.plateofpasta.chunkclaimfabric.world;

import com.google.common.collect.HashBasedTable;

/** Chunk data store per world. */
public class ChunkWorld {
  public String worldName;
  public HashBasedTable<Integer, Integer, Chunk> chunkTable = HashBasedTable.create();

  /**
   * Creates the datastore for the world.
   *
   * @param worldName World associated with this datastore.
   */
  public ChunkWorld(String worldName) {
    this.worldName = worldName;
  }

  /**
   * Gets the chunk at the X,Z coordinates.
   *
   * @param x X-coordinate of the chunk.
   * @param z Z-coordinate of the chunk.
   * @return Chunk mapped to the coordinates if it exists in the datastore, null otherwise.
   */
  public Chunk getChunk(int x, int z) {
    return chunkTable.get(x, z);
  }

  /**
   * Adds the chunk to the datastore.
   *
   * @param newChunk Chunk to add to the datastore.
   */
  public void addChunk(Chunk newChunk) {
    chunkTable.put(newChunk.getCoordX(), newChunk.getCoordZ(), newChunk);
  }

  /**
   * Removes the chunk from the datastore.
   *
   * @param chunk Chunk to add to the datastore.
   */
  public void removeChunk(Chunk chunk) {
    chunkTable.remove(chunk.getCoordX(), chunk.getCoordZ());
  }
}
