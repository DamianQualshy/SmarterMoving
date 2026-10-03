package net.smart.moving.mixin.client;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.smart.moving.render.ISmartMovingRenderer;
import net.smart.moving.render.SMRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer implements ISmartMovingRenderer {
    @Unique private SMRender smartMoving$renderer;

    @Override
    public SMRender smartMoving$getRenderer() {
        if (smartMoving$renderer == null)
            smartMoving$renderer = new SMRender((RenderPlayer) (Object) this);
        return smartMoving$renderer;
    }

    // The old doRender override wrapped vanilla. Keep Forge's Pre/Post events in the vanilla method.
    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("HEAD"))
    private void smartMoving$beforeRender(AbstractClientPlayer player, double x, double y, double z,
            float yaw, float partialTicks, CallbackInfo ci) {
        smartMoving$getRenderer().beforeDoRender(player, x, y, z, yaw, partialTicks);
    }

    @ModifyArg(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderLivingBase;doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V"),
            index = 2)
    private double smartMoving$renderY(double y) {
        return smartMoving$getRenderer().modifyRenderY(y);
    }

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("RETURN"))
    private void smartMoving$afterRender(AbstractClientPlayer player, double x, double y, double z,
            float yaw, float partialTicks, CallbackInfo ci) {
        smartMoving$getRenderer().afterDoRender(player);
    }

    // RenderPlayer declares applyRotations and renderLivingAt; renderName is inherited.
    @ModifyVariable(method = "applyRotations(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float smartMoving$bodyRotation(float bodyYaw, AbstractClientPlayer player, float age,
            float originalBodyYaw, float partialTicks) {
        return smartMoving$getRenderer().beforeRotateCorpse(player, age, bodyYaw, partialTicks);
    }

    @ModifyVariable(method = "renderLivingAt(Lnet/minecraft/client/entity/AbstractClientPlayer;DDD)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private double smartMoving$renderLivingY(double y, AbstractClientPlayer player) {
        return smartMoving$getRenderer().modifyLivingY(player, y);
    }
}
