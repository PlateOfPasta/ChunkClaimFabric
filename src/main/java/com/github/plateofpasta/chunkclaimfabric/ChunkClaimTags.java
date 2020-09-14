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

import com.github.plateofpasta.chunkclaimfabric.util.TagBuilderHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;

/** Wrapper class for holding custom tags. */
public class ChunkClaimTags {
  public static final Tag<Item> CHECKED_ITEMS =
      (new TagBuilderHelper<>(ChunkClaimFabric.MOD_ID + "-checked-items", Registry.ITEM))
          .add(
              Items.BUCKET,
              Items.WATER_BUCKET,
              Items.LAVA_BUCKET,
              Items.COD_BUCKET,
              Items.PUFFERFISH_BUCKET,
              Items.SALMON_BUCKET,
              Items.TROPICAL_FISH_BUCKET)
          .build();
  public static final Tag<Item> ALLOWED_DROPPABLE_ITEMS =
      (new TagBuilderHelper<>(ChunkClaimFabric.MOD_ID + "-allowed-droppable-items", Registry.ITEM))
          .add(Items.BOOK, Items.WRITABLE_BOOK, Items.WRITTEN_BOOK)
          .build();
  public static final Tag<Item> CHECKED_PLACED_BLOCKS =
      (new TagBuilderHelper<>(ChunkClaimFabric.MOD_ID + "-checked-placed-blocks", Registry.ITEM))
          .add(Items.FIRE_CHARGE, Items.FLINT_AND_STEEL)
          .add(ItemTags.SAPLINGS)
          .build();
  public static final Tag<Item> SPAWN_EGG_ITEMS =
      (new TagBuilderHelper<>(ChunkClaimFabric.MOD_ID + "-checked-spawn-eggs", Registry.ITEM))
          .add(
              Items.BAT_SPAWN_EGG,
              Items.BEE_SPAWN_EGG,
              Items.BLAZE_SPAWN_EGG,
              Items.CAT_SPAWN_EGG,
              Items.CAVE_SPIDER_SPAWN_EGG,
              Items.CHICKEN_SPAWN_EGG,
              Items.COD_SPAWN_EGG,
              Items.COW_SPAWN_EGG,
              Items.CREEPER_SPAWN_EGG,
              Items.DOLPHIN_SPAWN_EGG,
              Items.DONKEY_SPAWN_EGG,
              Items.DROWNED_SPAWN_EGG,
              Items.ELDER_GUARDIAN_SPAWN_EGG,
              Items.ENDERMAN_SPAWN_EGG,
              Items.ENDERMITE_SPAWN_EGG,
              Items.EVOKER_SPAWN_EGG,
              Items.FOX_SPAWN_EGG,
              Items.GHAST_SPAWN_EGG,
              Items.GUARDIAN_SPAWN_EGG,
              Items.HORSE_SPAWN_EGG,
              Items.HUSK_SPAWN_EGG,
              Items.LLAMA_SPAWN_EGG,
              Items.MAGMA_CUBE_SPAWN_EGG,
              Items.MOOSHROOM_SPAWN_EGG,
              Items.MULE_SPAWN_EGG,
              Items.OCELOT_SPAWN_EGG,
              Items.PANDA_SPAWN_EGG,
              Items.PARROT_SPAWN_EGG,
              Items.PHANTOM_SPAWN_EGG,
              Items.PIG_SPAWN_EGG,
              Items.PILLAGER_SPAWN_EGG,
              Items.POLAR_BEAR_SPAWN_EGG,
              Items.PUFFERFISH_SPAWN_EGG,
              Items.RABBIT_SPAWN_EGG,
              Items.RAVAGER_SPAWN_EGG,
              Items.SALMON_SPAWN_EGG,
              Items.SHEEP_SPAWN_EGG,
              Items.SHULKER_SPAWN_EGG,
              Items.SILVERFISH_SPAWN_EGG,
              Items.SKELETON_SPAWN_EGG,
              Items.SKELETON_HORSE_SPAWN_EGG,
              Items.SLIME_SPAWN_EGG,
              Items.SPIDER_SPAWN_EGG,
              Items.SQUID_SPAWN_EGG,
              Items.STRAY_SPAWN_EGG,
              Items.TRADER_LLAMA_SPAWN_EGG,
              Items.TROPICAL_FISH_SPAWN_EGG,
              Items.TURTLE_SPAWN_EGG,
              Items.VEX_SPAWN_EGG,
              Items.VILLAGER_SPAWN_EGG,
              Items.VINDICATOR_SPAWN_EGG,
              Items.WANDERING_TRADER_SPAWN_EGG,
              Items.WITCH_SPAWN_EGG,
              Items.WITHER_SKELETON_SPAWN_EGG,
              Items.WOLF_SPAWN_EGG,
              Items.ZOMBIE_SPAWN_EGG,
              Items.ZOMBIE_HORSE_SPAWN_EGG,
              Items.ZOMBIFIED_PIGLIN_SPAWN_EGG,
              Items.ZOMBIE_VILLAGER_SPAWN_EGG)
          .build();
  public static final Tag<EntityType<?>> PROTECTED_ENTITY =
      (new TagBuilderHelper<>(
              ChunkClaimFabric.MOD_ID + "-protected-entities", Registry.ENTITY_TYPE))
          .add(
              EntityType.BAT,
              EntityType.BEE,
              EntityType.CAT,
              EntityType.CHICKEN,
              EntityType.COD,
              EntityType.COW,
              EntityType.DOLPHIN,
              EntityType.DONKEY,
              EntityType.FOX,
              EntityType.HORSE,
              EntityType.IRON_GOLEM,
              EntityType.LLAMA,
              EntityType.MOOSHROOM,
              EntityType.MULE,
              EntityType.OCELOT,
              EntityType.PANDA,
              EntityType.PARROT,
              EntityType.PIG,
              EntityType.POLAR_BEAR,
              EntityType.PUFFERFISH,
              EntityType.RABBIT,
              EntityType.SALMON,
              EntityType.SHEEP,
              EntityType.SNOW_GOLEM,
              EntityType.SQUID,
              EntityType.TRADER_LLAMA,
              EntityType.TROPICAL_FISH,
              EntityType.TURTLE,
              EntityType.VILLAGER,
              EntityType.WANDERING_TRADER,
              EntityType.WOLF,
              EntityType.PAINTING,
              EntityType.BOAT,
              EntityType.CHEST_MINECART,
              EntityType.COMMAND_BLOCK_MINECART,
              EntityType.FURNACE_MINECART,
              EntityType.HOPPER_MINECART,
              EntityType.MINECART,
              EntityType.SPAWNER_MINECART,
              EntityType.TNT_MINECART)
          .build();
  public static final Tag<EntityType<?>> CHECKED_THROWN_ENTITIES =
      (new TagBuilderHelper<>(
              ChunkClaimFabric.MOD_ID + "-checked-thrown-entities", Registry.ENTITY_TYPE))
          .add(EntityType.EXPERIENCE_BOTTLE, EntityType.POTION)
          .build();
}
