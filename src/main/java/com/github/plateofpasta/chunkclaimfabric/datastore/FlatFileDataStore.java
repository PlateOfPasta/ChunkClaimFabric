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

package com.github.plateofpasta.chunkclaimfabric.datastore;

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.chunkclaimfabric.world.ChunkWorld;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/** Implements parent interface as a flat file store. */
public class FlatFileDataStore extends DataStore {
  private static final String PLAYER_DATA_FOLDER_PATH =
      DATA_LAYER_FOLDER_PATH + File.separator + "PlayerData";
  private static final String WORLD_DATA_FOLDER_PATH =
      DATA_LAYER_FOLDER_PATH + File.separator + "ChunkData";
  private static final String FILE_EXTENSION = ".json";

  /**
   * Runs the datastore initializer.
   *
   * @throws Exception Something went wrong during initialization of the datastore.
   */
  public FlatFileDataStore() throws Exception {
    this.initialize();
  }

  /**
   * Abstracts how the string filename is formed for a chunk.
   *
   * @param chunk Chunk to form the file name from.
   * @return String name.
   */
  private static String formChunkFileName(Chunk chunk) {
    return chunk.getCoordX() + "_" + chunk.getCoordZ() + FILE_EXTENSION;
  }

  /**
   * Abstracts how a chunk data file formed.
   *
   * @param fileName Filename to parse as chunk data file.
   * @return Array of strings containing chunk data file name components.
   */
  private static String[] parseChunkFileName(String fileName) {
    // todo Ugh, please refactor this at some point. You'll probably have to refactor to a db.
    return fileName.split("_");
  }

  /**
   * Determines if the file is a chunk data file (specifically by name).
   *
   * @param file File to check.
   * @return true if the file is (probably) a chunk data file, else false.
   */
  private static boolean isChunkFile(File file) {
    String fileName = file.getName();
    return !(fileName.equals(parseChunkFileName(fileName)[0]));
  }

  /**
   * Abstracts how the chunk data folder path name is formed.
   *
   * @param worldName Worldname to form the path.
   * @return String name.
   */
  private static String formChunkDataFolderPath(String worldName) {
    return ChunkClaimFabric.getPlugin().getServer().getRunDirectory().toString()
        + File.separator
        + WORLD_DATA_FOLDER_PATH
        + File.separator
        + (worldName.replaceAll("[\\\\\\/\\*\\?\\\"\\<\\>\\|\\:]", "-"));
  }

  /**
   * Forms the full path for a chunk's data file. Handles invalid file characters.
   *
   * @param chunk Chunk to get the path of.
   * @return String path.
   */
  private static String formFullChunkDataFilePath(Chunk chunk) {
    return (formChunkDataFolderPath(chunk.getWorldName())
        + File.separator
        + formChunkFileName(chunk));
  }

  /**
   * Forms the full path for a player's data file.
   *
   * @param playerName Name of the player.
   * @return String filepath.
   */
  private static String formFullPlayerDataFilePath(String playerName) {
    return (PLAYER_DATA_FOLDER_PATH + File.separator + playerName + FILE_EXTENSION);
  }

  /**
   * Helper for creating the parent directories of the filepath. In other words, for filepath
   * "foo/blue/two/file", the directories "foo/blue/two" will be made.
   *
   * @param filepath Filepath containing parent directories
   * @return true if making the directories was completely successful, otherwise false if complete
   *     failure or partial success.
   */
  private static boolean makeParentDirs(String filepath) {
    return new File(filepath).getParentFile().mkdirs();
  }

  /**
   * PlayerData datastore reader. Reads only non-transient data.
   *
   * @return Chunk read from storage.
   */
  private synchronized PlayerData readPlayerDataFromStorage(String filePath) throws IOException {
    try (FileReader fileReader = new FileReader(filePath)) {
      return readData(fileReader, PlayerData.class);
    }
  }

