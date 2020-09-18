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

import com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimConfig;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimPrompt;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimTags;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.event.BlockSpreadCallback;
import com.github.plateofpasta.chunkclaimfabric.event.DispenseCallback;
import com.github.plateofpasta.chunkclaimfabric.event.SaplingGrowCallback;
import com.github.plateofpasta.chunkclaimfabric.event.UseBedCallback;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.player.PlayerData;
import com.github.plateofpasta.chunkclaimfabric.util.ChunkClaimUtil;
import com.github.plateofpasta.chunkclaimfabric.util.arborist.GrowthType;
import com.github.plateofpasta.chunkclaimfabric.visual.Visualization;
import com.github.plateofpasta.chunkclaimfabric.visual.VisualizationType;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.event.FluidFlowCallback;
import com.github.plateofpasta.edgestitch.event.HopperInsertCallback;
import com.github.plateofpasta.edgestitch.event.PistonEvents;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.TreeFeature;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Handler for block events. */
public class BlockEventHandler {

  private final DataStore dataStore;

  /**
   * The handler requires a reference to the datastore.
   *
   * @param dataStore Chunk claim datastore of the plugin's server.
   */
  private BlockEventHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  /**
   * Bootstrap static method for initializing the callbacks for events.
   *
   * @param dataStore Chunk claim datastore of the plugin's server.
   */
  public static void initHandlers(DataStore dataStore) {
    BlockEventHandler handler = new BlockEventHandler(dataStore);
    UseBedCallback.EVENT.register(handler::onUseBedBlock);
    AttackBlockCallback.EVENT.register(handler::onBlockBreak);
    UseBlockCallback.EVENT.register(handler::onBlockPlace);
    UseBlockCallback.EVENT.register(handler::onSpawnEggUse);
    UseBlockCallback.EVENT.register(handler::onItemUseBlock);
    BlockSpreadCallback.EVENT.register(handler::onSpreadFromTo);
    FluidFlowCallback.EVENT.register(handler::onFluidFromTo);
    DispenseCallback.EVENT.register(handler::onDispenseFromTo);
    HopperInsertCallback.EVENT.register(handler::onHopperInsert);
    SaplingGrowCallback.EVENT.register(handler::onTreeGrow);
    PistonEvents.PISTON_EXTEND.register(handler::onPistonExtend);
    PistonEvents.PISTON_RETRACT.register(handler::onPistonRetract);
  }

  /**
   * Handler for piston extension.
   *
   * @param world World piston is in.
   * @param pistonPos Piston position in the world.
   * @param facingDir Direction the piston is facing.
   * @param pistonHandler PistonHandler involved in piston movement calculation.
   * @return PASS if movement calculation should proceed, else FAIL if the movement calculation
   *     should return not occur.
   */
  private ActionResult onPistonExtend(
      World world, BlockPos pistonPos, Direction facingDir, PistonHandler pistonHandler) {
    return onPistonMove(world, pistonPos, facingDir, pistonHandler, true);
  }

  /**
   * Handler for piston retraction.
   *
   * @param world World piston is in.
   * @param pistonPos Piston position in the world.
   * @param facingDir Direction the piston is facing.
   * @param pistonHandler PistonHandler involved in piston movement calculation.
   * @return PASS if movement calculation should proceed, else FAIL if the movement calculation
   *     should return not occur.
   */
  private ActionResult onPistonRetract(
      World world, BlockPos pistonPos, Direction facingDir, PistonHandler pistonHandler) {
    return onPistonMove(world, pistonPos, facingDir, pistonHandler, false);
  }

  /**
   * General handler for piston movement (extension or retraction) with regards to chunk claims.
   *
   * @param world World piston is in.
   * @param pistonPos Piston position in the world.
   * @param facingDir Direction the piston is facing.
   * @param pistonHandler PistonHandler involved in piston movement calculation.
   * @param isExtending If the piston is extending (true) or retracting (false).
   * @return PASS if movement calculation should proceed, else FAIL if the movement calculation
   *     should return a "don't move" value because at least one of the movable blocks is in a chunk
   *     claim other than the piston's chunk.
   */
  private ActionResult onPistonMove(
      World world,
      BlockPos pistonPos,
      Direction facingDir,
      PistonHandler pistonHandler,
      boolean isExtending) {
    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }

