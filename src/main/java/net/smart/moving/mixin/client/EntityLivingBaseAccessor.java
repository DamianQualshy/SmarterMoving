package net.smart.moving.mixin.client;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLivingBase.class)
public interface EntityLivingBaseAccessor {
    @Accessor("isJumping")
    boolean smartMoving$isJumping();

    @Accessor("isJumping")
    void smartMoving$setJumping(boolean jumping);
}
