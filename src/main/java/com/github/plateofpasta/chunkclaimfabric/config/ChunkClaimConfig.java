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

package com.github.plateofpasta.chunkclaimfabric.config;

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

import java.util.List;

/** Class for containing plugin configurations. */
@Config(name = ChunkClaimFabric.MOD_ID)
public class ChunkClaimConfig implements ConfigData {
  @Comment(value = "List of world names configured for chunk claim.")
  protected List<String> worlds;

  @Comment(
      value =
          "Configures the number of days before a claimed chunk that has not met the "
              + "minimum amount of modified blocks is to be automatically deleted.\n"
              + "A value of zero disables this feature.\n"
              + "See related configuration: minModBlocks")
  protected double autoDeleteDays = 0;

  @Comment(value = "Credits price to claim a single chunk. A price of zero is valid.")
  protected double chunkPrice = 1.0;

  @Comment(value = "Number of credits acquired per hour of continuous and non-idle play time.")
  protected double creditsPerHour = 1.0;

  @Comment(value = "Maximum number of credits a player can have.")
  protected double maxCredits = Double.MAX_VALUE;

  @Comment(
      value =
          "Minimum number of blocks that have to be modified in a chunk for it to\n"
              + "be considered \"built\" upon.\n"
              + "See related configuration: autoDeleteDays")
  protected int minModBlocks = 0;

  @Comment(
      value =
          "If true, players are allowed to use spawn eggs in their claimed chunks "
              + "at a credits price.\n"
              + "Else if false, spawn egg usage is not allowed anywhere in "
              + "chunk claim protected worlds.")
  protected boolean mobsForCredits = true;

  @Comment(value = "Credit price to use spawn eggs. A price of zero is valid.")
  protected double mobPrice = 0;

  @Comment(
      value =
          "If true, players are required to claim chunks next to their existing claims.\n"
              + "Else if false, players can claim chunks wherever they want.")
  protected boolean nextToForce = false;

  @Comment(
      value =
          "If true, players are not allowed to access containers unless they are trusted in "
              + "the claim that the container resides in.\n"
              + "Else if false, players are allowed to access any container in "
              + "any chunk regardless of permissions.\n"
              + "A container is any block with usable storage slots.")
  protected boolean protectContainers = true;

  @Comment(
      value =
          "If true, players are not allowed to use redstone switches (levers or buttons) "
              + "unless they are trusted in the claim that the switch resides in.\n"
              + "Else if false, players are allowed to use any switch in "
              + "any chunk regardless of permissions.")
  protected boolean protectSwitches = true;

  @Comment(
      value =
          "If true, chunks are regenerated to their naturally generated state when abandoned or "
              + "deleted.\n"
              + "Else if false, any modifications are kept when chunks are abandoned or deleted.")
  protected boolean regenerateChunk = true;

  @Comment(
      value =
          "The amount of credits a player is given when they join the server for the first time.")
  protected double startCredits = 1;

  /** @return List of world names specified to be managed by ChunkClaim. */
  public List<String> getWorlds() {
    return worlds;
  }

  /** @return Price to claim chunks. */
  public double getChunkPrice() {
    return chunkPrice;
  }

  /**
   * @return {@code true} if containers (any block usable storage slots) are protected, else false.
   */
  public boolean getProtectContainers() {
    return protectContainers;
  }

  /** @return {@code true} if , else false. */
  public boolean getProtectSwitches() {
    return protectSwitches;
  }

  /** @return {@code true} if , else false. */
  public boolean getMobsForCredits() {
    return mobsForCredits;
  }

  /** @return Price to spawn mobs using eggs. */
  public double getMobPrice() {
    return mobPrice;
  }

  /** @return {@code true} if the mob price is zero, else {@code false}. */
  public boolean areMobsFree() {
    return 0 == Double.compare(this.getMobPrice(), 0);
  }

  /** @return Number of credits given to players per hour. */
  public double getCreditsPerHour() {
    return creditsPerHour;
  }

  /** @return Upper bound on how many credits a player can accrue. */
  public double getMaxCredits() {
    return maxCredits;
  }

  /** @return Amount of gets a new player should start with. */
  public double getStartCredits() {
    return startCredits;
  }

  /** @return Minimum number of modification blocks. */
  public int getMinModBlocks() {
    return minModBlocks;
  }

  /** @return Number of days before claims are auto-deleted. */
  public double getAutoDeleteDays() {
    return autoDeleteDays;
  }

  /** @return Milliseconds equivalent of AutoDeleteDays. */
  public double getAutoDeleteMillis() {
    Double autoDeleteDays = this.getAutoDeleteDays();
    if (autoDeleteDays.equals((double) 0)) {
      return 0;
    } else {
      return autoDeleteDays * (24 * 60 * 60 * 1000);
    }
  }

  /** @return {@code true} if auto delete days is set to zero, else {@code false}. */
  public boolean isAutoDeleteDisabled() {
    return 0 == this.getAutoDeleteDays();
  }

  /** @return {@code true} if players are forced to claim next to existing chunks, else false. */
  public boolean getNextToForce() {
    return nextToForce;
  }

  /** @return {@code true} if chunks are regenerated when abandoned/deleted, else false. */
  public boolean getRegenerateChunk() {
    return regenerateChunk;
  }
}
