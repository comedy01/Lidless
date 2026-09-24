package dev.lidless.selftest.neoforge;

import dev.lidless.selftest.LidlessSelfTest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "lidless_selftest", dist = Dist.CLIENT)
public final class LidlessSelfTestNeoForge {
    public LidlessSelfTestNeoForge() {
        LidlessSelfTest test = new LidlessSelfTest();
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> test.tick());
    }
}
