package net.smart.moving.mixin.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.smart.moving.IEntityPlayerMP;
import net.smart.moving.SMServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntityServerHooks {
    @Inject(method = "setPosition", at = @At("RETURN"))
    private void smartMoving$afterSetPosition(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerMP) {
            SMServer controller = ((IEntityPlayerMP) this).getMoving();
            if (controller != null)
                controller.afterSetPosition(x, y, z);
        }
    }

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    private void smartMoving$isSneaking(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof EntityPlayerMP
                && ((EntityPlayerMP) (Object) this).getItemInUseCount() > 0)
            cir.setReturnValue(true);
    }
}
