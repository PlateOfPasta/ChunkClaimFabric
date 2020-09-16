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

package com.github.plateofpasta.chunkclaimfabric.handler;

import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimTags;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.util.ChunkClaimUtil;
import com.github.plateofpasta.edgestitch.event.ServerPlayerEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;

import java.util.Date;

/** Handler for player events. */
public class PlayerEventHandler {
  private final DataStore dataStore;

  /**
   * The handler requires a reference to the datastore.
   *
   * @param dataStore Chunk claim datastore of the plugin's server.
   */
  public PlayerEventHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  /**
   * Bootstrap static method for initializing the callbacks for events.
   *
   * @param dataStore Chunk claim datastore of the plugin's server.
   */
  public static void initHandlers(DataStore dataStore) {
    PlayerEventHandler handler = new PlayerEventHandler(dataStore);
    ServerPlayerEvents.PLAYER_CONNECT.register(handler::onPlayerJoin);
    ServerPlayerEvents.PLAYER_DISCONNECT.register(handler::onPlayerQuit);
    ServerPlayerEvents.PLAYER_DROP_ITEM.register(handler::onPlayerDrop);
  }

  /**
   * When a player successfully joins the server.
   *
   * @param playerName Player.
   */
  private void onPlayerJoin(String playerName) {
    PlayerData playerData = this.dataStore.getPlayerData(playerName);
    playerData.setLastLogin(new Date());
    if (null == playerData.getFirstJoin()) {
      playerData.setFirstJoin(new Date());
    }
    this.dataStore.savePlayerData(playerName, playerData);
  }

  /**
   * When a player quits.
   *
   * @param playerName Player.
   */
  private void onPlayerQuit(String playerName) {
    PlayerData playerData = this.dataStore.getPlayerData(playerName);
    // Make sure his data is all saved.
    this.dataStore.savePlayerData(playerName, playerData);
    // Drop data about this player.
    this.dataStore.clearCachedPlayerData(playerName);
  }

  /**
   * When a player drops an item.
   *
   * @param playerEntity Player dropping the item.
   * @param stack Item being dropped.
   * @return Typed action result PASS if the item is allowed to be dropped, else FAIL with a null
   *     value which should not spawn the item entity in the world.
   */
  private TypedActionResult<ItemEntity> onPlayerDrop(PlayerEntity playerEntity, ItemStack stack) {
    if (!ChunkClaimUtil.isConfiguredWorld(playerEntity.getEntityWorld())) {
      return TypedActionResult.pass(null);
    }

    // allow dropping books
    if (!ChunkClaimTags.ALLOWED_DROPPABLE_ITEMS.contains(stack.getItem())) {
      // This will return a null ItemEntity to the hooked function and should prevent the item
      // entity from spawning in the world.
      return TypedActionResult.fail(null);
    } else {
      return TypedActionResult.pass(null);
    }
  }
}
