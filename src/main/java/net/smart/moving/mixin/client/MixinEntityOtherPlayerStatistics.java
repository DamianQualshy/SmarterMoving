package net.smart.moving.mixin.client;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.smart.moving.render.ISmartMovingRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityOtherPlayerMP.class)
public abstract class MixinEntityOtherPlayerStatistics {
    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void smartMoving$sampleRemoteMovement(CallbackInfo ci) {
        ((ISmartMovingRenderState) this).smartMoving$getStatistics().calculate(true);
    }
}
