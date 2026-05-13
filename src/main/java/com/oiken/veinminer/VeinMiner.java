package com.oiken.veinminer;

import com.oiken.veinminer.client.ClientSetup;
import com.oiken.veinminer.network.VeinMinerNetwork;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(VeinMiner.MODID)
public class VeinMiner {

    public static final String MODID = "archveinminer_neof";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VeinMiner(IEventBus modEventBus, ModContainer modContainer) {
        // Register network packets
        modEventBus.addListener(VeinMinerNetwork::register);

        // Register config
        VeinMinerConfig.register(modContainer);

        // Only load client classes on the client — prevents crashes on dedicated servers
        if (FMLEnvironment.dist == Dist.CLIENT) {
            new ClientSetup(modEventBus);
        }
    }
}