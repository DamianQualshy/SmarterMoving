package net.smart.moving.mixin.server;

import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NetHandlerPlayServer.class)
public interface NetHandlerPlayServerAccessor {
    @Accessor("floatingTickCount")
    void smartMoving$setFloatingTickCount(int ticks);
}
