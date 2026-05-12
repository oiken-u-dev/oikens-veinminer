package com.oiken.veinminer.client;

import com.oiken.veinminer.VeinMiner;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = VeinMiner.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

    /** Press V (rebindable) to toggle vein mining on/off. */
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.veinminer.toggle",          // translation key
            GLFW.GLFW_KEY_V,                 // default: V
            "key.categories.veinminer"       // shown in Controls menu
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }
}
