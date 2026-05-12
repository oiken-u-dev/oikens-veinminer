package com.oiken.veinminer;

import com.oiken.veinminer.client.KeyBindings;
import com.oiken.veinminer.network.VeinMinerNetwork;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
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

        // VeinMinerEvents, KeyBindings, and ClientEvents are auto-registered
        // via their @EventBusSubscriber annotations.
    }
}
