package com.dmzssj4armors.mixin;

import com.dmzssj4armors.client.ArmorContext;
import com.dmzssj4armors.client.MixinTargets;
import com.dragonminez.client.render.layer.DMZCapeLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.BakedGeoModel;

/**
 * Capas: o {@code ArmorCapeRenderer} pede a textura direto ao resolver, sem passar pelo item.
 * Opcional (require = 0): se esta assinatura mudar numa versao futura do DMZ, so a capa deixa
 * de trocar, o resto do addon continua funcionando.
 */
@Mixin(value = DMZCapeLayer.class, remap = false)
public abstract class DMZCapeLayerMixin {

    @Inject(method = MixinTargets.CAPE_LAYER_RENDER, at = @At("HEAD"), require = 0)
    private void dmzssj4armors$pushPlayer(PoseStack poseStack, AbstractClientPlayer animatable, BakedGeoModel bakedModel,
                                          RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                          float partialTick, int packedLight, int packedOverlay, CallbackInfo ci) {
        ArmorContext.push(animatable);
    }

    @Inject(method = MixinTargets.CAPE_LAYER_RENDER, at = @At("RETURN"), require = 0)
    private void dmzssj4armors$popPlayer(PoseStack poseStack, AbstractClientPlayer animatable, BakedGeoModel bakedModel,
                                         RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                         float partialTick, int packedLight, int packedOverlay, CallbackInfo ci) {
        ArmorContext.pop();
    }
}
