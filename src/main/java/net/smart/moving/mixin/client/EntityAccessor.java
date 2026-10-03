package net.smart.moving.mixin.client;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("isInWeb")
    boolean smartMoving$isInWeb();

    @Accessor("isInWeb")
    void smartMoving$setInWeb(boolean inWeb);
}
