package net.smart.moving.mixin.client;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.smart.moving.IEntityPlayerSP;
import net.smart.moving.SMSelf;
import net.smart.moving.render.ISmartMovingRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntityClientHooks {
    @Inject(method = "updateRidden", at = @At("RETURN"))
    private void smartMoving$sampleRiding(CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerSP)
            ((ISmartMovingRenderState) this).smartMoving$getStatistics().calculateRidden();
    }
    @Unique
    private SMSelf smartMoving$controller() {
        if ((Object) this instanceof EntityPlayerSP)
            return (SMSelf) ((IEntityPlayerSP) this).getMoving();
        return null;
    }

    @Inject(method = "getBrightness", at = @At("HEAD"))
    private void smartMoving$beforeBrightness(CallbackInfoReturnable<Float> cir) {
        SMSelf controller = smartMoving$controller();
        if (controller != null)
            ((Entity) (Object) this).posY -= controller.getHeightOffset();
    }

    @Inject(method = "getBrightness", at = @At("RETURN"))
    private void smartMoving$afterBrightness(CallbackInfoReturnable<Float> cir) {
        SMSelf controller = smartMoving$controller();
        if (controller != null)
            ((Entity) (Object) this).posY += controller.getHeightOffset();
    }

    @Inject(method = "getBrightnessForRender", at = @At("HEAD"))
    private void smartMoving$beforeRenderBrightness(CallbackInfoReturnable<Integer> cir) {
        SMSelf controller = smartMoving$controller();
        if (controller != null)
            ((Entity) (Object) this).posY -= controller.getHeightOffset();
    }

    @Inject(method = "getBrightnessForRender", at = @At("RETURN"))
    private void smartMoving$afterRenderBrightness(CallbackInfoReturnable<Integer> cir) {
        SMSelf controller = smartMoving$controller();
        if (controller != null)
            ((Entity) (Object) this).posY += controller.getHeightOffset();
    }

    @Inject(method = "isInsideOfMaterial", at = @At("HEAD"), cancellable = true)
    private void smartMoving$material(Material material, CallbackInfoReturnable<Boolean> cir) {
        SMSelf controller = smartMoving$controller();
        if (controller != null) {
            Boolean override = controller.getMaterialOverride(material);
            if (override != null)
                cir.setReturnValue(override);
        }
    }

    @Inject(method = "setPositionAndRotation", at = @At("HEAD"))
    private void smartMoving$beforePositionAndRotation(double x, double y, double z,
            float yaw, float pitch, CallbackInfo ci) {
        SMSelf controller = smartMoving$controller();
        if (controller != null)
            controller.beforeSetPositionAndRotation();
    }
}
