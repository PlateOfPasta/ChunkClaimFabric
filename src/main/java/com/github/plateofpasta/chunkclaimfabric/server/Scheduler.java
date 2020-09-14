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

package com.github.plateofpasta.chunkclaimfabric.server;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

/**
 * Fabric event class for running operations at specific times in the future. If anyone knows what
 * originally made this please open a github issue.
 */
public class Scheduler {
  private final Int2ObjectMap<List<Consumer<MinecraftServer>>> scheduledTasks =
      new Int2ObjectOpenHashMap<>();
  private int currentTick = 0;

  /** Registers this object to the Fabric ServerTickCallback event registry. */
  public Scheduler() {
    ServerTickEvents.END_SERVER_TICK.register(
        minecraftServer -> {
          this.currentTick = minecraftServer.getTicks();
          final List<Consumer<MinecraftServer>> runnables =
              this.scheduledTasks.remove(this.currentTick);
          if (runnables != null) {
            for (Consumer<MinecraftServer> runnable : runnables) {
              runnable.accept(minecraftServer);
              if (runnable instanceof Repeating) { // reschedule repeating tasks
                Repeating repeating = ((Repeating) runnable);
                if (repeating.shouldQueue(this.currentTick)) {
                  this.queue(runnable, ((Repeating) runnable).next);
                }
              }
            }
          }
        });
  }

  /**
   * Queue a one time task to be executed on the server thread.
   *
   * @param task The action to perform.
   * @param tick How many ticks in the future this should be called, where 0 means at the end of the
   *     current tick.
   */
  public void queue(Consumer<MinecraftServer> task, int tick) {
    // Make the initial ArrayList smaller than the default size (10 elements) since it's unlikely,
    // on average, that there will be more than a few tasks per tick.
    this.scheduledTasks
        .computeIfAbsent(this.currentTick + tick + 1, t -> new ArrayList<>(4))
        .add(task);
  }

  /**
   * Schedule a repeating task that is executed infinitely every n ticks.
   *
   * @param task The action to perform.
   * @param tick How many ticks in the future this event should first be called.
   * @param interval The number of ticks in between each execution.
   */
  public void repeating(Consumer<MinecraftServer> task, int tick, int interval) {
    this.repeatWhile(task, null, tick, interval);
  }

  /**
   * Repeat the given task until the predicate returns false.
   *
   * @param task The action to perform.
   * @param requeue Whether or not to reschedule the task again, with the parameter being the
   *     current tick.
   * @param tick How many ticks in the future this event should first be called.
   * @param interval The number of ticks in between each execution.
   */
  public void repeatWhile(
      Consumer<MinecraftServer> task, IntPredicate requeue, int tick, int interval) {
    this.queue(new Repeating(task, requeue, interval), tick);
  }

  /** Consumer implementation for repeating tasks. */
  private static final class Repeating implements Consumer<MinecraftServer> {
    public final int next;
    private final Consumer<MinecraftServer> task;
    private final IntPredicate requeue;

    /**
     * @param task Delegated task.
     * @param requeue Predicate functional object for determining the exact repeat condition.
     * @param interval Interval between repetitions (in ticks)
     */
    private Repeating(Consumer<MinecraftServer> task, IntPredicate requeue, int interval) {
      this.task = task;
      this.requeue = requeue;
      this.next = interval;
    }

    /**
     * Determines if this task should be re-queued based on the current tick.
     *
     * @param predicate Current tick.
     * @return true if it should requeue, else false.
     */
    public boolean shouldQueue(int predicate) {
      if (this.requeue == null) {
        return true;
      }
      return this.requeue.test(predicate);
    }

    /**
     * Performs this task on the given argument.
     *
     * @param server Server to perform the task on.
     */
    @Override
    public void accept(MinecraftServer server) {
      this.task.accept(server);
    }
  }
}
