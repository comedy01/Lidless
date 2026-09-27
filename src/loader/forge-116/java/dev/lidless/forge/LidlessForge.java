package dev.lidless.forge;

import dev.lidless.client.LidlessClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

@Mod(LidlessClient.MOD_ID)
public final class LidlessForge {
    public LidlessForge() {
        ModLoadingContext context = ModLoadingContext.get();
        context.registerExtensionPoint(
                ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (remote, fromServer) -> true));
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        LidlessClient.init(FMLPaths.CONFIGDIR.get());
        ForgeClient.register(context, FMLJavaModLoadingContext.get().getModEventBus());
    }
}
