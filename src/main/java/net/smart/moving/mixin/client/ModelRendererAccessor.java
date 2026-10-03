package net.smart.moving.mixin.client;

import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelRenderer.class)
public interface ModelRendererAccessor {
    @Accessor("compiled") boolean smartMoving$isCompiled();
    @Accessor("displayList") int smartMoving$getDisplayList();
    @Invoker("compileDisplayList") void smartMoving$compileDisplayList(float scale);
}
