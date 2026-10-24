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
 * <p>A roupa da forma fica neste addon em {@code dmz_ssj4_armors:textures/armor/<mesmo nome>.png}.
 * Para a transicao, em vez de desenhar duas camadas, geramos texturas intermediarias (mistura
 * das duas, em {@link #STEPS} passos) e devolvemos a do passo atual. Assim o DMZ continua
 * desenhando a armadura do jeito dele e a transicao funciona em todos os caminhos de render
 * (corpo, camada customizada, mao em primeira pessoa, capa).</p>
 */
public final class ArmorBlender {

    public static final int STEPS = 8;
    private static final int MAX_BLENDS = 1024;
    private static final String DMZ_NAMESPACE = "dragonminez";

    private static final Map<ResourceLocation, Boolean> VARIANT_EXISTS = new HashMap<>();
    private static final Map<String, ResourceLocation> BLENDS = new HashMap<>();
    private static final Set<ResourceLocation> DYNAMIC_TEXTURES = new HashSet<>();
    private static final Set<String> FAILED = new HashSet<>();

    private ArmorBlender() {
    }

    /**
     * @param original textura que o DMZ escolheu (ja considera roupa danificada etc.)
     * @return a mesma textura, a roupa da forma, ou uma mistura das duas
     */
    public static ResourceLocation apply(AbstractClientPlayer player, ResourceLocation original) {
        if (!SsjArmorConfig.ENABLED.get()) return original;
        if (!DMZ_NAMESPACE.equals(original.getNamespace())) return original;

        String path = original.getPath();
        if (!path.startsWith("textures/armor/")) return original;

        ResourceLocation variant = new ResourceLocation(DmzSsj4Armors.MOD_ID, path);
        if (!variantExists(variant)) return original;

        float progress = FadeTracker.progress(player);
        int step = Math.round(progress * STEPS);
        if (step <= 0) return original;
        if (step >= STEPS) return variant;
        return blended(original, variant, step);
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
        String key = original.getPath() + "#" + step;
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
                    output.setPixelRGBA(x, y, mix(a, b, t));
                }
            }

            ResourceLocation location = new ResourceLocation(DmzSsj4Armors.MOD_ID, "dynamic/" + sanitize(key));
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
     * Mistura dois pixels ABGR. Pixel transparente num dos lados nao "suja" a cor do outro:
     * a cor e ponderada pela opacidade de cada um.
     */
    private static int mix(int a, int b, float t) {
        float alphaA = (a >>> 24) & 0xFF;
        float alphaB = (b >>> 24) & 0xFF;
        float weightA = alphaA * (1.0f - t);
        float weightB = alphaB * t;
        float total = weightA + weightB;
        if (total < 0.0001f) return 0;

        int red = channel(a & 0xFF, b & 0xFF, weightA, weightB, total);
        int green = channel((a >>> 8) & 0xFF, (b >>> 8) & 0xFF, weightA, weightB, total);
        int blue = channel((a >>> 16) & 0xFF, (b >>> 16) & 0xFF, weightA, weightB, total);
        int alpha = Math.min(255, Math.round(total));
        return (alpha << 24) | (blue << 16) | (green << 8) | red;
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
