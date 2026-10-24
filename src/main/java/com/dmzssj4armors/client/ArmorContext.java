package com.dmzssj4armors.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Guarda "quem esta sendo renderizado agora".
 *
 * <p>O {@code ArmorTextureResolver} do DragonMine Z nao recebe o jogador, entao os mixins
 * empilham o jogador antes de a armadura ser desenhada e desempilham no fim. Tudo roda na
 * thread de render, por isso nao precisa de sincronizacao.</p>
 */
public final class ArmorContext {

    private static final int MAX_DEPTH = 32;
    private static final AbstractClientPlayer[] STACK = new AbstractClientPlayer[MAX_DEPTH];
    private static int depth = 0;

    private ArmorContext() {
    }

    /** Empilha o dono da armadura. Entidades que nao sao jogadores entram como {@code null}. */
    public static void push(@Nullable Entity entity) {
        if (depth >= MAX_DEPTH) {
            // Algum push ficou sem pop (excecao no meio do render). Recomeca limpo.
            reset();
        }
        STACK[depth++] = entity instanceof AbstractClientPlayer player ? player : null;
    }

    public static void pop() {
        if (depth > 0) {
            STACK[--depth] = null;
        }
    }

    @Nullable
    public static AbstractClientPlayer current() {
        return depth == 0 ? null : STACK[depth - 1];
    }

    public static void reset() {
        Arrays.fill(STACK, null);
        depth = 0;
    }
}
