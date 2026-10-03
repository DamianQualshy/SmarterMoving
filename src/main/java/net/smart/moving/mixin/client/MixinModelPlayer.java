package net.smart.moving.mixin.client;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.smart.moving.model.IModelPlayer;
import net.smart.moving.model.SMModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public abstract class MixinModelPlayer {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void smartMoving$render(Entity entity, float limbSwing, float limbSwingAmount, float age,
            float headYaw, float headPitch, float scale, CallbackInfo ci) {
        SMModel model = ((IModelPlayer) this).getMovingModelIfPresent();
        if (model != null && !model.md.isInventory
                && !(entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isElytraFlying())) {
            model.md.render(entity, limbSwing, limbSwingAmount, age, headYaw, headPitch, scale);
            ci.cancel();
        }
    }

    @Inject(method = "setRotationAngles", at = @At("HEAD"), cancellable = true)
    private void smartMoving$angles(float limbSwing, float limbSwingAmount, float age, float headYaw,
            float headPitch, float scale, Entity entity, CallbackInfo ci) {
        SMModel model = ((IModelPlayer) this).getMovingModelIfPresent();
        if (model != null && !model.md.isInventory
                && !(entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isElytraFlying())) {
            model.md.setRotationAngles(limbSwing, limbSwingAmount, age, headYaw, headPitch, scale, entity);
            ci.cancel();
        }
    }
}
