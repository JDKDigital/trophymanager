package cy.jdkdigital.trophymanager.common.item;

import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
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
            CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
            return TrophyBlock.trophyName(tag.getStringOr("Subject", ""), tag.getStringOr("Name", ""));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay pDisplay, Consumer<Component> pConsumer, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pDisplay, pConsumer, pTooltipFlag);

        if (pStack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag tag = pStack.get(DataComponents.CUSTOM_DATA).copyTag();
            pConsumer.accept(Component.translatable("trophymanager.tooltip.trophy.scale", tag.getFloatOr("Scale", TrophyManagerConfig.GENERAL.defaultScale.get().floatValue())));
        }
    }
}