  /**
   * Initialize the file datastore.
   *
   * @throws Exception
   */
  @Override
  void initialize() throws Exception {

    // Ensure data folders exist.
    new File(PLAYER_DATA_FOLDER_PATH).mkdirs();
    new File(WORLD_DATA_FOLDER_PATH).mkdirs();

    // Load worlds.
    for (String worldName : ChunkClaimFabric.getClaimConfig().getWorlds()) {
      if (null != ChunkClaimFabric.getPlugin().getServer().getWorld(worldName)) {
        this.loadWorldData(worldName);
      }
    }
    super.initialize();
  }

  /**
   * Gets the primary key as a string for chunk data.
   *
   * @param chunk Chunk to get the primary key of.
   * @return String representing the chunk's primary key in the datastore.
   */
  @Override
  public String getChunkPrimaryKey(Chunk chunk) {
    return null;
  }

  /**
   * Gets the primary key as a string for player data.
   *
   * @param playerName Player name to get the key of.
   * @return String representing the player data's primary key in the datastore.
   */
  @Override
  public String getPlayerDataPrimaryKey(String playerName) {
    return null;
  }

  /**
   * Loads the world data by loading all chunks from their filestore into memory.
   *
   * @param worldName World to load.
   */
  @Override
  public synchronized void loadWorldData(String worldName) {
    // Create a new world object and register it.
    this.worlds.put(worldName, new ChunkWorld(worldName));

    // Load chunks data into memory.
    // Get a list of all the chunks in the world folder.
    File chunkDataFolder = new File(formChunkDataFolderPath(worldName));
    // Ensure data folder exist.
    chunkDataFolder.mkdirs();

    File[] files = chunkDataFolder.listFiles();
    if (null == files) {
      return;
    }

    for (File file : files) {
      // Avoids folders.
      if (file.isFile()) {
        if (!isChunkFile(file)) {
          continue;
        }

        Chunk chunk = null;
        try {
          chunk = readChunkFromStorage(file.getAbsolutePath());
        } catch (IOException e) {
          ChunkClaimFabric.logInfo(
              "IOException saving data for chunk at path: "
                  + new File(chunkDataFolder, file.getName()).toString()
                  + System.lineSeparator()
                  + "Error: "
                  + e.getMessage());
        }

        // Continue if we've failed to load the chunk data.
        if (null == chunk) {
          continue;
        }

        chunk.setModifiedDate(new Date(file.lastModified()));

        // todo add helper for this remaining block?
        this.chunks.add(chunk);
        if (!chunk.hasMetMinimum()) {
          this.unusedChunks.add(chunk);
        }

        this.worlds.get(chunk.getWorldName()).addChunk(chunk);
        chunk.setInDataStore(true);
      }
    }
  }

  /**
   * Chunk datastore writer. Writes only the Chunk's non-transient data.
   *
   * @param chunk Chunk to write to the datastore.
   */
  public synchronized void writeChunkToStorage(Chunk chunk) {
    String chunkDataFilePath = formFullChunkDataFilePath(chunk);
    // Ensure that the world folder exists.
    makeParentDirs(chunkDataFilePath);

    // Open the chunk's file for overwriting.
    File chunkFile = new File(chunkDataFilePath);

    try (FileWriter writer = new FileWriter(chunkFile)) {
      // Write chunk to the file.
      this.writeData(chunk, writer);
    } catch (IOException e) {
      ChunkClaimFabric.logInfo(
          "IOException when saving data for chunk at path: "
              + formFullChunkDataFilePath(chunk)
              + System.lineSeparator()
              + "Error: "
              + e.getMessage());
    } finally {
      // Update date.
      chunk.setModifiedDate(new Date(chunkFile.lastModified()));
    }
  }

  /**
   * Chunk datastore reader. Reads only the Chunk's non-transient data.
   *
   * @return Chunk read from storage.
   */
  synchronized Chunk readChunkFromStorage(String filePath) throws IOException {
    try (FileReader fileReader = new FileReader(filePath)) {
      return readData(fileReader, Chunk.class);
    }
  }

