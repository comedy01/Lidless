package dev.lidless.showcase.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Accessor("xpos")
    void lidlessShowcase$setX(double x);

    @Accessor("ypos")
    void lidlessShowcase$setY(double y);
}
