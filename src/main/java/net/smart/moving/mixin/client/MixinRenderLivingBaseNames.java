package net.smart.moving.mixin.client;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.smart.moving.render.ISmartMovingRenderer;
import net.smart.moving.render.SMRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLivingBase.class)
public abstract class MixinRenderLivingBaseNames {
    @Unique private AbstractClientPlayer smartMoving$namePlayer;
    @Unique private boolean smartMoving$originalSneaking;

    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("HEAD"))
    private void smartMoving$beforeName(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof RenderPlayer && entity instanceof AbstractClientPlayer) {
            smartMoving$namePlayer = (AbstractClientPlayer) entity;
            SMRender render = ((ISmartMovingRenderer) this).smartMoving$getRenderer();
            smartMoving$originalSneaking = render.beforeRenderName(smartMoving$namePlayer);
        }
    }

    @ModifyArg(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderLivingBase;renderEntityName(Lnet/minecraft/entity/Entity;DDDLjava/lang/String;D)V"),
            index = 2)
    private double smartMoving$nameY(double y) {
        return smartMoving$namePlayer == null ? y
                : ((ISmartMovingRenderer) this).smartMoving$getRenderer()
                    .modifyNameY(smartMoving$namePlayer, y, smartMoving$originalSneaking);
    }

    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("RETURN"))
    private void smartMoving$afterName(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        if (smartMoving$namePlayer != null) {
            ((ISmartMovingRenderer) this).smartMoving$getRenderer()
                    .afterRenderName(smartMoving$namePlayer, smartMoving$originalSneaking);
            smartMoving$namePlayer = null;
        }
    }

    @Inject(method = "renderLayers", at = @At("HEAD"))
    private void smartMoving$beforeLayers(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
            float partialTicks, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {
        if ((Object) this instanceof RenderPlayer && entity instanceof AbstractClientPlayer)
            ((ISmartMovingRenderer) this).smartMoving$getRenderer()
                    .beforeLayers((AbstractClientPlayer) entity, partialTicks);
    }

    @Inject(method = "renderLayers", at = @At("RETURN"))
    private void smartMoving$afterLayers(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
            float partialTicks, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {
        if ((Object) this instanceof RenderPlayer && entity instanceof AbstractClientPlayer)
            ((ISmartMovingRenderer) this).smartMoving$getRenderer().afterLayers();
    }

    @Inject(method = "handleRotationFloat", at = @At("RETURN"), cancellable = true)
    private void smartMoving$ridingTime(EntityLivingBase entity, float partialTicks,
            CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof RenderPlayer && entity instanceof AbstractClientPlayer)
            cir.setReturnValue(cir.getReturnValueF()
                    + ((net.smart.moving.render.ISmartMovingRenderState) entity)
                            .smartMoving$getStatistics().ticksRiding);
    }
}
