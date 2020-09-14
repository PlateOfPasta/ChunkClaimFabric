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

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Class for representing a ChunkClaim chunk. */
public class Chunk {
  private String ownerName;
  private String worldName;
  private int modifiedBlocks = 0;
  private ArrayList<String> builderNames = new ArrayList<String>();
  private Date modifiedDate;
  private Date claimDate;
  private boolean markedForDelete = false;
  private transient boolean inDataStore;
  private ChunkPos chunkPos;
  private transient boolean marked = false;
  private transient boolean inspected = false;

  /**
   * Base constructor for a Chunk. Only initializes the members to the given parameters. Does not
   * initialize claim date.
   *
   * @param chunkPos Chunk position.
   * @param worldName World name of the chunk.
   */
  public Chunk(ChunkPos chunkPos, String worldName) {
    this.chunkPos = chunkPos;
    this.worldName = worldName;
  }

  /**
   * Constructor for a Chunk. Only initializes the members to the given parameters. Does not
   * initialize claim date.
   *
   * @param px X-coordinate of a block in the chunk in the game world. Not a chunk coordinate.
   * @param pz Z-coordinate of a block in the chunk in the game world. Not a chunk coordinate.
   * @param worldName World name of the chunk.
   */
  public Chunk(int px, int pz, String worldName) {
    this(new ChunkPos(new BlockPos(px, 0, pz)), worldName); // Delegate construction.
  }

  /**
   * Constructor for a Chunk. Initializes the appropriate members using the parameters and claimDate
   * with `new Date()`. Note: providing an ownerName is the requirement for initializing the claim
   * date.
   *
   * @param px X-coordinate of a block in the chunk in the game world. Not a chunk coordinate.
   * @param pz Z-coordinate of a block in the chunk in the game world. Not a chunk coordinate.
   * @param worldName World name of the chunk.
   * @param ownerName Player name who owns this chunk.
   */
  public Chunk(int px, int pz, String worldName, String ownerName) {
    this(px, pz, worldName); // Delegate construction.
    this.ownerName = ownerName;
    this.claimDate = new Date();
  }

  /**
   * Makes a Chunk using basic information that can be acquired from the {@link
   * com.github.plateofpasta.edgestitch.world.EdgestitchLocation}.
   *
   * @param location Location inside the chunk.
   */
  public Chunk(EdgestitchLocation location) {
    this(location.getX(), location.getZ(), location.getWorld().getName());
  }

  /**
   * Constructor for a Chunk.
   *
   * @param location Location in the game world inside the chunk.
   * @param ownerName Player name who owns this chunk.
   */
  public Chunk(EdgestitchLocation location, String ownerName) {
    this(location.getX(), location.getZ(), location.getWorld().getName(), ownerName);
  }

  /**
   * Constructor for a Chunk.
   *
   * @param location Location in the game world inside the chunk.
   * @param ownerName Player name who owns this chunk.
   * @param builderNames Player names who are able to build in this chunk.
   */
  public Chunk(EdgestitchLocation location, String ownerName, List<String> builderNames) {
    this(location, ownerName); // Delegate construction.
    this.builderNames.addAll(builderNames);
  }

  /**
   * Constructor for a Chunk.
   *
   * @param px X-coordinate of a block in the chunk in the game world. Not a chunk coordinate.
   * @param pz Z-coordinate of a block in the chunk in the game world. Not a chunk coordinate.
   * @param worldName World name of the chunk.
   * @param ownerName Player name who owns this chunk.
   * @param builderNames Player names who are able to build in this chunk.
   */
  public Chunk(
      int px, int pz, String worldName, String ownerName, Date claimDate, String[] builderNames) {
    this(px, pz, worldName, ownerName); // Delegate construction.
    for (String s : builderNames) {
      if (!s.isEmpty()) {
        this.builderNames.add(s);
      }
    }
  }

  /**
   * Constructor for a Chunk.
   *
   * @param px px X-coordinate of a block in the chunk in the game world. Not a chunk coordinate.
   * @param pz pz Z-coordinate of a block in the chunk in the game world. Not a chunk coordinate.
   * @param worldName World name of the chunk.
   * @param ownerName Player name who owns this chunk.
   * @param builderNames Player names who are able to build in this chunk.
   */
  public Chunk(int px, int pz, String worldName, String ownerName, String[] builderNames) {
    // Delegate construction.
    this(px, pz, worldName, ownerName, new Date(), builderNames);
  }

  /**
   * Getter for the chunk's owner name.
   *
   * @return Owner name.
   */
  public String getOwnerName() {
    return this.ownerName;
  }

  /**
   * Sets owner name.
   *
   * @param ownerName New owner name.
   */
  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  /**
   * Getter for the chunk's world name.
   *
   * @return World name.
   */
  public String getWorldName() {
    return this.worldName;
  }

  /**
   * Getter for number of modified blocks.
   *
   * @return Number of modified blocks.
   */
  public int getModifiedBlocks() {
    return this.modifiedBlocks;
  }

  /**
   * @return {@code true} if the owner of this chunk has modified the minimum amount of blocks
   *     required for this chunk to be considered "built," else {@code false}.
   */
  public boolean hasMetMinimum() {
    return this.getModifiedBlocks() >= ChunkClaimFabric.getClaimConfig().getMinModBlocks();
  }

