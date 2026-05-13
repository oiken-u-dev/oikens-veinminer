package com.oiken.veinminer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

@EventBusSubscriber(modid = VeinMiner.MODID)
public class VeinMinerEvents {

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    /** Players currently mid-vein-mine — prevents recursive event firing. */
    private static final Set<UUID> activeMiners = new HashSet<>();

    /** Per-player toggle (default ON). */
    private static final Map<UUID, Boolean> toggleState = new HashMap<>();

    public static boolean isEnabled(UUID playerId) {
        return toggleState.getOrDefault(playerId, true);
    }

    public static void setEnabled(UUID playerId, boolean enabled) {
        toggleState.put(playerId, enabled);
    }

    // -----------------------------------------------------------------------
    // Event
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // Server-side only
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();

        // Re-entry guard — skip blocks we're breaking ourselves
        if (activeMiners.contains(playerId)) return;

        // Toggle check
        if (!isEnabled(playerId)) return;

        // Must be crouching to activate
        if (!player.isCrouching()) return;

        BlockPos origin = event.getPos();
        BlockState originState = level.getBlockState(origin);

        // Determine mining mode
        boolean isOre = originState.is(Tags.Blocks.ORES)
        || originState.is(net.minecraft.core.registries.BuiltInRegistries.BLOCK
            .get(net.minecraft.resources.ResourceLocation.parse("expandeddelight:salt_ore")))
        || originState.is(net.minecraft.core.registries.BuiltInRegistries.BLOCK
            .get(net.minecraft.resources.ResourceLocation.parse("expandeddelight:deepslate_salt_ore")));
        boolean isLog = originState.is(BlockTags.LOGS);

        if (!isOre && !isLog) return;

        // --- Work out how many blocks we can actually break ---
        ItemStack tool = player.getMainHandItem();
        int cap = VeinMinerConfig.MAX_VEIN_SIZE.get();

        int maxBreakable;
        if (tool.isEmpty() || tool.getMaxDamage() == 0 || player.isCreative()) {
            // Unbreakable / no tool / creative — only cap applies
            maxBreakable = cap;
        } else {
            int remainingDurability = tool.getMaxDamage() - tool.getDamageValue();
            maxBreakable = Math.min(remainingDurability, cap);
        }

        if (maxBreakable <= 0) return;

        // Find the vein (BFS)
        List<BlockPos> vein = findVein(level, origin, originState, isLog, maxBreakable);

        // Single block? Let vanilla handle it normally, nothing special to do.
        if (vein.size() <= 1) return;

        // --- Take over from vanilla ---
        event.setCanceled(true);
        activeMiners.add(playerId);

        try {
            for (int i = 0; i < vein.size(); i++) {
                BlockPos pos = vein.get(i);

                // Break with full tool-drop support (fortune / silk touch aware)
                breakBlockAsPlayer(level, pos, player);

                // Apply one durability per extra block (skip creative / unbreakable tools)
                if (!tool.isEmpty() && tool.getMaxDamage() > 0 && !player.isCreative()) {
                    tool.hurtAndBreak(1, level, player,
                            item -> player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                    if (tool.isEmpty()) break; // tool just broke — stop here
                }
            }
        } finally {
            activeMiners.remove(playerId);
        }

        // Play the break sound exactly once, at the origin block position
        var sound = originState.getSoundType(level, origin, player);
        level.playSound(null, origin, sound.getBreakSound(), SoundSource.BLOCKS,
                sound.getVolume(), sound.getPitch());
    }

    // -----------------------------------------------------------------------
    // BFS
    // -----------------------------------------------------------------------

    private static List<BlockPos> findVein(ServerLevel level, BlockPos origin,
                                           BlockState originState, boolean isLog, int maxSize) {
        List<BlockPos> result   = new ArrayList<>();
        Set<BlockPos>  visited  = new HashSet<>();
        Queue<BlockPos> queue   = new ArrayDeque<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && result.size() < maxSize) {
            BlockPos current = queue.poll();
            result.add(current);

            for (BlockPos neighbor : neighbors(current, isLog)) {
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);

                BlockState neighborState = level.getBlockState(neighbor);
                boolean matches = isLog
                        // Trees: any connected log counts (handles multi-wood modded trees)
                        ? neighborState.is(BlockTags.LOGS)
                        // Ores: must be the exact same block (mine THIS ore's vein only)
                        : neighborState.is(originState.getBlock());

                if (matches) queue.add(neighbor);
            }
        }

        return result;
    }

    /**
     * For ores we check all 26 neighbours (face + edge + corner) because ore
     * veins generate diagonally.  For logs we only need the 6 face neighbours
     * since trunks grow in straight lines.
     */
    private static Iterable<BlockPos> neighbors(BlockPos pos, boolean isLog) {
        List<BlockPos> list = new ArrayList<>();
        if (isLog) {
            list.add(pos.north()); list.add(pos.south());
            list.add(pos.east());  list.add(pos.west());
            list.add(pos.above()); list.add(pos.below());
        } else {
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++)
                        if (dx != 0 || dy != 0 || dz != 0)
                            list.add(pos.offset(dx, dy, dz));
        }
        return list;
    }

    // -----------------------------------------------------------------------
    // Block-break helper — replicates ServerPlayerGameMode.destroyBlock()
    // so that fortune / silk touch drops work correctly.
    // -----------------------------------------------------------------------

    private static void breakBlockAsPlayer(ServerLevel level, BlockPos pos, ServerPlayer player) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        BlockEntity be   = level.getBlockEntity(pos);
        Block        block = state.getBlock();
        ItemStack    tool  = player.getMainHandItem();

        // Pre-break hook (particles, sounds that some blocks fire here — kept for mod compat)
        block.playerWillDestroy(level, pos, state, player);

        // Remove the block from the world
        if (level.removeBlock(pos, false)) {
            block.destroy(level, pos, state);

            // Drop loot using the actual held tool (respects fortune / silk touch)
            if (!player.isCreative()) {
                block.playerDestroy(level, player, pos, state, be, tool.copy());
            }
        }
    }
}
