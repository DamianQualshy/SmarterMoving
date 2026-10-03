package net.smart.moving.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import net.smart.moving.IEntityPlayerSP;
import net.smart.moving.SMSelf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer implements IEntityPlayerSP {
    @Unique private SMSelf smartMoving$controller;
    @Unique private SMSelf.ActionStateStart smartMoving$actionStateStart;

    protected MixinEntityPlayerSP(World world, GameProfile profile) {
        super(world, profile);
    }

    @Unique
    private EntityPlayerSP smartMoving$self() {
        return (EntityPlayerSP) (Object) this;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void smartMoving$construct(CallbackInfo ci) {
        smartMoving$controller = new SMSelf(smartMoving$self(), this);
    }

    @Override
    public SMSelf getMoving() { return smartMoving$controller; }

    @Inject(method = "move", at = @At("HEAD"))
    private void smartMoving$beforeMove(MoverType mover, double x, double y, double z, CallbackInfo ci) {
        if (smartMoving$controller != null && !isElytraFlying())
            smartMoving$controller.beforeMoveEntity(x, y, z);
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void smartMoving$afterMove(MoverType mover, double x, double y, double z, CallbackInfo ci) {
        if (smartMoving$controller != null && !isElytraFlying())
            smartMoving$controller.afterMoveEntity(x, y, z);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void smartMoving$beforeUpdate(CallbackInfo ci) {
        smartMoving$controller.beforeOnUpdate();
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void smartMoving$afterUpdate(CallbackInfo ci) {
        smartMoving$controller.afterOnUpdate();
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void smartMoving$beforeLivingUpdate(CallbackInfo ci) {
        smartMoving$controller.beforeOnLivingUpdate();
    }

    @Inject(method = "onLivingUpdate", at = @At("RETURN"))
    private void smartMoving$afterLivingUpdate(CallbackInfo ci) {
        smartMoving$controller.afterOnLivingUpdate();
    }

    @Inject(method = "updateEntityActionState", at = @At("HEAD"))
    private void smartMoving$beforeActionState(CallbackInfo ci) {
        if (smartMoving$controller != null)
            smartMoving$actionStateStart = smartMoving$controller.beforeUpdateEntityActionState();
    }

    @Inject(method = "updateEntityActionState", at = @At("RETURN"))
    private void smartMoving$afterActionState(CallbackInfo ci) {
        if (smartMoving$actionStateStart != null) {
            smartMoving$controller.afterUpdateEntityActionState(false, smartMoving$actionStateStart);
            smartMoving$actionStateStart = null;
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void smartMoving$pushOutOfBlocks(double x, double y, double z,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(smartMoving$controller.pushOutOfBlocks(x, y, z));
    }

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    private void smartMoving$isSneaking(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(smartMoving$controller.isSneaking());
    }

    @Override
    public boolean getSleepingField() {
        return ((EntityPlayerAccessor) this).smartMoving$isSleeping();
    }

    @Override
    public boolean getIsJumpingField() {
        return ((EntityLivingBaseAccessor) this).smartMoving$isJumping();
    }

    @Override
    public boolean getIsInWebField() {
        return ((EntityAccessor) this).smartMoving$isInWeb();
    }

    @Override
    public void setIsInWebField(boolean inWeb) {
        ((EntityAccessor) this).smartMoving$setInWeb(inWeb);
    }

    @Override
    public Minecraft getMcField() {
        return ((EntityPlayerSPAccessor) this).smartMoving$getMinecraft();
    }

    @Override
    public void setMoveForwardField(float value) { moveForward = value; }

    @Override
    public void setMoveStrafingField(float value) { moveStrafing = value; }

    @Override
    public void setIsJumpingField(boolean jumping) {
        ((EntityLivingBaseAccessor) this).smartMoving$setJumping(jumping);
    }

    @Override
    public boolean localIsSneaking() {
        return smartMoving$self().movementInput != null
                && smartMoving$self().movementInput.sneak && !getSleepingField();
    }
}
