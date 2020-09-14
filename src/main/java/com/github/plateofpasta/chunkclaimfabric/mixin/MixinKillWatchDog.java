package com.github.plateofpasta.chunkclaimfabric.mixin;

import net.minecraft.server.dedicated.DedicatedServerWatchdog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServerWatchdog.class)
public class MixinKillWatchDog {

  @Inject(method = "run", at = @At("HEAD"), cancellable = true)
  void stopIfDebug(CallbackInfo callbackInfo) {
    boolean isDebug =
        java.lang.management.ManagementFactory.getRuntimeMXBean()
            .getInputArguments()
            .toString()
            .contains("jdwp");
    if (isDebug) {
      callbackInfo.cancel();
    }
  }
}
