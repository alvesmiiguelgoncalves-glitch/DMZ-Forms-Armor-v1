package com.dmzssj4armors.mixin;

import com.dmzssj4armors.client.ArmorContext;
import com.dmzssj4armors.client.MixinTargets;
import com.dragonminez.client.render.layer.DMZCustomArmorLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Corpos "buffed" do DMZ (SSJ4 GT, SSJ4 Daima, SSJ Full Power / buffedg3...) desenham a roupa
 * por esta camada, que nao conhece o jogador na hora de pedir a textura. Registramos o jogador
 * durante o desenho do osso do corpo.
 */
@Mixin(value = DMZCustomArmorLayer.class, remap = false)
public abstract class DMZCustomArmorLayerMixin {

    @Inject(method = MixinTargets.CUSTOM_ARMOR_RENDER_FOR_BONE, at = @At("HEAD"))
    private void dmzssj4armors$pushPlayer(PoseStack poseStack, AbstractClientPlayer animatable, GeoBone playerBone,
                                          RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                          float partialTick, int packedLight, int packedOverlay, CallbackInfo ci) {
        ArmorContext.push(animatable);
    }

    @Inject(method = MixinTargets.CUSTOM_ARMOR_RENDER_FOR_BONE, at = @At("RETURN"))
    private void dmzssj4armors$popPlayer(PoseStack poseStack, AbstractClientPlayer animatable, GeoBone playerBone,
                                         RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                         float partialTick, int packedLight, int packedOverlay, CallbackInfo ci) {
        ArmorContext.pop();
    }
}
