package com.oiken.veinminer.client;

import com.oiken.veinminer.VeinMiner;
import com.oiken.veinminer.network.TogglePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = VeinMiner.MODID, value = Dist.CLIENT)
public class ClientEvents {

    /** Mirrors the server-side toggle so we can show the right HUD message instantly. */
    private static boolean veinMinerEnabled = true;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // consumeClick() returns true once per physical press, ignoring held/repeat
        if (!KeyBindings.TOGGLE_KEY.consumeClick()) return;

        veinMinerEnabled = !veinMinerEnabled;

        // Notify the server
        PacketDistributor.sendToServer(new TogglePacket(veinMinerEnabled));

        // Show a hotbar-style toast so the player knows the current state
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            String status = veinMinerEnabled
                    ? "§a✔ Vein Miner ON §7(crouch while mining)"
                    : "§c✘ Vein Miner OFF";
            mc.player.displayClientMessage(
                    Component.literal("§6[VeinMiner] §r" + status),
                    true   // true = action-bar / hotbar message (not chat)
            );
        }
    }
}
