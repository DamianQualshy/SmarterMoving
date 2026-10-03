package net.smart.moving.mixin.server;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.smart.moving.IEntityPlayerMP;
import net.smart.moving.SMServer;
import net.smart.utilities.SoundUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP implements IEntityPlayerMP {
    @Unique private SMServer smartMoving$controller;

    @Unique
    private EntityPlayerMP smartMoving$self() {
        return (EntityPlayerMP) (Object) this;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void smartMoving$construct(CallbackInfo ci) {
        smartMoving$controller = new SMServer(this, false);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void smartMoving$beforeUpdate(CallbackInfo ci) {
        smartMoving$controller.beforeOnUpdate();
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void smartMoving$afterUpdate(CallbackInfo ci) {
        smartMoving$controller.afterOnUpdate();
    }

    @Override
    public SMServer getMoving() { return smartMoving$controller; }

    @Override
    public EntityPlayerMP getEntityPlayerMP() { return smartMoving$self(); }

    @Override
    public void sendPacket(IMessage message) {
        net.smart.moving.SMPacketHandler.INSTANCE.sendTo(message, smartMoving$self());
    }

    @Override
    public void sendPacketToTrackedPlayers(FMLProxyPacket packet) {
        EntityPlayerMP player = smartMoving$self();
        player.mcServer.getWorld(player.dimension).getEntityTracker().sendToTracking(player, packet);
    }

    @Override
    public String getUsername() { return smartMoving$self().getGameProfile().getName(); }

    @Override
    public void resetFallDistance() {
        EntityPlayerMP player = smartMoving$self();
        player.fallDistance = 0;
        player.motionY = 0.08;
    }

    @Override
    public void resetTicksForFloatKick() {
        ((NetHandlerPlayServerAccessor) smartMoving$self().connection).smartMoving$setFloatingTickCount(0);
    }

    @Override
    public void setHeight(float height) { smartMoving$self().height = height; }

    @Override
    public float getHeight() { return smartMoving$self().height; }

    @Override
    public double getMinY() { return smartMoving$self().getEntityBoundingBox().minY; }

    @Override
    public void setMaxY(double maxY) {
        EntityPlayerMP player = smartMoving$self();
        AxisAlignedBB box = player.getEntityBoundingBox();
        player.setEntityBoundingBox(new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, maxY, box.maxZ));
    }

    @Override
    public float doGetHealth() { return smartMoving$self().getHealth(); }

    @Override
    public AxisAlignedBB getBox() { return smartMoving$self().getEntityBoundingBox(); }

    @Override
    public AxisAlignedBB expandBox(AxisAlignedBB box, double x, double y, double z) {
        return box.expand(x, y, z);
    }

    @Override
    public List<?> getEntitiesExcludingPlayer(AxisAlignedBB box) {
        EntityPlayerMP player = smartMoving$self();
        return player.world.getEntitiesWithinAABBExcludingEntity(player, box);
    }

    @Override
    public boolean isDeadEntity(Entity entity) { return entity.isDead; }

    @Override
    public void onCollideWithPlayer(Entity entity) { entity.onCollideWithPlayer(smartMoving$self()); }

    @Override
    public void localAddExhaustion(float exhaustion) {
        smartMoving$self().getFoodStats().addExhaustion(exhaustion);
    }

    @Override
    public void localPlaySound(String soundId, float volume, float pitch) {
        SoundEvent event = SoundUtil.getSoundEvent(soundId);
        if (event != null) smartMoving$self().playSound(event, volume, pitch);
    }

}
