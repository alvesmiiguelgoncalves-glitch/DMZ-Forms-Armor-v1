package com.dmzssj4armors.client;

import com.dmzssj4armors.SsjArmorConfig;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Progresso (0..1) da transicao da roupa de cada jogador, no mesmo espirito do
 * {@code BodyLayerFadeTracker} que o DMZ usa para a pelagem do SSJ4.
 *
 * <p>Avanca uma vez por tick de jogo. Jogador que aparece ja na forma, ou que ficou muito
 * tempo sem ser desenhado, entra direto no estado final (sem "fade" do nada).</p>
 */
public final class FadeTracker {

    private static final int SNAP_AFTER_TICKS = 40;
    private static final int MAX_STATES = 256;

    private static final class State {
        float progress;
        long lastTick;
        boolean target;
    }

    private static final Map<Integer, State> STATES = new HashMap<>();

    private FadeTracker() {
    }

    /** @return 0 = roupa normal, 1 = roupa da forma, entre os dois = transicao. */
    public static float progress(AbstractClientPlayer player) {
        long now = player.level().getGameTime();
        State state = STATES.get(player.getId());

        if (state == null) {
            if (STATES.size() >= MAX_STATES) prune(now);
            state = new State();
            state.target = FormMatcher.isTargetForm(player);
            state.progress = state.target ? 1.0f : 0.0f;
            state.lastTick = now;
            STATES.put(player.getId(), state);
            return state.progress;
        }

        long elapsed = now - state.lastTick;
        if (elapsed == 0) return state.progress;

        state.lastTick = now;
        state.target = FormMatcher.isTargetForm(player);

        if (elapsed < 0 || elapsed > SNAP_AFTER_TICKS) {
            state.progress = state.target ? 1.0f : 0.0f;
            return state.progress;
        }

        float step = elapsed / (float) SsjArmorConfig.FADE_TICKS.get();
        float goal = state.target ? 1.0f : 0.0f;
        if (state.progress < goal) {
            state.progress = Math.min(goal, state.progress + step);
        } else if (state.progress > goal) {
            state.progress = Math.max(goal, state.progress - step);
        }
        return state.progress;
    }

    public static void clear() {
        STATES.clear();
    }

    private static void prune(long now) {
        Iterator<State> it = STATES.values().iterator();
        while (it.hasNext()) {
            long age = now - it.next().lastTick;
            if (age < 0 || age > 200) it.remove();
        }
        if (STATES.size() >= MAX_STATES) STATES.clear();
    }
}
