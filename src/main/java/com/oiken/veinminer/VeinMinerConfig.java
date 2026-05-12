package com.oiken.veinminer;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class VeinMinerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_VEIN_SIZE = BUILDER
            .comment("Maximum number of blocks that can be broken in a single vein mine operation.",
                     "The mod will break as many blocks as durability allows, up to this cap.",
                     "Default: 64")
            .defineInRange("maxVeinSize", 64, 1, 1024);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
