package dev.lidless.fabric;

import dev.lidless.tooltip.ContainerPreview;
import dev.lidless.tooltip.ContainerPreviewTooltip;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;

final class TooltipHook {
    private TooltipHook() {
    }

    static void register() {
        ClientTooltipComponentCallback.EVENT.register(data ->
                data instanceof ContainerPreview preview ? new ContainerPreviewTooltip(preview) : null);
    }
}
