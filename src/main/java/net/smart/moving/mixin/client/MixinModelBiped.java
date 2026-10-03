package net.smart.moving.mixin.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.smart.moving.model.IModelPlayer;
import net.smart.moving.model.SMModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped implements IModelPlayer {
    @Unique private SMModel smartMoving$model;

    @Override
    public SMModel getMovingModel() {
        if (smartMoving$model == null)
            smartMoving$model = new SMModel((ModelBiped) (Object) this);
        return smartMoving$model;
    }

    @Override
    public SMModel getMovingModelIfPresent() { return smartMoving$model; }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void smartMoving$render(Entity entity, float limbSwing, float limbSwingAmount, float age,
            float headYaw, float headPitch, float scale, CallbackInfo ci) {
        if (smartMoving$model != null && !smartMoving$model.md.isInventory
                && !(entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isElytraFlying())) {
            smartMoving$model.md.render(entity, limbSwing, limbSwingAmount, age, headYaw, headPitch, scale);
            ci.cancel();
        }
    }

    @Inject(method = "setRotationAngles", at = @At("HEAD"), cancellable = true)
    private void smartMoving$angles(float limbSwing, float limbSwingAmount, float age, float headYaw,
            float headPitch, float scale, Entity entity, CallbackInfo ci) {
        if (smartMoving$model != null && !smartMoving$model.md.isInventory
                && !(entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isElytraFlying())) {
            smartMoving$model.md.setRotationAngles(limbSwing, limbSwingAmount, age, headYaw, headPitch, scale, entity);
            ci.cancel();
        }
    }
}
