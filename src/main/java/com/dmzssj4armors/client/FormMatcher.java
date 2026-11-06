package com.dmzssj4armors.client;

import com.dmzssj4armors.FormsConfig;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.client.player.AbstractClientPlayer;

import javax.annotation.Nullable;

/** Decide qual conjunto de roupas ("perfil") o jogador usa na forma atual. */
public final class FormMatcher {

    private FormMatcher() {
    }

    /**
     * @return {@code null} = roupa original do DMZ; {@code ""} = roupa padrao do addon (a do SSJ4);
     *         qualquer outro texto = nome da pasta de texturas dentro de {@code textures/armor/}.
     */
    @Nullable
    public static String profileFor(AbstractClientPlayer player) {
        StatsData stats = StatsProvider.get(StatsCapability.INSTANCE, player).orElse(null);
        if (stats == null) return null;

        var character = stats.getCharacter();
        if (character == null) return null;

        // 1) Lista de formas do config (chaves no padrao do lang do DMZ).
        String form = safe(character.getActiveForm());
        if (!form.isEmpty()) {
            FormsConfig.Entry listed = FormsConfig.lookup(safe(character.getRaceName()), safe(character.getActiveFormGroup()), form);
            if (listed != null) return listed.enabled() ? listed.profile() : null;
        }

        // 2) Forma que nao esta na lista: SSJ4 do GT e do Daima e detectado pelo modelo
        // ("ssj4gt" e "ssj4d", mesmo criterio da pelagem do DMZ) e usa a roupa padrao.
        String logicKey = character.getRenderLogicKey();
        boolean ssj4 = logicKey != null && (logicKey.contains("ssj4gt") || logicKey.contains("ssj4d"));
        return ssj4 ? "" : null;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
