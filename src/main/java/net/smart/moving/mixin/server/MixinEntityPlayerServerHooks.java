package net.smart.moving.mixin.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.smart.moving.IEntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerServerHooks {
    @Inject(method = "onLivingUpdate", at = @At("RETURN"))
    private void smartMoving$afterLivingUpdate(CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerMP)
            ((IEntityPlayerMP) this).getMoving().afterOnLivingUpdate();
    }

    @Inject(method = "isPlayerSleeping", at = @At("HEAD"))
    private void smartMoving$beforeIsPlayerSleeping(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof EntityPlayerMP)
            ((IEntityPlayerMP) this).getMoving().beforeIsPlayerSleeping();
    }

    @Inject(method = "isEntityInsideOpaqueBlock", at = @At("HEAD"), cancellable = true)
    private void smartMoving$insideOpaqueBlock(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof EntityPlayerMP
                && ((IEntityPlayerMP) this).getMoving().crawlingCooldown > 0)
            cir.setReturnValue(false);
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    private void smartMoving$eyeHeight(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof EntityPlayerMP)
            cir.setReturnValue(((EntityPlayerMP) (Object) this).height - 0.18F);
    }

    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    private void smartMoving$addExhaustion(float exhaustion, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerMP) {
            ((EntityPlayerMP) (Object) this).getFoodStats().addExhaustion(exhaustion);
            ci.cancel();
        }
    }
}
