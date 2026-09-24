package dev.lidless.selftest.forge;

import dev.lidless.selftest.LidlessSelfTest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("lidless_selftest")
public final class LidlessSelfTestForge {
    public LidlessSelfTestForge() {
        LidlessSelfTest test = new LidlessSelfTest();
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                test.tick();
            }
        });
    }
}
