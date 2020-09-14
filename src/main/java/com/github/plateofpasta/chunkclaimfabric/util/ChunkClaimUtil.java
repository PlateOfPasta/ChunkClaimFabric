package com.github.plateofpasta.chunkclaimfabric.util;

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import com.github.plateofpasta.edgestitch.world.EdgestitchWorld;
import net.minecraft.world.World;

import java.util.ArrayList;

/** Utility methods that don't have a better home. */
public class ChunkClaimUtil {
  /**
   * Get all the chunks in within the radius (centered at the given chunk) that are owned by the
   * player.
   *
   * @param location Location in the center chunk.
   * @param targetPlayerName Player whose chunk's we're trying to find.
   * @param radius Search radius. Value of zero will return only the center chunk if valid.
   * @return List of chunks owned by the player.
   */
  public static ArrayList<Chunk> getChunksInRadius(
      EdgestitchLocation location, String targetPlayerName, int radius) {
    Chunk chunk = new Chunk(location);
    ArrayList<Chunk> chunksInRadius = new ArrayList<Chunk>();

    for (int x = chunk.getCoordX() - radius; x <= chunk.getCoordX() + radius; x++) {
      for (int z = chunk.getCoordZ() - radius; z <= chunk.getCoordZ() + radius; z++) {

        Chunk foundChunk =
            ChunkClaimFabric.getPlugin().getDataStore().getChunkAtPos(x, z, chunk.getWorldName());

        if (foundChunk != null && foundChunk.getOwnerName().equals(targetPlayerName)) {
          chunksInRadius.add(foundChunk);
        }
      }
    }
    return chunksInRadius;
  }

  /**
   * Checks if the given world is covered under the protection of ChunkClaim.
   *
   * @param worldName World to check.
   * @return true if the world is covered under ChunkClaim, else false.
   */
  private static boolean isConfiguredWorld(String worldName) {
    return ChunkClaimFabric.getClaimConfig().getWorlds().contains(worldName);
  }

  /**
   * Checks if the given world is covered under the protection of ChunkClaim.
   *
   * @param world World to check.
   * @return true if the world is covered under ChunkClaim, else false.
   */
  public static boolean isConfiguredWorld(EdgestitchWorld world) {
    return isConfiguredWorld(world.getName());
  }

  /**
   * Checks if the given world is covered under the protection of ChunkClaim.
   *
   * @param world World to check.
   * @return true if the world is covered under ChunkClaim, else false.
   */
  public static boolean isConfiguredWorld(World world) {
    return isConfiguredWorld(EdgestitchWorld.Companion.getName(world));
  }
}
