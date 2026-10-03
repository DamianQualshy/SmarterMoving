package net.smart.moving.mixin.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LayerArmorBase.class)
public interface LayerArmorBaseAccessor {
    @Accessor("modelArmor")
    ModelBase smartMoving$getModelArmor();

    @Accessor("modelLeggings")
    ModelBase smartMoving$getModelLeggings();
}
