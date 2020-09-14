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

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.io.InputStreamReader;
import java.util.*;

import static com.github.plateofpasta.chunkclaimfabric.ChunkClaimFabric.MOD_ID;

/** Because lang files aren't a thing on dedicated servers. */
public abstract class ChunkClaimPrompt {
  public static final Map<String, String> kvMap;

  static {
    Map<String, String> tempMap;
    try {
      tempMap =
          new Gson()
              .fromJson(
                  new JsonReader(
                      new InputStreamReader(
                          ChunkClaimConfig.class.getResourceAsStream(
                              "/assets/chunkclaimfabric/lang/en_us.json"))),
                  Map.class);
    } catch (Exception e) {
      tempMap = new TreeMap<>();
    }
    kvMap = tempMap;
  }

  /**
   * Acquires the text from the en_us.json file if it exists and converts it to {@link MutableText}.
   * If variadic arguments are supplied, it is assumed the value from the JSON file is a format
   * string and will be applied to {@link String#format(String, Object...)}. \n Note, this should
   * only be used on dedicated servers where there is not support for lang files.
   *
   * @param key String key for a value in the en_us.json file.
   * @param args Optional variadic for when the JSON value string is a format string.
   * @return {@link MutableText} value prompt from the JSON language entry.
   */
  public static MutableText get(String key, Object... args) {
    if (kvMap.isEmpty()) {
      return new LiteralText(
          String.format(
              "The en_us.json lang file for %s has failed to load. Please try reloading the server or reinstalling the mod.",
              MOD_ID));
    } else {
      if (0 == args.length) {
        return new LiteralText(kvMap.get(key));
      } else {
        return new LiteralText(String.format(kvMap.get(key), args));
      }
    }
  }

  /**
   * Joins {@link MutableText} with a delimiter.
   *
   * @param delim String delimiter.
   * @param textArgs Variadic list of {@link MutableText} to join together.
   * @return {@link MutableText} consisting of all other text joined with the delimiter.
   */
  public static MutableText joinText(String delim, MutableText... textArgs) {
    List<MutableText> textList = Arrays.asList(textArgs);
    Iterator<MutableText> iter = textList.iterator();
    MutableText init;
    if (iter.hasNext()) {
      init = iter.next();
    } else {
      return new LiteralText("");
    }
    while (iter.hasNext()) {
      init.append(delim);
      init.append(iter.next());
    }
    return init;
  }

  /**
   * Helper for forming variable prompt.
   *
   * @param remainingCredits Amount of credits the player has left.
   * @return String for player message.
   */
  public static MutableText justClaimedChunk(double remainingCredits) {
    return get("prompt.chunkclaim.format.just_claimed_chunk", remainingCredits);
  }

  /**
   * Helper for forming variable prompt.
   *
   * @param playerName Player name whose claim we don't have permission to build on.
   * @return Player message.
   */
  public static MutableText noPermissionFrom(String playerName) {
    return get("prompt.chunkclaim.format.no_permission_from", playerName);
  }

  /**
   * Helper for forming variable prompt.
   *
   * @param playerName Player name whose claim we don't have permission to build on.
   * @return String for player message.
   */
  public static MutableText noBuildPermissionFrom(String playerName) {
    return get("prompt.chunkclaim.format.no_build_permission_from", playerName);
  }

  /**
   * Helper for forming variable prompt.
   *
   * @param playerName Player name whose claim we have permission to build on.
   * @return String for player message.
   */
  public static MutableText buildPermissionFrom(String playerName) {
    return get("prompt.chunkclaim.format.build_permission_from", playerName);
  }

  /**
   * Helper for forming a list of trusted builders string.
   *
   * @param trustedBuilders String list of trusted builders.
   * @return String describing trusted builders.
   */
  public static MutableText trustedBuilderList(String trustedBuilders) {
    if (trustedBuilders.isEmpty()) {
      return get("prompt.chunkclaim.format.trusted_builder_list", "None");
    } else {
      return get("prompt.chunkclaim.format.trusted_builder_list", trustedBuilders);
    }
  }

  /**
   * Helper for forming a trusted builder string.
   *
   * @param trustedBuilder String name of one trusted builder.
   * @return String describing the trusted builder.
   */
  public static MutableText trustedBuilder(String trustedBuilder) {
    return get("prompt.chunkclaim.format.trusted_builder", trustedBuilder);
  }

