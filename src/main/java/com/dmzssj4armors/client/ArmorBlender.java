package com.dmzssj4armors.client;

import com.dmzssj4armors.DmzSsj4Armors;
import com.dmzssj4armors.SsjArmorConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Escolhe a textura da armadura para o jogador.
 *
 * <p>A roupa padrao fica neste addon em {@code dmz_ssj4_armors:textures/armor/<mesmo nome>.png}; outros
 * conjuntos ("perfis") ficam em {@code dmz_ssj4_armors:textures/armor/<perfil>/<mesmo nome>.png}, no
 * addon ou em qualquer resource pack.
 * Para a transicao, em vez de desenhar duas camadas, geramos texturas intermediarias (mistura
 * das duas, em {@link #STEPS} passos) e devolvemos a do passo atual. Assim o DMZ continua
 * desenhando a armadura do jeito dele e a transicao funciona em todos os caminhos de render
 * (corpo, camada customizada, mao em primeira pessoa, capa).</p>
 */
public final class ArmorBlender {

    public static final int STEPS = 12;
    private static final int MAX_BLENDS = 1024;
    private static final String DMZ_NAMESPACE = "dragonminez";
    private static final String ARMOR_DIR = "textures/armor/";

    private static final Map<ResourceLocation, Boolean> VARIANT_EXISTS = new HashMap<>();
    private static final Map<String, ResourceLocation> BLENDS = new HashMap<>();
    private static final Set<ResourceLocation> DYNAMIC_TEXTURES = new HashSet<>();
    private static final Set<String> FAILED = new HashSet<>();

    private ArmorBlender() {
    }

    /**
     * @param original textura que o DMZ escolheu (ja considera roupa danificada etc.)
     * @return a mesma textura, a roupa do perfil da forma, ou uma mistura de duas
     */
    public static ResourceLocation apply(AbstractClientPlayer player, ResourceLocation original) {
        if (!SsjArmorConfig.ENABLED.get()) return original;
        if (!DMZ_NAMESPACE.equals(original.getNamespace())) return original;

        String path = original.getPath();
        if (!path.startsWith(ARMOR_DIR)) return original;

        FadeTracker.Frame frame = FadeTracker.frame(player);
        ResourceLocation from = resolve(original, path, frame.from);
        ResourceLocation to = resolve(original, path, frame.to);
        if (from.equals(to)) return from;

        int step = Math.round(frame.progress * STEPS);
        if (step <= 0) return from;
        if (step >= STEPS) return to;
        return blended(from, to, step);
    }

    /** Textura do perfil para esta peca; se o perfil nao tem a peca, vale a original do DMZ. */
    private static ResourceLocation resolve(ResourceLocation original, String path, @Nullable String profile) {
        if (profile == null) return original;
        String rest = path.substring(ARMOR_DIR.length());
        String variantPath = profile.isEmpty() ? path : ARMOR_DIR + profile + "/" + rest;
        ResourceLocation variant = new ResourceLocation(DmzSsj4Armors.MOD_ID, variantPath);
        return variantExists(variant) ? variant : original;
    }

    /** Chamado quando os resource packs recarregam: descarta caches e texturas geradas. */
    public static void clear() {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation location : DYNAMIC_TEXTURES) {
            textures.release(location);
        }
        DYNAMIC_TEXTURES.clear();
        BLENDS.clear();
        VARIANT_EXISTS.clear();
        FAILED.clear();
        FadeTracker.clear();
        ArmorContext.reset();
    }

    private static boolean variantExists(ResourceLocation variant) {
        return VARIANT_EXISTS.computeIfAbsent(variant,
                location -> Minecraft.getInstance().getResourceManager().getResource(location).isPresent());
    }

    private static ResourceLocation blended(ResourceLocation original, ResourceLocation variant, int step) {
        String key = original + "|" + variant + "#" + step;
        ResourceLocation cached = BLENDS.get(key);
        if (cached != null) return cached;

        ResourceLocation result;
        if (BLENDS.size() >= MAX_BLENDS || FAILED.contains(key) || !RenderSystem.isOnRenderThread()) {
            result = hardSwitch(original, variant, step);
            // Nao guarda: se foi so falta de thread/limite, tenta de novo depois.
            return result;
        }

        result = createBlend(original, variant, step, key);
        BLENDS.put(key, result);
        return result;
    }

    private static ResourceLocation hardSwitch(ResourceLocation original, ResourceLocation variant, int step) {
        return step * 2 >= STEPS ? variant : original;
    }

    private static ResourceLocation createBlend(ResourceLocation original, ResourceLocation variant, int step, String key) {
        float t = step / (float) STEPS;
        NativeImage output = null;
        try (NativeImage from = read(original); NativeImage to = read(variant)) {
            int width = to.getWidth();
            int height = to.getHeight();
            int fromWidth = from.getWidth();
            int fromHeight = from.getHeight();

            output = new NativeImage(NativeImage.Format.RGBA, width, height, false);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int a = from.getPixelRGBA(x * fromWidth / width, y * fromHeight / height);
                    int b = to.getPixelRGBA(x, y);
                    output.setPixelRGBA(x, y, mix(a, b, t, threshold(x, y)));
                }
            }

            ResourceLocation location = new ResourceLocation(DmzSsj4Armors.MOD_ID,
                    "dynamic/" + sanitize(key) + "_" + Integer.toHexString(key.hashCode()));
            Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(output));
            output = null; // agora e responsabilidade do DynamicTexture
            DYNAMIC_TEXTURES.add(location);
            return location;
        } catch (Exception e) {
            if (FAILED.add(key)) {
                DmzSsj4Armors.LOGGER.warn("Nao foi possivel gerar a transicao de {} (passo {}); usando troca direta.",
                        original, step, e);
            }
            return hardSwitch(original, variant, step);
        } finally {
            if (output != null) output.close();
        }
    }

    private static NativeImage read(ResourceLocation location) throws IOException {
        try (InputStream in = Minecraft.getInstance().getResourceManager().open(location)) {
            return NativeImage.read(in);
        }
    }

    /**
     * Mistura dois pixels ABGR.
     *
     * <p>A armadura do DMZ e desenhada com um render type "cutout": o pixel ou aparece inteiro ou
     * some, nao existe meia transparencia. Por isso um pixel que so existe numa das roupas
     * (a camisa que some no SSJ4, a munhequeira que aparece...) nao pode simplesmente ficar mais
     * ou menos transparente: ele "dissolve", aparecendo/sumindo em pontos espalhados conforme o
     * progresso. O mesmo ponto usa sempre o mesmo limiar, entao ida e volta sao a mesma animacao
     * ao contrario. Pixels que existem nas duas roupas misturam a cor normalmente.</p>
     */
    private static int mix(int a, int b, float t, float threshold) {
        int alphaA = (a >>> 24) & 0xFF;
        int alphaB = (b >>> 24) & 0xFF;

        if (alphaA == 0 && alphaB == 0) return 0;
        if (alphaB == 0) return threshold >= t ? a : 0;   // so na roupa antiga: some quando t passa do limiar
        if (alphaA == 0) return threshold < t ? b : 0;    // so na roupa da forma: aparece quando t passa do limiar

        float weightA = alphaA * (1.0f - t);
        float weightB = alphaB * t;
        float total = weightA + weightB;
        int red = channel(a & 0xFF, b & 0xFF, weightA, weightB, total);
        int green = channel((a >>> 8) & 0xFF, (b >>> 8) & 0xFF, weightA, weightB, total);
        int blue = channel((a >>> 16) & 0xFF, (b >>> 16) & 0xFF, weightA, weightB, total);
        int alpha = Math.round(alphaA * (1.0f - t) + alphaB * t);
        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }

    /** Limiar fixo (0..1) por pixel, espalhado de forma irregular. */
    private static float threshold(int x, int y) {
        int h = x * 0x1F123BB5 + y * 0x5F356495;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        h *= 0x297A2D39;
        h ^= h >>> 15;
        return (h >>> 8) / (float) (1 << 24);
    }

    private static int channel(int a, int b, float weightA, float weightB, float total) {
        int value = Math.round((a * weightA + b * weightB) / total);
        return Math.max(0, Math.min(255, value));
    }

    private static String sanitize(String key) {
        String name = key.toLowerCase(Locale.ROOT)
                .replace("textures/armor/", "")
                .replace(".png", "")
                .replace('#', '_');
        StringBuilder out = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/';
            out.append(ok ? c : '_');
        }
        return out.toString();
    }
}
