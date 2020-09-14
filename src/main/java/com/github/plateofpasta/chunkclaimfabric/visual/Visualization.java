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

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import com.github.plateofpasta.edgestitch.world.EdgestitchWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;

/** Represents a visualization sent to a player. */
public class Visualization {
  public ArrayList<VisualizationElement> elements = new ArrayList<>();

  /**
   * Factory method to build a visualization from a claim. VisualizationType determines the style
   * (gold blocks, silver, red, diamond, etc).
   *
   * @param chunk Chunk to be visualized.
   * @param coordY Y-coordinate of the chunk.
   * @param visualizationType Type of visualization.
   * @param not Location not to apply the visualization.
   * @return Constructed Visualization.
   */
  public static Visualization fromChunk(
      Chunk chunk, int coordY, VisualizationType visualizationType, EdgestitchLocation not) {
    Visualization visualization = new Visualization();
    visualization.addChunkElements(chunk, coordY, visualizationType, not);
    return visualization;
  }

  /**
   * Applies a visualization to a player
   *
   * @param player Player to send visualization to.
   * @param visualization Visualization to visualize.
   */
  public static void apply(ChunkClaimPlayer player, Visualization visualization) {
    PlayerData playerData =
        ChunkClaimFabric.getPlugin().getDataStore().getPlayerData(player.getName());

    // If the player has any current visualization, clear it first.
    if (playerData.getCurrentVisualization() != null) {
      Visualization.revert(player);
    }

    playerData.setCurrentVisualization(visualization);
    // Create a task to send the player the visualization in about half a second.
    ChunkClaimFabric.getScheduler()
        .queue(
            minecraftServer -> visualizationApplicationTask(player, playerData, visualization), 10);

    // Clear the visualization after 20 seconds.
    ChunkClaimFabric.getScheduler()
        .queue(minecraftServer -> visualizationClearTask(player, playerData, visualization), 400);
  }

  public static void applyWithMessage(
      ChunkClaimPlayer player, Visualization visualization, MutableText message) {
    player.sendMessage(message);
    Visualization.apply(player, visualization);
  }

  /**
   * Reverts a visualization by sending another block change list, this time with the real world
   * block values.
   *
   * @param player Player to revert visualization.
   */
  public static void revert(ChunkClaimPlayer player) {
    PlayerData playerData =
        ChunkClaimFabric.getPlugin().getDataStore().getPlayerData(player.getName());
    Visualization visualization = playerData.getCurrentVisualization();

    if (null != visualization) {
      visualizationClearTask(player, playerData, visualization);
      playerData.setCurrentVisualization(null);
    }
  }

  /**
   * Function for applying visualization.
   *
   * @param player Player to apply the visualization to.
   * @param playerData Meta-data related to the player.
   * @param visualization Visualization to apply.
   */
  private static void visualizationApplicationTask(
      ChunkClaimPlayer player, PlayerData playerData, Visualization visualization) {
    // For each element (=block) of the visualization
    if (playerData.getCurrentVisualization() == visualization) {
      for (VisualizationElement element : visualization.elements) {
        // Send the player a fake block change event.
        if (element.location != null) {
          player.sendFakeBlock(
              element.getVisualizedMaterial().getVisualizedBlock(), element.getBlockVector3());
        }
      }
    }
  }

  /**
   * Function for clearing visualization.
   *
   * @param player Player to clear the visualization from.
   * @param playerData Meta-data related to the player.
   * @param visualization Visualization to clear.
   */
  private static void visualizationClearTask(
      ChunkClaimPlayer player, PlayerData playerData, Visualization visualization) {
    if (playerData.getCurrentVisualization() == visualization) {
      for (VisualizationElement element : visualization.elements) {
        if (element.location != null) {
          BlockState block = element.location.getBlockState();
          player.sendFakeBlock(block, element.getBlockVector3());
        }
      }
    }
  }

  /**
   * Finds a block the player can probably see, so visualizations "cling" to the ground or ceiling.
   *
   * @param world World we're currently in, required to get locations and blocks.
   * @param coord3D Coordinate to start looking.
   * @param not Location that is not a valid location.
   * @return Selected visible location, or null.
   */
  private static EdgestitchLocation getVisibleLocation(
      EdgestitchWorld world, Vec3d coord3D, EdgestitchLocation not) {
    final int maxY = world.getMaxY();
    BlockState block = world.getBlockState(coord3D);
    final Direction direction = (isSeeThrough(block)) ? Direction.DOWN : Direction.UP;

    // While within height bounds and is somehow visible.
    while ((1 < coord3D.getY())
        && (coord3D.getY() < (maxY - 1))
        && ((!isSeeThrough(getRelativeBlock(coord3D, Direction.UP, world)))
            || isSeeThrough(block))) {
      coord3D = getRelativeCoord(coord3D, direction);
      block = world.getBlockState(coord3D);
    }

    EdgestitchLocation location = new EdgestitchLocation(world, coord3D);

    if (not != null && (location.equalsPosition(not))) {
      return null;
    } else {
      return location;
    }
  }

