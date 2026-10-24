package com.dmzssj4armors.mixin;

import com.dmzssj4armors.client.ArmorContext;
import com.dragonminez.common.init.armor.DbzArmorCapeItem;
import com.dragonminez.common.init.armor.DbzArmorItem;
import com.dragonminez.common.init.armor.DbzArmorPatrollerItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * As armaduras normais do DMZ devolvem a textura em {@code getArmorTexture(stack, entity, ...)}.
 * Aqui so registramos quem e a entidade, para o {@link ArmorTextureResolverMixin} saber.
 */
@Mixin(value = {DbzArmorItem.class, DbzArmorPatrollerItem.class, DbzArmorCapeItem.class}, remap = false)
public abstract class DbzArmorItemsMixin {

    @Inject(method = "getArmorTexture", at = @At("HEAD"))
    private void dmzssj4armors$pushEntity(ItemStack stack, Entity entity, EquipmentSlot slot, String type,
                                          CallbackInfoReturnable<String> cir) {
        ArmorContext.push(entity);
    }

    @Inject(method = "getArmorTexture", at = @At("RETURN"))
    private void dmzssj4armors$popEntity(ItemStack stack, Entity entity, EquipmentSlot slot, String type,
                                         CallbackInfoReturnable<String> cir) {
        ArmorContext.pop();
    }
}
