package com.dmzssj4armors.mixin;

import com.dmzssj4armors.client.ArmorContext;
import com.dragonminez.client.render.DMZRenderHand;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Braco em primeira pessoa: o DMZ chama o resolver direto, com o jogador local em maos.
 * Opcional (require = 0): se esse metodo privado mudar de nome no DMZ, so a primeira pessoa
 * deixa de trocar.
 */
@Mixin(value = DMZRenderHand.class, remap = false)
public abstract class DMZRenderHandMixin {

    @Inject(method = "renderDbzArmor", at = @At("HEAD"), require = 0)
    private void dmzssj4armors$pushPlayer(PoseStack ps, MultiBufferSource buffer, int light,
                                          AbstractClientPlayer player, ModelPart arm, CallbackInfo ci) {
        ArmorContext.push(player);
    }

    @Inject(method = "renderDbzArmor", at = @At("RETURN"), require = 0)
    private void dmzssj4armors$popPlayer(PoseStack ps, MultiBufferSource buffer, int light,
                                         AbstractClientPlayer player, ModelPart arm, CallbackInfo ci) {
        ArmorContext.pop();
    }
}
