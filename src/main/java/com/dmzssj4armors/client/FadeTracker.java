package com.dmzssj4armors.client;

import com.dmzssj4armors.SsjArmorConfig;
import net.minecraft.client.player.AbstractClientPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Transicao da roupa de cada jogador, no mesmo espirito do {@code BodyLayerFadeTracker} que o DMZ
 * usa para a pelagem do SSJ4.
 *
 * <p>Cada jogador tem um par {@code from -> to} de perfis ({@code null} = roupa original,
 * {@code ""} = padrao do addon, outro texto = pasta) e um progresso de 0 a 1 entre os dois.
 * Avanca uma vez por tick de jogo. Se a forma muda no meio da transicao, o par e ajustado
 * (voltar para o perfil anterior inverte a animacao). Jogador que aparece ja numa forma, ou que
 * ficou muito tempo sem ser desenhado, entra direto no estado final.</p>
 */
public final class FadeTracker {

    private static final int SNAP_AFTER_TICKS = 40;
    private static final int MAX_STATES = 256;

    /** Estado de um jogador. Somente leitura fora desta classe. */
    public static final class Frame {
        @Nullable public String from;
        @Nullable public String to;
        public float progress = 1.0f;
        long lastTick;
    }

    private static final Map<Integer, Frame> STATES = new HashMap<>();

    private FadeTracker() {
    }

    public static Frame frame(AbstractClientPlayer player) {
        long now = player.level().getGameTime();
        Frame frame = STATES.get(player.getId());

        if (frame == null) {
            if (STATES.size() >= MAX_STATES) prune(now);
            frame = new Frame();
            frame.from = frame.to = FormMatcher.profileFor(player);
            frame.lastTick = now;
            STATES.put(player.getId(), frame);
            return frame;
        }

        long elapsed = now - frame.lastTick;
        if (elapsed == 0) return frame;
        frame.lastTick = now;

        String target = FormMatcher.profileFor(player);

        if (elapsed < 0 || elapsed > SNAP_AFTER_TICKS) {
            frame.from = frame.to = target;
            frame.progress = 1.0f;
            return frame;
        }

        if (!Objects.equals(target, frame.to)) {
            if (Objects.equals(target, frame.from) && frame.progress < 1.0f) {
                // Voltou para o perfil de antes no meio da transicao: inverte a animacao.
                frame.from = frame.to;
                frame.to = target;
                frame.progress = 1.0f - frame.progress;
            } else {
                // Forma nova: o que esta na tela agora vira o ponto de partida.
                if (frame.progress >= 0.5f) frame.from = frame.to;
                frame.to = target;
                frame.progress = 0.0f;
            }
        }

        if (frame.progress < 1.0f) {
            frame.progress = Math.min(1.0f, frame.progress + elapsed / (float) SsjArmorConfig.FADE_TICKS.get());
        }
        if (Objects.equals(frame.from, frame.to)) frame.progress = 1.0f;
        return frame;
    }

    public static void clear() {
        STATES.clear();
    }

    private static void prune(long now) {
        Iterator<Frame> it = STATES.values().iterator();
        while (it.hasNext()) {
            long age = now - it.next().lastTick;
            if (age < 0 || age > 200) it.remove();
        }
        if (STATES.size() >= MAX_STATES) STATES.clear();
    }
}
