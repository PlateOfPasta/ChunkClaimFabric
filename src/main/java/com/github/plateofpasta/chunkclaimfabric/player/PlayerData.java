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

package com.github.plateofpasta.chunkclaimfabric.player;

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.util.TimeDateUtil;
import com.github.plateofpasta.chunkclaimfabric.visual.Visualization;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Player metadata related to ChunkClaim. */
public class PlayerData {
  public transient Chunk lastChunk = null;
  private double credits = ChunkClaimFabric.getClaimConfig().getStartCredits();
  private float bonus = 0L;
  private String playerName;
  private ArrayList<String> builderNames = new ArrayList<String>();
  private Date lastLogin = new Date();
  private Date firstJoin = new Date();
  private transient Visualization currentVisualization = null;
  private transient EdgestitchLocation lastAfkCheckLocation = null;
  private transient boolean ignoreChunks = false;

  /**
   * Gets the amount of credits allocated to a player.
   *
   * @return Number of starting credits.
   */
  public double getCredits() {
    return this.credits;
  }

  /**
   * Sets the credits value.
   *
   * @param credits Value to set.
   */
  public void setCredits(double credits) {
    this.credits = credits;
  }

  /**
   * Adds credits to the credit counter.
   *
   * @param credits Credits to add.
   */
  public void addCredits(double credits) {
    this.credits += credits;
  }

  /**
   * Removes credits to the credit counter.
   *
   * @param credits Credits to remove.
   */
  public void removeCredits(double credits) {
    this.credits -= credits;
  }

  public float getBonus() {
    return bonus;
  }

  /** @param bonus New bonus value. */
  public void setBonus(float bonus) {
    this.bonus = bonus;
  }

  /** @param bonus Bonus to add. */
  public void addBonus(float bonus) {
    this.bonus += bonus;
  }

  /** @return Player name. */
  public String getPlayerName() {
    return playerName;
  }

  /** @param playerName New player name. */
  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  /** @return List of builder names. */
  public List<String> getBuilderNames() {
    return builderNames;
  }

  /** @param builderNames New list of builder names. */
  public void setBuilderNames(List<String> builderNames) {
    this.builderNames = new ArrayList<>(builderNames);
  }

  /**
   * @param name Name to add.
   * @return true if added, else false.
   */
  public boolean addBuilderName(String name) {
    return builderNames.add(name);
  }

  /**
   * @param name Name to remove.
   * @return true if added, else false.
   */
  public boolean removeBuilderName(String name) {
    return builderNames.remove(name);
  }

  /** @return Date the player last logged in. */
  public Date getLastLogin() {
    return this.lastLogin;
  }

  /** @param lastLogin New last login date. */
  public void setLastLogin(Date lastLogin) {
    if (null == lastLogin) {
      lastLogin = new Date();
    }
    this.lastLogin = lastLogin;
  }

  /**
   * Gets the last login time (relative to now) in days.
   *
   * @return Days since last login.
   */
  public long getLastLoginInDays() {
    return TimeDateUtil.differenceInDays(new Date(), this.getLastLogin());
  }

  /** @return Date the player first joined. */
  public Date getFirstJoin() {
    return firstJoin;
  }

  /** @param firstJoin New first join date. */
  public void setFirstJoin(Date firstJoin) {
    if (null == firstJoin) {
      firstJoin = new Date();
    }
    this.firstJoin = firstJoin;
  }

  /**
   * Gets the first join time (relative to now) in days.
   *
   * @return Days since first join.
   */
  public long getFirstJoinInDays() {
    return TimeDateUtil.differenceInDays(new Date(), this.getFirstJoin());
  }

  /** @return Gets the last chunk the player claimed. */
  public Chunk getLastChunk() {
    return lastChunk;
  }

  /** @param lastChunk The newest chunk the player claimed. */
  public void setLastChunk(Chunk lastChunk) {
    this.lastChunk = lastChunk;
  }

  /** @return Gets the current claim visualization of the player. */
  public Visualization getCurrentVisualization() {
    return currentVisualization;
  }

  /** @param currentVisualization New claim visualization of the player. */
  public void setCurrentVisualization(Visualization currentVisualization) {
    this.currentVisualization = currentVisualization;
  }

  /** @return Location the player was at during the last AFK check. */
  public EdgestitchLocation getLastAfkCheckLocation() {
    return lastAfkCheckLocation;
  }

  /** @param lastAfkCheckLocation New location for AFK check. */
  public void setLastAfkCheckLocation(EdgestitchLocation lastAfkCheckLocation) {
    this.lastAfkCheckLocation = lastAfkCheckLocation;
  }

  /** @return Whether or not the player can ignore chunk claims. */
  public boolean canIgnoreChunkClaims() {
    return ignoreChunks;
  }

  /** @param ignoreChunks New state of the ignore chunks member. */
  public void setIgnoreChunks(boolean ignoreChunks) {
    this.ignoreChunks = ignoreChunks;
  }

  /**
   * Toggles the value of ignoreChunks.
   *
   * @return New value of ignoreChunks.
   */
  public boolean toggleIgnoreChunks() {
    this.ignoreChunks = !(this.ignoreChunks);
    return this.ignoreChunks;
  }

  /** @return {@code true} if the player can afford to claim a chunk, else {@code false}. */
  public boolean canAffordClaim() {
    return 0
        <= Double.compare(this.getCredits(), ChunkClaimFabric.getClaimConfig().getChunkPrice());
  }

  /** @return {@code true} if the player can afford to claim a chunk, else {@code false}. */
  public boolean canAffordMob() {
    return 0 <= Double.compare(this.getCredits(), ChunkClaimFabric.getClaimConfig().getMobPrice());
  }
}
