package com.dmzssj4armors;

import net.minecraftforge.common.ForgeConfigSpec;

/** Configuracao do cliente: config/dmz_ssj4_armors-client.toml (as formas ficam em dmz_ssj4_armors-forms.json). */
public final class SsjArmorConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue FADE_TICKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("general");

        ENABLED = builder
                .comment("Liga/desliga a troca de textura das armaduras.")
                .define("enabled", true);

        FADE_TICKS = builder
                .comment("Duracao da transicao entre a roupa normal e a roupa da forma, em ticks (20 ticks = 1 segundo).",
                        "10 e o mesmo ritmo da pelagem do SSJ4 no DragonMine Z.")
                .defineInRange("fadeTicks", 10, 1, 200);

        builder.pop();
        SPEC = builder.build();
    }

    private SsjArmorConfig() {
    }
}
