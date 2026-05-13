package com.oiken.veinminer.client;

import net.neoforged.bus.api.IEventBus;

/**
 * Handles all client-side mod-bus event registration.
 * Only instantiated on the client side (see VeinMiner.java).
 */
public class ClientSetup {

    public ClientSetup(IEventBus modEventBus) {
        modEventBus.addListener(KeyBindings::onRegisterKeyMappings);
    }
}