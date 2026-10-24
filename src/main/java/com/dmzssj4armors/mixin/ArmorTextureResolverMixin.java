package com.dmzssj4armors.mixin;

import com.dmzssj4armors.client.ArmorBlender;
import com.dmzssj4armors.client.ArmorContext;
import com.dragonminez.client.util.ArmorTextureResolver;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ponto central: todo caminho de render do DMZ que escolhe textura de armadura passa por
 * {@code ArmorTextureResolver.resolve}. Aqui trocamos o resultado pela roupa da forma
 * (ou pela mistura da transicao) quando o jogador atual esta numa forma alvo.
 */
@Mixin(value = ArmorTextureResolver.class, remap = false)
public abstract class ArmorTextureResolverMixin {

    @Inject(method = "resolve", at = @At("RETURN"), cancellable = true)
    private static void dmzssj4armors$swapTexture(String modId, String itemId, EquipmentSlot slot, ItemStack stack,
                                                  CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = ArmorContext.current();
        if (player == null) return;

        ResourceLocation original = cir.getReturnValue();
        ResourceLocation swapped = ArmorBlender.apply(player, original);
        if (swapped != original) {
            cir.setReturnValue(swapped);
        }
    }
}
