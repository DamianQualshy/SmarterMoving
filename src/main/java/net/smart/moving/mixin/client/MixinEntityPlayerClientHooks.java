package net.smart.moving.mixin.client;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.smart.moving.IEntityPlayerSP;
import net.smart.moving.SMSelf;
import net.smart.moving.render.ISmartMovingRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerClientHooks {
    @Unique
    private SMSelf smartMoving$controller() {
        return ((IEntityPlayerSP) this).getMoving() instanceof SMSelf
                ? (SMSelf) ((IEntityPlayerSP) this).getMoving() : null;
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void smartMoving$travel(float strafe, float vertical, float forward, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerSP
                && !((EntityPlayerSP) (Object) this).isElytraFlying()) {
            SMSelf controller = smartMoving$controller();
            if (controller != null) {
                controller.moveEntityWithHeading(strafe, forward);
                // Smart Moving replaces this travel call, so the inherited
                // EntityLivingBase RETURN sampler does not run here.
                ((ISmartMovingRenderState) this).smartMoving$getStatistics().calculate(false);
                ci.cancel();
            }
        }
    }

    @Inject(method = "trySleep", at = @At("HEAD"))
    private void smartMoving$beforeSleep(BlockPos pos,
            CallbackInfoReturnable<EntityPlayer.SleepResult> cir) {
        if ((Object) this instanceof EntityPlayerSP)
            smartMoving$controller().beforeSleepInBedAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Inject(method = "getSleepTimer", at = @At("HEAD"))
    private void smartMoving$beforeGetSleepTimer(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof EntityPlayerSP)
            smartMoving$controller().beforeGetSleepTimer();
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void smartMoving$jump(CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerSP) {
            smartMoving$controller().jump();
            ci.cancel();
        }
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    private void smartMoving$afterWrite(NBTTagCompound tag, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerSP)
            smartMoving$controller().afterWriteEntityToNBT(tag);
    }

    @Inject(method = "canTriggerWalking", at = @At("HEAD"), cancellable = true)
    private void smartMoving$canTriggerWalking(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof EntityPlayerSP && smartMoving$controller() != null)
            cir.setReturnValue(smartMoving$controller().canTriggerWalking());
    }
}
