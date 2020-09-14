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

package com.github.plateofpasta.chunkclaimfabric;

import com.github.plateofpasta.edgestitch.world.EdgestitchWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Abstracts a dedicated minecraft server. */
public class Server {
  private final MinecraftServer minecraftServer;

  /**
   * Constructor for the server API.
   *
   * @param minecraftServer Minecraft dedicated server that is wrapped by this.
   */
  public Server(MinecraftServer minecraftServer) {
    this.minecraftServer = minecraftServer;
  }

  /**
   * Gets a world by name.
   *
   * @param worldName Name of the world to get.
   * @return World object.
   */
  public EdgestitchWorld getWorld(String worldName) {
    for (ServerWorld world : minecraftServer.getWorlds()) {
      EdgestitchWorld EdgestitchWorld = new EdgestitchWorld(world);
      if (EdgestitchWorld.getName().equals(worldName)) {
        return EdgestitchWorld;
      }
    }
    return null;
  }

  /** @return List of world name(s) available to this server. */
  public List<String> getAvailableWorldNames() {
    List<String> worldNames = new ArrayList<>(3);
    for (ServerWorld world : minecraftServer.getWorlds()) {
      worldNames.add(EdgestitchWorld.Companion.getName(world));
    }
    return worldNames;
  }

  /** @return Array of player names. */
  public String[] getOnlinePlayerNames() {
    return minecraftServer.getPlayerNames();
  }

  /** @return Run directory of the minecraft server. */
  public File getRunDirectory() {
    return this.minecraftServer.getRunDirectory();
  }
}
