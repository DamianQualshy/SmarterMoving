package net.smart.moving.mixin.client;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.smart.moving.IEntityPlayerSP;
import net.smart.moving.SMSelf;
import net.smart.moving.render.ISmartMovingRenderState;
import net.smart.moving.render.MovingRenderData;
import net.smart.moving.render.MovingStatistics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer implements ISmartMovingRenderState {
    @Unique private MovingStatistics smartMoving$statistics;
    @Unique private MovingRenderData smartMoving$renderData;

    @Override
    public MovingStatistics smartMoving$getStatistics() {
        if (smartMoving$statistics == null)
            smartMoving$statistics = new MovingStatistics((AbstractClientPlayer) (Object) this);
        return smartMoving$statistics;
    }

    @Override
    public MovingRenderData smartMoving$getRenderData() {
        if (smartMoving$renderData == null)
            smartMoving$renderData = new MovingRenderData();
        return smartMoving$renderData;
    }
    @Redirect(method = "getFovModifier", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;getAttributeValue()D"))
    private double smartMoving$fovMovementSpeed(IAttributeInstance attribute) {
        double speed = attribute.getAttributeValue();
        if ((Object) this instanceof EntityPlayerSP) {
            SMSelf controller = (SMSelf) ((IEntityPlayerSP) this).getMoving();
            if (controller != null)
                return controller.getFovMovementSpeed(speed);
        }
        return speed;
    }
}
