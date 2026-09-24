package dev.lidless.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.lidless.client.gui.LidlessSettingsScreen;
import net.minecraft.client.Minecraft;

public final class LidlessModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new LidlessSettingsScreen(parent, Minecraft.getInstance().options);
    }
}
