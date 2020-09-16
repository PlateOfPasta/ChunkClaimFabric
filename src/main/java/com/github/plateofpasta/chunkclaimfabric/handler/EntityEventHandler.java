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

import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimPrompt;
import com.github.plateofpasta.chunkclaimfabric.config.ChunkClaimTags;
import com.github.plateofpasta.chunkclaimfabric.datastore.DataStore;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.github.plateofpasta.chunkclaimfabric.util.AlwaysMissHitResult;
import com.github.plateofpasta.chunkclaimfabric.util.ChunkClaimUtil;
import com.github.plateofpasta.chunkclaimfabric.world.Chunk;
import com.github.plateofpasta.edgestitch.event.ProjectileHitCallback;
import com.github.plateofpasta.edgestitch.event.ServerWorldEvents;
import com.github.plateofpasta.edgestitch.world.EdgestitchLocation;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** Handler for entity events. */
public class EntityEventHandler {
  private final DataStore dataStore;

  /**
   * The handler requires a reference to the datastore.
   *
   * @param dataStore Chunk claim datastore of the plugin's server.
   */
  public EntityEventHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  /**
   * Bootstrap static method for initializing the callbacks for world events.
   *
   * @param dataStore Chunk claim datastore of the plugin's server.
   */
  public static void initHandlers(DataStore dataStore) {
    EntityEventHandler handler = new EntityEventHandler(dataStore);
    UseEntityCallback.EVENT.register(handler::onPlayerInteractEntity);
    AttackEntityCallback.EVENT.register(handler::onPlayerInteractEntity);
    ServerWorldEvents.ENTITY_SPAWN.register(handler::onExpOrbSpawn);
    ProjectileHitCallback.EVENT.register(handler::onProjectileHit);
  }

  /**
   * When a player interacts with (right-clicks) an entity. Satisfies the Fabric event API
   * `UseEntityCallback`.
   *
   * @param playerEntity Player performing the action.
   * @param world World the action is occurring in.
   * @param hand Which hand is interacting. Ignored.
   * @param entity Target entity.
   * @param hitResult Hit result on the entity. Ignored.
   * @return Action result for this callback, whether we want to cancel the interaction or not.
   * @see net.fabricmc.fabric.api.event.player.UseEntityCallback
   */
  private ActionResult onPlayerInteractEntity(
      PlayerEntity playerEntity,
      World world,
      Hand hand,
      Entity entity,
      /* Nullable */ EntityHitResult hitResult) {

    if (playerEntity.isSpectator()) {
      return ActionResult.PASS;
    }

    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return ActionResult.PASS;
    }

    ChunkClaimPlayer player = new ChunkClaimPlayer(playerEntity);
    Chunk chunk =
        this.dataStore.getChunkAt(new EdgestitchLocation(world, entity.getBlockPos()), null);
    if (chunk == null) {
      player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.no_permission"));
    } else if ((ChunkClaimTags.PROTECTED_ENTITY.contains(entity.getType())
        && !chunk.canModify(player.getName()))) {
      player.sendMessage(ChunkClaimPrompt.noPermissionFrom(chunk.getOwnerName()));
      return ActionResult.FAIL;
    }
    return ActionResult.PASS;
  }

  /**
   * Prevents experience orbs from being spawned.
   *
   * @param world World the experience orb is being spawned in.
   * @param entity Entity that might be an experience orb.
   */
  private TypedActionResult<Boolean> onExpOrbSpawn(World world, Entity entity) {
    if (!ChunkClaimUtil.isConfiguredWorld(world)) {
      return TypedActionResult.pass(null); // Return value doesn't matter on PASS.
    }

    if (entity instanceof ExperienceOrbEntity) {
      // Prevent the exp orb from being spawned and return false to indicate the entity was not
      // spawned.
      return TypedActionResult.fail(false);
    } else {
      return TypedActionResult.pass(null); // Return value doesn't matter on PASS.
    }
  }

  /**
   * Prevents projectiles from damaging other entities when fired by someone without claim
   * permissions.
   *
   * @param projectileEntity Projectile being fired.
   * @param hitResult Hit result of the entity.
   * @return PASS if hit result should be unmodified, else FAIL with a modified HitResult.
   */
  private TypedActionResult<HitResult> onProjectileHit(
      ProjectileEntity projectileEntity, HitResult hitResult) {
    if (hitResult instanceof EntityHitResult) {
      EntityHitResult entityHitResult = (EntityHitResult) hitResult;
      World world = entityHitResult.getEntity().getEntityWorld();
      if (!ChunkClaimUtil.isConfiguredWorld(world)) {
        return TypedActionResult.pass(null); // Return value doesn't matter on PASS.
      }

      // Projectile always misses if shot by non-player entity.
      if (null == projectileEntity.getOwner()
          || !(projectileEntity.getOwner() instanceof PlayerEntity)) {
        return TypedActionResult.fail(new AlwaysMissHitResult(entityHitResult.getPos()));
      }

      // Verify claim permission if a player shot at a protected entity.
      if (ChunkClaimTags.PROTECTED_ENTITY.contains(entityHitResult.getEntity().getType())) {
        ChunkClaimPlayer player = new ChunkClaimPlayer((PlayerEntity) projectileEntity.getOwner());
        EdgestitchLocation location =
            new EdgestitchLocation(world, entityHitResult.getEntity().getBlockPos());
        if (player.canPlayerModifyAtLocation(location)) {
          return TypedActionResult.pass(null); // Return value doesn't matter on PASS.
        } else {
          player.sendMessage(ChunkClaimPrompt.get("prompt.chunkclaim.entity_protected"));
          return TypedActionResult.fail(new AlwaysMissHitResult(entityHitResult.getPos()));
        }
      }
    }
    // We don't care about BlockHitResult.
    return TypedActionResult.pass(null); // Return value doesn't matter on PASS.
  }

  /**
   * Handler for thrown entities in chunk claim configured worlds.
   *
   * @param thrownEntity Entity being thrown.
   * @param hitResult Hit result of the entity.
   * @return PASS if this is not a chunk claim world, else FAIL to always remove the thrown entity
   *     if it is in the list of checked thrown entities.
   */
  private TypedActionResult<HitResult> onThrownCollision(
      ThrownEntity thrownEntity, HitResult hitResult) {
    // Any thrown entity in the checked list is always removed in configured worlds, regardless of
    // claims.
    if (ChunkClaimTags.CHECKED_THROWN_ENTITIES.contains(thrownEntity.getType())
        && ChunkClaimUtil.isConfiguredWorld(thrownEntity.getEntityWorld())) {
      return TypedActionResult.fail(null); // Return value doesn't matter on FAIL.
    } else {
      return TypedActionResult.pass(null); // Return value doesn't matter on PASS.
    }
  }
}
