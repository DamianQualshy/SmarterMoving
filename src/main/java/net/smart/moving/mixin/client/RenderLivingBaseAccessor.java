package net.smart.moving.mixin.client;

import java.util.List;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderLivingBase.class)
public interface RenderLivingBaseAccessor {
    @Accessor("layerRenderers")
    List<LayerRenderer<?>> smartMoving$getLayerRenderers();
}
