package com.dmzssj4armors;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Addon do DragonMine Z: troca a textura das armaduras quando o jogador esta
 * em SSJ4 (GT / Daima) ou SSJ Full Power, com transicao suave.
 *
 * <p>Todo o trabalho acontece no cliente, via mixins (ver pacote {@code mixin}).</p>
 */
@Mod(DmzSsj4Armors.MOD_ID)
public class DmzSsj4Armors {

    public static final String MOD_ID = "dmz_ssj4_armors";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DmzSsj4Armors() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SsjArmorConfig.SPEC, MOD_ID + "-client.toml");
    }
}
