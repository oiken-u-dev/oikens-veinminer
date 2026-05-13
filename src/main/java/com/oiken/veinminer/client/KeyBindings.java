package com.oiken.veinminer.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    /** Press V (rebindable) to toggle vein mining on/off. */
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.veinminer.toggle",          // translation key
            GLFW.GLFW_KEY_V,                 // default: V
            "key.categories.veinminer"       // shown in Controls menu
    );

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }
}