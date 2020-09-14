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

package com.github.plateofpasta.chunkclaimfabric.util;

import java.util.Date;

/** Util class to provide static helpers for Time/Date. */
public class TimeDateUtil {
  /**
   * Helper for finding the difference in days between two dates.
   *
   * @param d1 First date (minuend).
   * @param d2 Second date (subtrahend).
   * @return {@code d1 - d2} to days.
   */
  public static long differenceInDays(Date d1, Date d2) {
    return (d1.getTime() - d2.getTime()) / (1000 * 60 * 60 * 24);
  }

  /**
   * Helper for finding the difference in minutes between two dates.
   *
   * @param d1 First date (minuend).
   * @param d2 Second date (subtrahend).
   * @return {@code d1 - d2} to minutes.
   */
  public static long differenceInMinutes(Date d1, Date d2) {
    return (d1.getTime() - d2.getTime()) / (1000 * 60);
  }
}