  /**
   * Deletes the chunk data from the file store on disk.
   *
   * @param chunk Chunk to delete.
   * @return
   */
  @Override
  boolean deleteChunkFromSecondaryStorage(Chunk chunk) {
    String fullPathName = formFullChunkDataFilePath(chunk);
    // remove from disk
    File chunkFile = new File(fullPathName);

    if (chunkFile.exists() && !chunkFile.delete()) {
      ChunkClaimFabric.logInfo("Error: Unable to delete chunk file at path" + fullPathName);
      return false;
    }
    return true;
  }

  /**
   * Satisfies parent interface for flat file datastore.
   *
   * @param playerName Name of the player whose data we're reading.
   * @return PlayerData object filled with the non-transient data in their file.
   */
  @Override
  synchronized PlayerData getPlayerDataFromStorage(String playerName) {
    File file = new File(formFullPlayerDataFilePath(playerName));

    PlayerData playerData = new PlayerData();
    playerData.setPlayerName(playerName);

    if (!file.exists()) {
      // Create a file with defaults if it doesn't exist.
      this.savePlayerData(playerName, playerData);
    } else {
      try {
        playerData = readPlayerDataFromStorage(file.getAbsolutePath());
      } catch (IOException e) {
        ChunkClaimFabric.logInfo(
            "IOException saving player data at path: "
                + file.toString()
                + System.lineSeparator()
                + "Error: "
                + e.getMessage());
      }
    }
    return playerData;
  }

  /**
   * Satisfies parent interface for flat file datastore.
   *
   * @param playerName Player name to store.
   * @param playerData PlayerData object to store.
   */
  @Override
  public synchronized void savePlayerData(String playerName, PlayerData playerData) {
    playerData.setPlayerName(playerName); // This might be redundant.
    writePlayerDataToStorage(playerData);
  }

  /**
   * PlayerData datastore writer. Writes only the non-transient data.
   *
   * @param playerData PlayerData to write to the datastore.
   */
  private synchronized void writePlayerDataToStorage(PlayerData playerData) {
    String playerFullDataFilePath = formFullPlayerDataFilePath(playerData.getPlayerName());
    makeParentDirs(playerFullDataFilePath);

    // Open the file for overwriting.
    File playerFile = new File(playerFullDataFilePath);

    try (FileWriter writer = new FileWriter(playerFile)) {
      // Write to the file.
      this.writeData(playerData, writer);
    } catch (IOException e) {
      ChunkClaimFabric.logInfo(
          "IOException when saving player data at path: "
              + playerFullDataFilePath
              + System.lineSeparator()
              + "Error: "
              + e.getMessage());
    }
  }

  /**
   * Implements the parent interface. Does nothing since files are opened/closed on the fly for the
   * FlatFileDataStore.
   */
  @Override
  public synchronized void close() {}

  /**
   * Handles writing data to a file.
   *
   * @param data Data to write.
   * @param writer FileWriter to write with.
   * @param <T> Type of data.
   * @throws IOException There was a problem with the file writer.
   */
  private synchronized <T> void writeData(T data, FileWriter writer) throws IOException {
    Gson gson = new Gson();
    try {
      gson.toJson(data, writer);
    } catch (JsonIOException e) {
      throw new IOException(e);
    }
  }

  /**
   * Handles reading data from a file.
   *
   * @param reader FileReader to read with.
   * @param classOfT Class of data (i.e. from `MyClass.class`)
   * @param <T> Type of data.
   * @return Data read from file.
   * @throws IOException There was a problem with the file reader, related to either file I/O or
   *     parsing.
   */
  private synchronized <T> T readData(FileReader reader, Class<T> classOfT) throws IOException {
    Gson gson = new Gson();
    try {
      return gson.fromJson(reader, classOfT);
    } catch (JsonSyntaxException | JsonIOException e) {
      throw new IOException(e);
    }
  }
}
