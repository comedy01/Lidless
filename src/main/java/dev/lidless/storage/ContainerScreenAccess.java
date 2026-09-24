package dev.lidless.storage;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.inventory.Slot;

public interface ContainerScreenAccess {
    int lidless$leftPos();

    int lidless$topPos();

    int lidless$titleLabelX();

    Slot lidless$hoveredSlot();

    void lidless$addWidget(AbstractWidget widget);

    StorageControls lidless$controls();
}
