package com.oiken.veinminer.network;

import com.oiken.veinminer.VeinMinerEvents;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class VeinMinerNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(
                com.oiken.veinminer.VeinMiner.MODID
        );

        registrar.playToServer(
                TogglePacket.TYPE,
                TogglePacket.STREAM_CODEC,
                // Runs on the main server thread
                (payload, context) -> context.enqueueWork(() ->
                        VeinMinerEvents.setEnabled(context.player().getUUID(), payload.enabled())
                )
        );
    }
}
