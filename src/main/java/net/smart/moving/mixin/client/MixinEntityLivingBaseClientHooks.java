package net.smart.moving.mixin.client;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.smart.moving.IEntityPlayerSP;
import net.smart.moving.SMSelf;
import net.smart.moving.render.ISmartMovingRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBaseClientHooks {
    @Inject(method = "travel", at = @At("RETURN"))
    private void smartMoving$sampleLocalMovement(float strafe, float vertical, float forward, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerSP)
            ((ISmartMovingRenderState) this).smartMoving$getStatistics().calculate(false);
    }
    @Inject(method = "isOnLadder", at = @At("HEAD"), cancellable = true)
    private void smartMoving$isOnLadder(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof EntityPlayerSP) {
            SMSelf controller = (SMSelf) ((IEntityPlayerSP) this).getMoving();
            if (controller != null)
                cir.setReturnValue(controller.isOnLadderOrVine());
        }
    }
}