  /**
   * Helper for forming a trusted builder string.
   *
   * @param untrustedBuilder String name of one untrusted builder.
   * @return String describing the trusted builder.
   */
  public static MutableText untrustedBuilder(String untrustedBuilder) {
    return get("prompt.chunkclaim.format.untrusted_builder", untrustedBuilder);
  }

  /**
   * Helper for forming a string that describes number of days since a player logged in.
   *
   * @param days Days since last login.
   * @return String describing days since last login.
   */
  public static MutableText daysSinceLastLogin(long days) {
    return get("prompt.chunkclaim.format.last_login_days", days);
  }

  /**
   * Helper for forming a string that describes number of days since a player first logged in.
   *
   * @param days Days since first login.
   * @return String describing days since first login.
   */
  public static MutableText daysSinceFirstLogin(long days) {
    return get("prompt.chunkclaim.format.first_login_days", days);
  }

  /**
   * Helper for forming a string that the amount of credits.
   *
   * @param credits Amount of credits.
   * @return String describing credits amount.
   */
  public static MutableText creditsAmount(double credits) {
    return get("prompt.chunkclaim.format.total_credits", credits);
  }

  /**
   * Helper for forming a string describing how many credits something costs.
   *
   * @param item String name of the item.
   * @param cost Cost value.
   */
  public static MutableText creditsCost(String item, double cost) {
    return get("prompt.chunkclaim.format.item_costs_credits", item, cost);
  }

  /**
   * Helper for forming a string specifying a bonus adjustment.
   *
   * @param playerName Player who's bonus is being adjusted.
   * @param bonus Bonus adjustment amount.
   * @return String description.
   */
  public static MutableText adjustedBonus(String playerName, int bonus) {
    return get("prompt.chunkclaim.format.adjust_bonus", playerName, bonus);
  }

  /**
   * Helper for forming a string specifying who can delete (abandon) a chunk.
   *
   * @param playerName Player (owner) who can delete the chunk.
   * @return String description.
   */
  public static MutableText onlyOtherCanDelete(String playerName) {
    return get("prompt.chunkclaim.format.only_other_delete", playerName);
  }

  /**
   * Helper for forming a string that describes a radius operation that was performed.
   *
   * @param radiusOp Operation performed (e.g. deleted, abandoned)
   * @param radius Numerical radius.
   * @param numberChunks Numerical amount of chunks effected.
   * @return String description.
   */
  public static MutableText radiusOperation(String radiusOp, int radius, int numberChunks) {
    return get("prompt.chunkclaim.format.radius_operation", radiusOp, radius, numberChunks);
  }

  /**
   * Helper for forming a string that describes a number of chunks deleted.
   *
   * @param numberChunks Numerical amount of chunks effected.
   * @return String description.
   */
  public static MutableText chunksDeleted(int numberChunks) {
    return get("prompt.chunkclaim.format.chunks_deleted", numberChunks);
  }

  /** @return String containing usage strings for all normal player commands. */
  public static MutableText helpPlayerCommands() {
    return joinText(
        "\n",
        get("prompt.chunkclaim.cmd.usage.abandon"),
        get("prompt.chunkclaim.cmd.usage.claim"),
        get("prompt.chunkclaim.cmd.usage.credits"),
        get("prompt.chunkclaim.cmd.usage.help"),
        get("prompt.chunkclaim.cmd.usage.mark"),
        get("prompt.chunkclaim.cmd.usage.trust"),
        get("prompt.chunkclaim.cmd.usage.untrust"));
  }

  /** @return String containing usage strings for all admin player commands. */
  public static MutableText helpAdminCommands() {
    return joinText(
        "\n",
        get("prompt.chunkclaim.cmd.admin.usage.bonus"),
        get("prompt.chunkclaim.cmd.admin.usage.delete"),
        get("prompt.chunkclaim.cmd.admin.usage.delete_all"),
        get("prompt.chunkclaim.cmd.admin.usage.ignore"),
        get("prompt.chunkclaim.cmd.admin.usage.list"),
        get("prompt.chunkclaim.cmd.admin.usage.next"));
  }
}
