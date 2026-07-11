package com.andimarius.chain;

import com.andimarius.chain.network.ChainNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Entry-point-ul modului.
 * Tot ce este global (rețea, evenimente server/client) pornește din acest punct.
 */
@Mod(ChainedPlayersMod.MOD_ID)
public class ChainedPlayersMod {
    public static final String MOD_ID = "chainlink";

    public ChainedPlayersMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ChainNetwork::register);

        // Evenimentele de gameplay (comenzi, tick-uri, cleanup) sunt pe bus-ul Forge.
        MinecraftForge.EVENT_BUS.register(new ChainServerEvents());
    }
}
