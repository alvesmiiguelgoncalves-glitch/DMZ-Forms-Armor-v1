package com.dmzssj4armors.client;

/**
 * Assinaturas (com descritor) dos metodos do DragonMine Z que recebem mixin.
 *
 * <p>Os metodos de camada do DMZ sao genericos ({@code T extends AbstractClientPlayer & GeoAnimatable}),
 * entao o compilador gera uma versao "ponte" extra de cada um. O descritor completo escolhe so a
 * versao real, cujo parametro do jogador e {@code AbstractClientPlayer}.</p>
 */
public final class MixinTargets {

    public static final String CUSTOM_ARMOR_RENDER_FOR_BONE =
            "renderForBone(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/player/AbstractClientPlayer;"
                    + "Lsoftware/bernie/geckolib/cache/object/GeoBone;"
                    + "Lnet/minecraft/client/renderer/RenderType;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "Lcom/mojang/blaze3d/vertex/VertexConsumer;FII)V";

    public static final String CAPE_LAYER_RENDER =
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/player/AbstractClientPlayer;"
                    + "Lsoftware/bernie/geckolib/cache/object/BakedGeoModel;"
                    + "Lnet/minecraft/client/renderer/RenderType;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "Lcom/mojang/blaze3d/vertex/VertexConsumer;FII)V";

    private MixinTargets() {
    }
}