  /**
   * Gets the block coordinate in the relative direction to the given one.
   *
   * @param coord3D Reference block.
   * @param relative Direction to calculate relative block from the reference.
   * @return Relative block coordinate.
   */
  private static Vec3d getRelativeCoord(Vec3d coord3D, Direction relative) {
    Vec3i relativeVec = relative.getVector();
    return coord3D.add(new Vec3d(relativeVec.getX(), relativeVec.getY(), relativeVec.getZ()));
  }

  /**
   * Gets the block in the relative direction to the given one.
   *
   * @param coord3D Reference block.
   * @param relative Direction to calculate relative block from the reference.
   * @return Relative block coordinate.
   */
  private static BlockState getRelativeBlock(
      Vec3d coord3D, Direction relative, EdgestitchWorld world) {
    return world.getBlockState(getRelativeCoord(coord3D, relative));
  }

  /**
   * Determines if a block is see through to allow visualization blocks to appear underneath. The
   * goal of this is to render a visualization under blocks like air, flowers, fences, etc. instead
   * of replacing them (which looks weird).
   *
   * @param blockState Block to check.
   * @return true if is see through, else false.
   */
  private static boolean isSeeThrough(BlockState blockState) {
    Material blockMaterial = blockState.getMaterial();
    return (!blockMaterial.isLiquid())
        && (blockState.isAir()
            || blockMaterial.equals(Material.PLANT)
            || blockMaterial.isReplaceable());
  }

  /**
   * Add visualization elements to the chunk.
   *
   * @param chunk Chunk to add visualization to.
   * @param height Y-coordinate to put the visualization at. Note, the visualization may appear at a
   *     different y-coordinate depending on if a block is see through.
   * @param vtype Visualization type.
   * @param not Location to not put the visualization at.
   */
  private void addChunkElements(
      Chunk chunk, int height, VisualizationType vtype, EdgestitchLocation not) {
    EdgestitchWorld world = ChunkClaimFabric.getPlugin().getServer().getWorld(chunk.getWorldName());

    int smallX = chunk.getStartX();
    int smallZ = chunk.getStartZ();

    int bigX = chunk.getEndX();
    int bigZ = chunk.getEndZ();

    final Vec3d[] cornerVisualCoords = {
      // Bottom left corner.
      new Vec3d(smallX, height, smallZ),
      // Bottom right corner
      new Vec3d(bigX, height, smallZ),
      // Top right corner.
      new Vec3d(bigX, height, bigZ),
      // Top left corner.
      new Vec3d(smallX, height, bigZ),
    };

    final Vec3d[] accentVisualCoords = {
      // Bottom left corner.
      new Vec3d(smallX + 1, height, smallZ), new Vec3d(smallX + 2, height, smallZ),
      new Vec3d(smallX, height, smallZ + 1), new Vec3d(smallX, height, smallZ + 2),
      // Bottom right corner
      new Vec3d(bigX - 1, height, smallZ), new Vec3d(bigX - 2, height, smallZ),
      new Vec3d(bigX, height, smallZ + 1), new Vec3d(bigX, height, smallZ + 2),
      // Top right corner.
      new Vec3d(bigX - 1, height, bigZ), new Vec3d(bigX - 2, height, bigZ),
      new Vec3d(bigX, height, bigZ - 1), new Vec3d(bigX, height, bigZ - 2),
      // Top left corner.
      new Vec3d(smallX + 1, height, bigZ), new Vec3d(smallX + 2, height, bigZ),
      new Vec3d(smallX, height, bigZ - 1), new Vec3d(smallX, height, bigZ - 2),
    };

    VisualizationMaterial cornerMaterial = vtype.getCornerMaterial();
    VisualizationMaterial accentMaterial = vtype.getAccentMaterial();

    for (Vec3d cornerCoord : cornerVisualCoords) {
      EdgestitchLocation loc = getVisibleLocation(world, cornerCoord, not);
      this.elements.add(new VisualizationElement(loc, cornerMaterial));
    }

    for (Vec3d accentCoord : accentVisualCoords) {
      EdgestitchLocation loc = getVisibleLocation(world, accentCoord, not);
      this.elements.add(new VisualizationElement(loc, accentMaterial));
    }
  }
}