    final DataStore datastore = ChunkClaimFabric.getPlugin().getDataStore();
    final Chunk pistonChunk = datastore.getChunkAt(new EdgestitchLocation(world, pistonPos), null);
    if (null == pistonChunk) {
      return ActionResult.FAIL;
    }
    if (ChunkClaimPlayer.hasIgnorePermission(pistonChunk.getOwnerName())) {
      return ActionResult.PASS;
    }
    final ChunkPos neighborChunkPos = pistonChunk.offset(facingDir);
    final Chunk neighborChunk =
        datastore.getChunkAtPos(neighborChunkPos.x, neighborChunkPos.z, pistonChunk.getWorldName());
    final boolean cannotModifyNeighbor =
        (null == neighborChunk || !neighborChunk.canModify(pistonChunk.getOwnerName()));
    final Direction motionDir = isExtending ? facingDir : facingDir.getOpposite();

    // Claim conflict exists if the checked block position is in or will be moved to the neighbor
    // chunk AND the owner of the pistonChunk cannot modify other chunks.
    // TODO: This creates a weird scenario when admins are ignoring chunks and trusted builders can
    // use piston blocks in their claims.
    Predicate<BlockPos> isClaimConflict =
        (sourcePos) -> {
          // Where a pushed block starts (source).
          ChunkPos sourceChunkPos = new ChunkPos(sourcePos);
          // Where a pushed block ends up (destination).
          ChunkPos destChunkPos = new ChunkPos(sourcePos.offset(motionDir));
          return ((sourceChunkPos.equals(neighborChunkPos))
                  || (destChunkPos.equals(neighborChunkPos)))
              && cannotModifyNeighbor;
        };

