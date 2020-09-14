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

package com.github.plateofpasta.chunkclaimfabric.command;

import com.github.plateofpasta.chunkclaimfabric.command.admin.*;
import com.github.plateofpasta.chunkclaimfabric.player.ChunkClaimPlayer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ChunkCommands {
  public static String CHUNK_COMMANDS_NAMESPACE = "chunk";

  public static void init() {
    AdminPredicate adminPredicate = new AdminPredicate();
    LiteralArgumentBuilder<ServerCommandSource> builder =
        CommandManager.literal(CHUNK_COMMANDS_NAMESPACE)
            // Abandon command.
            .then(
                CommandManager.literal(Abandon.NAMESPACE)
                    .then(
                        CommandManager.argument(
                                // Minimum 0 max 10.
                                Abandon.ARG0_NAMESPACE, IntegerArgumentType.integer(0, 10))
                            .executes(new Abandon()))
                    .executes(new Abandon()))
            // Bonus admin command.
            .then(
                CommandManager.literal(Bonus.NAMESPACE)
                    .requires(adminPredicate)
                    .then(
                        CommandManager.argument(Bonus.ARG0_NAMESPACE, EntityArgumentType.player())
                            .then(
                                CommandManager.argument(
                                        // Minimum 1.
                                        Bonus.ARG1_NAMESPACE, IntegerArgumentType.integer(1))
                                    .executes(new Bonus()))))
            // Claim command.
            .then(CommandManager.literal(Claim.NAMESPACE).executes(new Claim()))
            // Credits command.
            .then(CommandManager.literal(Credits.NAMESPACE).executes(new Credits()))
            // Delete admin command.
            .then(
                CommandManager.literal(Delete.NAMESPACE)
                    .requires(adminPredicate)
                    .then(
                        CommandManager.argument(Delete.ARG0_NAMESPACE, EntityArgumentType.player())
                            .then(
                                CommandManager.argument(
                                        // Minimum 0 max 10.
                                        Delete.ARG1_NAMESPACE, IntegerArgumentType.integer(0, 10))
                                    .executes(new Delete()))
                            .executes(new Delete())))
            // DeleteAll admin command.
            .then(
                CommandManager.literal(DeleteAll.NAMESPACE)
                    .requires(adminPredicate)
                    .then(
                        CommandManager.argument(
                                DeleteAll.ARG0_NAMESPACE, EntityArgumentType.player())
                            .executes(new DeleteAll())))
            // Help command.
            .then(CommandManager.literal(Help.NAMESPACE).executes(new Help()))
            // Ignore admin command.
            .then(
                CommandManager.literal(Ignore.NAMESPACE)
                    .requires(adminPredicate)
                    .executes(new Ignore()))
            // List admin command.
            .then(
                CommandManager.literal(ListCommand.NAMESPACE)
                    .requires(adminPredicate)
                    .then(
                        CommandManager.argument(
                                ListCommand.ARG0_NAMESPACE, EntityArgumentType.player())
                            .executes(new ListCommand())))
            // Next command.
            .then(
                CommandManager.literal(Next.NAMESPACE)
                    .requires(adminPredicate)
                    .then(
                        CommandManager.argument(Next.ARG0_NAMESPACE, EntityArgumentType.player())
                            .executes(new Next())))
            // Trust command.
            .then(
                CommandManager.literal(Trust.NAMESPACE)
                    .then(
                        CommandManager.argument(Trust.ARG0_NAMESPACE, EntityArgumentType.player())
                            .executes(new Trust())))
            // Untrust command.
            .then(
                CommandManager.literal(Untrust.NAMESPACE)
                    .then(
                        CommandManager.argument(Untrust.ARG0_NAMESPACE, EntityArgumentType.player())
                            .executes(new Untrust())))
            // Testing command.
            .then(
                CommandManager.literal("test")
                    .executes(
                        context -> {
                          System.out.println("canary");
                          return 0;
                        }))
            // Base chunk command executor.
            .executes(new BaseCommand());

    CommandRegistrationCallback.EVENT.register(
        (dispatcher, dedicated) -> {
          if (dedicated) {
            dispatcher.register(builder);
          }
        });
  }

  /**
   * Helper to parse integer arguments with a default value.
   *
   * @param context Command context to parse from.
   * @param argNamespace String name of the argument to parse from the command context.
   * @param defaultValue Default value when parsing fails.
   * @return Parsed integer value.
   */
  public static int parseIntWithDefault(
      CommandContext<ServerCommandSource> context, String argNamespace, int defaultValue) {
    int intArg = defaultValue;
    try {
      intArg = context.getArgument(argNamespace, Integer.class);
    } catch (IllegalArgumentException ignored) {
    }

    return intArg;
  }

  /**
   * Helper to parse player name arguments.
   *
   * @param context Command context to parse from. * @param argNamespace String name of the argument
   *     to parse from the command context.
   * @return {@link ChunkClaimPlayer} parsed from argument.
   * @throws IllegalArgumentException Thrown if argument parsing failed, most likely due to invalid
   *     argument namespace.
   * @throws CommandSyntaxException Thrown if argument parsing failed, most likely due to invalid
   *     argument namespace.
   */
  public static ChunkClaimPlayer parsePlayerArg(
      CommandContext<ServerCommandSource> context, String argNamespace)
      throws IllegalArgumentException, CommandSyntaxException {
    return new ChunkClaimPlayer(EntityArgumentType.getPlayer(context, argNamespace));
  }
}
