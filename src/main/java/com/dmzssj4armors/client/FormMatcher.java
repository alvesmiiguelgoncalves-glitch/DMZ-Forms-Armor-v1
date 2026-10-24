package com.dmzssj4armors.client;

import com.dmzssj4armors.SsjArmorConfig;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.client.player.AbstractClientPlayer;

/** Decide se o jogador esta numa forma que usa a roupa alternativa. */
public final class FormMatcher {

    private FormMatcher() {
    }

    public static boolean isTargetForm(AbstractClientPlayer player) {
        StatsData stats = StatsProvider.get(StatsCapability.INSTANCE, player).orElse(null);
        if (stats == null) return false;

        var character = stats.getCharacter();
        if (character == null) return false;

        // SSJ4 do GT e do Daima: o DMZ usa os modelos "ssj4gt" e "ssj4d" (mesmo criterio da pelagem).
        String logicKey = character.getRenderLogicKey();
        if (logicKey != null && (logicKey.contains("ssj4gt") || logicKey.contains("ssj4d"))) {
            return true;
        }

        // Demais formas (SSJ Full Power etc.) pela lista da config.
        String form = safe(character.getActiveForm());
        if (form.isEmpty()) return false;
        String id = safe(character.getActiveFormGroup()) + ":" + form;
        for (String entry : SsjArmorConfig.FORMS.get()) {
            if (entry.equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