    int numConflicts = 0;
    numConflicts += pistonHandler.getMovedBlocks().stream().filter(isClaimConflict).count();
    numConflicts += pistonHandler.getBrokenBlocks().stream().filter(isClaimConflict).count();
    // Fail operation if ANY of the blocks are owned by someone other than the piston owner.
    if (0 != numConflicts) {
      return ActionResult.FAIL;
    }
    return ActionResult.PASS;
  }

  /**
   * Block players from entering beds they don't have permission for.
   *
   * @param playerEntity Player using (right-clicking) the bed block.
   * @param world World event occurred.
   * @param blockPos Position of the block in the world.
   */
  private ActionResult onUseBedBlock(PlayerEntity playerEntity, World world, BlockPos blockPos) {
    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }
    ChunkClaimPlayer player = new ChunkClaimPlayer(playerEntity);
    Chunk chunk = this.dataStore.getChunkAt(new EdgestitchLocation(world, blockPos), null);

    if (chunk == null) {
      player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.no_permission"));
    } else {
      if (!chunk.canModify(player.getName())) {
        player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.no_permission"));
        return ActionResult.FAIL;
      }
    }

    return ActionResult.PASS;
  }

  /**
   * Functional interface implementation for using buckets on blocks.
   *
   * @param playerEntity Player using the item.
   * @param world World the player is in.
   * @param hand Player hand that the used item is in.
   * @return Typed action result PASS if the player is allowed to use the item at the current
   *     location, else FAIL. Does not modify the ItemStack in the player's hand.
   */
  private ActionResult onItemUseBlock(
      PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
    final ItemStack itemStack = playerEntity.getStackInHand(hand);
    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }

    final Item item = itemStack.getItem();
    if (!ChunkClaimTags.CHECKED_ITEMS.contains(item)) {
      return ActionResult.PASS;
    }

    EdgestitchLocation location = new EdgestitchLocation(world, blockHitResult.getBlockPos());
    Chunk chunk = this.dataStore.getChunkAt(location, null);

    // Usage on unclaimed chunk is invalid.
    if (chunk == null) {
      return ActionResult.FAIL;
    }

    ChunkClaimPlayer player = new ChunkClaimPlayer(playerEntity);
    if (chunk.canModify(player.getName())) {
      return ActionResult.PASS;
    } else {
      player.sendMessage("You don't have " + chunk.getOwnerName() + "'s permission to build here.");
      return ActionResult.FAIL;
    }
  }

  /**
   * Helper for checking chunk data when a player modifies a block near their existing claim. Sends
   * them appropriate messages and visualizations.
   *
   * @param player Player attempting to modifying chunk
   * @param location Location player is trying to modify.
   */
  private void handleOwnsNearBlockModify(ChunkClaimPlayer player, EdgestitchLocation location) {
    if (!ChunkClaimFabric.getClaimConfig().getNextToForce() && !player.hasModPermission()) {
      player.sendMessages(
          ChunkClaimPrompt.get("prompt.chunkclaim.dont_own_next_to"),
          ChunkClaimPrompt.get("prompt.chunkclaim.how_to_confirm"));
    } else {
      List<Chunk> playerChunks = this.dataStore.getAllChunksForPlayer(player.getName());
      if (playerChunks.size() > 0) {
        player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.only_next_to_first"));
      } else {
        player.sendMessages(
            ChunkClaimPrompt.get("prompt.chunkclaim.dont_own_next_to"),
            ChunkClaimPrompt.get("prompt.chunkclaim.how_to_confirm"));
      }
    }

    Visualization.apply(
        player,
        Visualization.fromChunk(
            new Chunk(location), location.getY(), VisualizationType.PUBLIC, location));
  }

  /**
   * Helper for when a player tries to break a block in a non-claimed chunk - the chunk will be
   * automatically claimed without the `/chunk claim` command if claim conditions are met.
   *
   * @param player Player that is trying to (maybe) claim the chunk.
   * @param location Location of the block being broken in a chunk.
   * @param playerData Chunk claim player data.
   */
  private void tryClaim(
      ChunkClaimPlayer player, EdgestitchLocation location, PlayerData playerData) {
    // If no one has claimed the chunk, try to let the player claim it.
    String playerName = player.getName();

    if (!player.hasClaimChunkPermission()) {
      player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.no_claim_permission"));
      Visualization.apply(
          player,
          Visualization.fromChunk(
              new Chunk(location), location.getY(), VisualizationType.ERROR_CHUNK, location));
      return;
    }

    if (!dataStore.ownsNear(location, playerName)) {
      handleOwnsNearBlockModify(player, location);
    } else if (playerData.canAffordClaim()) {
      // Claim chunk and add it to the datastore.
      Chunk newChunk = new Chunk(location, playerName, playerData.getBuilderNames());
      this.dataStore.claimChunk(playerName, newChunk);
      playerData.setLastChunk(newChunk);

      // Send success prompt and visualization to player.
      player.sendMessage(ChunkClaimPrompt.justClaimedChunk(playerData.getCredits()));
      Visualization.apply(
          player,
          Visualization.fromChunk(newChunk, location.getY(), VisualizationType.CHUNK, location));
    } else {
      player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.not_enough_credits_claim"));
      if (playerData.getLastChunk() != null) {
        playerData.setLastChunk(null);
        Visualization.apply(
            player,
            Visualization.fromChunk(
                new Chunk(location), location.getY(), VisualizationType.PUBLIC, location));
      }
    }
  }

  /**
   * When a player breaks a block.
   *
   * @param playerEntity Player breaking the block.
   * @param world World the player is in.
   * @param hand Player hand performing the action.
   * @param blockPos Block position in the world.
   * @param direction Which face of the block is being attacked (broken).
   * @return PASS if the action is allowed, else FAIL.
   */
  private ActionResult onBlockBreak(
      PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction) {
    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }
    final ChunkClaimPlayer player = new ChunkClaimPlayer(playerEntity);
    final EdgestitchLocation location = new EdgestitchLocation(world, blockPos);
    final PlayerData playerData = this.dataStore.getPlayerData(player.getName());
    final Chunk chunk = dataStore.getChunkAt(location, playerData.getLastChunk());

    if (playerData.canIgnoreChunkClaims()) {
      return ActionResult.PASS;
    }

    if (chunk == null) {
      // Try claim, but the action should always cancel the block break.
      tryClaim(player, location, playerData);
    } else if (chunk.canModify(player.getName())) {
      return ActionResult.PASS;
    } else {
      player.sendMessage(ChunkClaimPrompt.noBuildPermissionFrom(chunk.getOwnerName()));
      if (playerData.getLastChunk() != chunk) {
        playerData.setLastChunk(chunk);
        Visualization.apply(
            player,
            Visualization.fromChunk(
                chunk, location.getY(), VisualizationType.ERROR_CHUNK, location));
      }
    }
    return ActionResult.FAIL;
  }

  /**
   * When a player places a block.
   *
   * @param playerEntity Player placing the block.
   * @param world World the player is in.
   * @param hand Player hand performing the action.
   * @param hitResult Hit result of the action.
   * @return PASS if the action is allowed, else FAIL.
   */
  private ActionResult onBlockPlace(
      PlayerEntity playerEntity, World world, Hand hand, BlockHitResult hitResult) {
    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }
    final ChunkClaimPlayer player = new ChunkClaimPlayer(playerEntity);
    final EdgestitchLocation location = new EdgestitchLocation(world, hitResult.getBlockPos());
    final PlayerData playerData = this.dataStore.getPlayerData(player.getName());
    final Chunk chunk = dataStore.getChunkAt(location, playerData.getLastChunk());

    if (playerData.canIgnoreChunkClaims()) {
      return ActionResult.PASS;
    }

    // Interaction that will consume the player's action. For example, pressing buttons/levers or
    // opening blocks with inventories.
    boolean probablyConsumableInteraction =
        !(playerEntity.shouldCancelInteraction()
            && (!playerEntity.getMainHandStack().isEmpty()
                || !playerEntity.getOffHandStack().isEmpty()));
    if (chunk == null) {
      return ActionResult.FAIL;
    } else if (chunk.canModify(player.getName())) {
      // We want the modify counter to increase when players place blocks, not when they right
      // click blocks.
      if (!probablyConsumableInteraction) {
        chunk.modify();
      }
      return ActionResult.PASS;
    } else {
      if (probablyConsumableInteraction) {
        ChunkClaimConfig config = ChunkClaimFabric.getClaimConfig();
        if (!config.getProtectSwitches()) {
          if (ChunkClaimTags.PROTECTED_SWITCHES.contains(location.getBlockState().getBlock())) {
            return ActionResult.PASS;
          }
        }
        if (!config.getProtectContainers()) {
          BlockEntity blockEntity = location.getWorld().getBlockEntity(location.getBlockPos());
          if (blockEntity != null
              && (blockEntity instanceof LockableContainerBlockEntity
                  || blockEntity.getCachedState().getBlock() instanceof AbstractChestBlock)) {
            return ActionResult.PASS;
          }
        }
      }
      player.sendMessage(ChunkClaimPrompt.noBuildPermissionFrom(chunk.getOwnerName()));
      if (playerData.getLastChunk() != chunk) {
        playerData.setLastChunk(chunk);
        Visualization.apply(
            player,
            Visualization.fromChunk(
                chunk, location.getY(), VisualizationType.ERROR_CHUNK, location));
      }
      return ActionResult.FAIL;
    }
  }

  /**
   * Prevent players from using spawn egg items on a block. The player must right click on a block
   * with the spawn egg in order to spawn, hence the action is a block event.
   *
   * @param playerEntity Player trying to use the spawn egg.
   * @param world World the player is in.
   * @param hand Player hand performing the action.
   * @param hitResult Hit result of the action.
   * @return PASS if the action is allowed, else FAIL.
   */
  private ActionResult onSpawnEggUse(
      PlayerEntity playerEntity, World world, Hand hand, BlockHitResult hitResult) {
    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }

    if (ChunkClaimTags.SPAWN_EGG_ITEMS.contains(playerEntity.getStackInHand(hand).getItem())) {
      // Purchase the mob spawn with credits.
      ChunkClaimPlayer player = new ChunkClaimPlayer(playerEntity);
      PlayerData playerData = this.dataStore.getPlayerData(player.getName());
      if (playerData.canAffordMob()) {
        if (!ChunkClaimFabric.getClaimConfig().areMobsFree()) {
          playerData.removeCredits(ChunkClaimFabric.getClaimConfig().getMobPrice());
          this.dataStore.savePlayerData(player.getName(), playerData);
          player.sendMessages(
              ChunkClaimPrompt.joinText(
                  " ",
                  ChunkClaimPrompt.get("prompt.chunkclaim.purchased_mob"),
                  ChunkClaimPrompt.creditsAmount(playerData.getCredits())));
        }
        return ActionResult.PASS;
      } else {
        player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.not_enough_credits_mob"));
        return ActionResult.FAIL;
      }
    } else {
      return ActionResult.PASS;
    }
  }

  /**
   * Handler for block spread events, delegates to onFromTo.
   *
   * @param world World spread event is occurring.
   * @param fromBlockPos Spread from position.
   * @param toBlockPos Spread to position.
   * @param blockState Spread to new state.
   * @return PASS if spread is allowed, else FAIL.
   */
  ActionResult onSpreadFromTo(
      ServerWorld world, BlockPos fromBlockPos, BlockPos toBlockPos, BlockState blockState) {
    return onFromTo(world, fromBlockPos, toBlockPos);
  }

  /**
   * Handler for fluid flow events, delegates to onFromTo.
   *
   * @param world World fluid flow event is occurring.
   * @param toBlockPos Spread to position.
   * @param toBlockState Spread to state.
   * @param direction Spread direction.
   * @param fluidState Flowing fluid's state.
   * @return PASS if the fluid flow is allowed, else FAIL.
   */
  private ActionResult onFluidFromTo(
      World world,
      BlockPos toBlockPos,
      BlockState toBlockState,
      Direction direction,
      FluidState fluidState) {
    // Always allow fluids to flow straight down.
    if (Direction.DOWN == direction) {
      return ActionResult.PASS;
    }
    return onFromTo(world, toBlockPos.offset(direction.getOpposite()), toBlockPos);
  }

  /**
   * Ensures dispensers or droppers cannot be used to dispense a block or item across a claim
   * boundary. Delegates to onFromTo.
   *
   * @param world World event is occurring.
   * @param fromBlockPos Position in the world of the dispenser/dropper.
   * @param toBlockPos Position being dispensed/dropped to.
   * @return PASS if the dispense is allowed, else FAIL.
   */
  private ActionResult onDispenseFromTo(World world, BlockPos fromBlockPos, BlockPos toBlockPos) {
    return onFromTo(world, fromBlockPos, toBlockPos);
  }

  /**
   * Ensures hoppers cannot be used to insert items into a container across a claim boundary.
   * Delegates to onFromTo.
   *
   * @param hopper Hopper trying to insert items.
   * @param toBlockPos Position being inserted into.
   * @return PASS if the insert is allowed, else FAIL.
   */
  private ActionResult onHopperInsert(HopperBlockEntity hopper, BlockPos toBlockPos) {
    return onFromTo(hopper.getWorld(), hopper.getPos(), toBlockPos);
  }

  /**
   * General case handler for handling events that may go from one claim to another.
   *
   * @param world World event is occurring.
   * @param fromBlockPos Position in the world the event is "moving" from.
   * @param toBlockPos Position in the world the event is "moving" to.
   * @return PASS if the movement is allowed, else FAIL.
   */
  private ActionResult onFromTo(World world, BlockPos fromBlockPos, BlockPos toBlockPos) {
    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }
    // From where?
    Chunk fromChunk = this.dataStore.getChunkAt(new EdgestitchLocation(world, fromBlockPos), null);
    // Where to?
    Chunk toChunk = this.dataStore.getChunkAt(new EdgestitchLocation(world, toBlockPos), fromChunk);

    return onFromToChunk(fromChunk, toChunk);
  }

  /**
   * Primary implementation for determining from-to validity.
   *
   * @param fromChunk Chunk that is the origin of the movement.
   * @param toChunk Chunk that is the destination of the movement.
   * @return PASS if movement is allowed, else FAIL.
   */
  ActionResult onFromToChunk(Chunk fromChunk, Chunk toChunk) {
    // Allow spread within the same claim or wilderness to wilderness.
    if (fromChunk == toChunk) {
      return ActionResult.PASS;
    }

    // Block any spread into the wilderness from a claim.
    if (null != fromChunk && null == toChunk) {
      return ActionResult.FAIL;
    }
    // If spreading into a claim.
    else {
      // Who owns the spreading block, if anyone?
      String fromOwner = null;
      if (null != fromChunk) {
        fromOwner = fromChunk.getOwnerName();
      }

      // Cancel unless the owner of the spreading block is allowed to build in the receiving claim.
      if (null == fromOwner || !toChunk.canModify(fromOwner)) {
        return ActionResult.FAIL;
      }
    }
    return ActionResult.PASS;
  }

  /**
   * Prevents tree growth between non-contiguous claims.
   *
   * @param growthType Growth type of the sapling.
   * @param feature Tree feature that performs generation.
   * @param world World tree is being grown in.
   * @param rootBlockPos Root position of the growth.
   * @param logPositions Set of coordinates for the tree logs.
   * @param leavesPositions Set of coordinates for the tree leaves.
   * @return {@code PASS} always.
   */
  private ActionResult onTreeGrow(
      GrowthType growthType,
      TreeFeature feature,
      World world,
      BlockPos rootBlockPos,
      Set<BlockPos> logPositions,
      Set<BlockPos> leavesPositions,
      Set<BlockPos> decoratorPositions) {

    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }

    // From where?
    Chunk fromChunk = this.dataStore.getChunkAt(new EdgestitchLocation(world, rootBlockPos), null);

    // To where?
    Chunk toChunk = null;
    for (Set<BlockPos> posSet : Arrays.asList(logPositions, leavesPositions, decoratorPositions)) {
      for (Iterator<BlockPos> iterator = posSet.iterator(); iterator.hasNext(); ) {
        BlockPos toPos = iterator.next();
        toChunk = this.dataStore.getChunkAt(new EdgestitchLocation(world, toPos), toChunk);
        if (ActionResult.FAIL == onFromToChunk(fromChunk, toChunk)) {
          TreeFeature.setBlockStateWithoutUpdatingNeighbors(
              world, toPos, Blocks.AIR.getDefaultState());
          iterator.remove();
        }
      }
    }
    return ActionResult.PASS;
  }
}
