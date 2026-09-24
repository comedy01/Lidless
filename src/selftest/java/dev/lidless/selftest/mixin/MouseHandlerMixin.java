package dev.lidless.selftest.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerMixin {
    @Accessor("xpos")
    void lidless$setX(double x);

    @Accessor("ypos")
    void lidless$setY(double y);
}
