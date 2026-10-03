package net.smart.moving.mixin.client;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPlayer.class)
public interface ModelPlayerAccessor {
    @Accessor("smallArms") boolean smartMoving$isSmallArms();
    @Accessor("bipedCape") ModelRenderer smartMoving$getCape();
    @Mutable @Accessor("bipedCape") void smartMoving$setCape(ModelRenderer model);
    @Accessor("bipedDeadmau5Head") ModelRenderer smartMoving$getDeadmau5Head();
    @Mutable @Accessor("bipedDeadmau5Head") void smartMoving$setDeadmau5Head(ModelRenderer model);
}
