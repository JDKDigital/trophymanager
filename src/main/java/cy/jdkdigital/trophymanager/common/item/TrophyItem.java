package cy.jdkdigital.trophymanager.common.item;

import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.client.render.item.TrophyItemStackRenderer;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class TrophyItem extends BlockItem
{
    public TrophyItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nonnull
    @Override
    public Component getName(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            var tag = stack.get(DataComponents.CUSTOM_DATA).getUnsafe();
            return TrophyBlock.trophyName(tag.getString("Subject"), tag.getString("Name"));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);

        if (pStack.has(DataComponents.CUSTOM_DATA)) {
            var tag = pStack.get(DataComponents.CUSTOM_DATA).getUnsafe();
            float scale = tag.contains("Scale") ? tag.getFloat("Scale") : TrophyManagerConfig.GENERAL.defaultScale.get().floatValue();
            pTooltipComponents.add(Component.translatable("trophymanager.tooltip.trophy.scale", scale));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions()
        {
            final BlockEntityWithoutLevelRenderer myRenderer = new TrophyItemStackRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return myRenderer;
            }
        });
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return armorType == EquipmentSlot.HEAD;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }
}