  /**
   * Getter for list of builder names.
   *
   * @return Number of modified blocks.
   */
  public List<String> getBuilderNames() {
    return this.builderNames;
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

  /**
   * Forms a delimited string of the trusted builder names.
   *
   * @param delim Delimiter to join the names with.
   * @return Delimited string of builder names.
   */
  public String getBuilderNamesString(String delim) {
    return String.join(delim, this.getBuilderNames());
  }

  /**
   * Getter for the modified date.
   *
   * @return Modified date.
   */
  public Date getModifiedDate() {
    return this.modifiedDate;
  }

  /**
   * Setter for the modified date.
   *
   * @param d New modified date.
   */
  public void setModifiedDate(Date d) {
    this.modifiedDate = d;
  }

  /**
   * Getter for the claim date.
   *
   * @return Claim date.
   */
  public Date getClaimDate() {
    return this.claimDate;
  }

  /**
   * Checks if this chunk should be in the datastore.
   *
   * @return true if the chunk should be in the datastore, else false.
   */
  public boolean isInDataStore() {
    return this.inDataStore;
  }

  /** Sets the boolean that determines if this chunk should be in the datastore. */
  public void setInDataStore(boolean b) {
    this.inDataStore = b;
  }

  /**
   * Gets the chunk X-coordinate of this chunk.
   *
   * @return Chunk X-coordinate.
   */
  public int getCoordX() {
    return chunkPos.x;
  }

  /**
   * Gets the starting X-coordinate of this chunk. This is a normal coordinate.
   *
   * @return Starting X-coordinate.
   */
  public int getStartX() {
    return chunkPos.getStartX();
  }

  /**
   * Gets the ending X-coordinate of this chunk. This is a normal coordinate.
   *
   * @return Ending X-coordinate.
   */
  public int getEndX() {
    return chunkPos.getEndX();
  }

  /**
   * Gets the Z-coordinate of this chunk.
   *
   * @return Chunk Z-coordinate.
   */
  public int getCoordZ() {
    return chunkPos.z;
  }

  /**
   * Gets the starting Z-coordinate of this chunk. This is a normal coordinate.
   *
   * @return Starting Z-coordinate.
   */
  public int getStartZ() {
    return chunkPos.getStartZ();
  }

  /**
   * Gets the ending Z-coordinate of this chunk. This is a normal coordinate.
   *
   * @return Ending Z-coordinate.
   */
  public int getEndZ() {
    return chunkPos.getEndZ();
  }

  /** @return Position of this chunk. */
  public ChunkPos getChunkPos() {
    return this.chunkPos;
  }

  /** @return string with the Chunk X and Z coordinates formatted. */
  public String getChunkCoordString() {
    return String.format("(%d, %d", this.getCoordX(), this.getCoordZ());
  }

  /**
   * Checks if marked is set.
   *
   * @return true if marked is set, else false.
   */
  public boolean isMarked() {
    return marked;
  }

  /**
   * Checks if this chunk is inspected
   *
   * @return true if inspected is set, else false.
   */
  public boolean isInspected() {
    return inspected;
  }

  /**
   * Sets the inspected value of this chunk.
   *
   * @param b Boolean to set inspected to.
   */
  public void setInspected(boolean b) {
    this.inspected = b;
  }

  /**
   * Check if the location is contained within this chunk.
   *
   * @param location Location to check.
   * @return True if contained, else false.
   */
  public boolean contains(EdgestitchLocation location) {
    int locationX = location.getChunkX();
    int locationZ = location.getChunkZ();
    String locationWorldName = location.getWorld().getName();

    return (locationX == this.getCoordX())
        && (locationZ == this.getCoordZ())
        && (locationWorldName.equals(this.worldName));
  }

  /**
   * Modify (increment by one) the chunk's number of modified blocks so we can determine if this
   * chunk is "built." Note: this function performs datastore operations until the modification
   * minimum has been met.
   */
  public void modify() {
    if (!this.hasMetMinimum()) {
      this.modifiedBlocks++;
      ChunkClaimFabric.getPlugin().writeChunkToStorage(this);
    }
  }

  /** Marks the chunk for deletion. */
  public void markForDelete() {
    this.markedForDelete = true;
    ChunkClaimFabric.getPlugin().writeChunkToStorage(this);
  }

  /** Unmarks the chunk for deletion. */
  public void unmarkForDelete() {
    this.markedForDelete = false;
    ChunkClaimFabric.getPlugin().writeChunkToStorage(this);
  }

  /**
   * Checks if the player is capable of modifying in this chunk. Either the owner, a builder, or
   * someone who can ignore chunk policies.
   *
   * @param playerName Player name to check.
   * @return True if trusted, else false.
   */
  public boolean canModify(String playerName) {
    return (this.builderNames.contains(playerName))
        || (this.ownerName.equals(playerName))
        ||
        // todo modify this to be more agnostic of the singleton and public member.
        (ChunkClaimFabric.getPlugin()
            .getDataStore()
            .getPlayerData(playerName)
            .canIgnoreChunkClaims());
  }

  /**
   * Equivalency comparison operation.
   *
   * @param other Other Chunk to compare this object against.
   */
  public boolean equals(Chunk other) {
    return this.getCoordX() == other.getCoordX()
        && this.getCoordZ() == other.getCoordZ()
        && this.getWorldName().equals(other.getWorldName());
  }

  /**
   * @param direction Offset direction. UP or DOWN returns this chunk's position, since chunk
   *     coordinates are 2D.
   * @return Chunk position relative to this in the given direction.
   */
  public ChunkPos offset(Direction direction) {
    BlockPos neighborPos =
        new BlockPos(
            this.chunkPos.getStartX() + (16 * direction.getOffsetX()),
            0,
            this.chunkPos.getEndZ() + (16 * direction.getOffsetZ()));
    return new ChunkPos(neighborPos);
  }
}
