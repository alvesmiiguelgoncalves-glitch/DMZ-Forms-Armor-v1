package com.dmzssj4armors;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/** Configuracao do cliente: config/dmz_ssj4_armors-client.toml */
public final class SsjArmorConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue FADE_TICKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FORMS;

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

        FORMS = builder
                .comment("Formas que ativam a roupa alternativa, no formato \"grupo:forma\".",
                        "SSJ4 (GT e Daima) tambem e detectado automaticamente pelo modelo da forma.",
                        "Padrao: SSJ4 (grupo oozaru = GT, grupo supersaiyan = Daima) e SSJ Full Power.")
                .defineListAllowEmpty("forms",
                        List.of("oozaru:supersaiyan4", "supersaiyan:supersaiyan4", "legendaryforms:ssjfullpower"),
                        entry -> entry instanceof String s && s.contains(":"));

        builder.pop();
        SPEC = builder.build();
    }

    private SsjArmorConfig() {
    }
}
