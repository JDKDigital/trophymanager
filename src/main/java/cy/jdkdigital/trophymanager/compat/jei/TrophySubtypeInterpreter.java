package cy.jdkdigital.trophymanager.compat.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class TrophySubtypeInterpreter implements ISubtypeInterpreter<ItemStack>
{
    public static final TrophySubtypeInterpreter INSTANCE = new TrophySubtypeInterpreter();

    private TrophySubtypeInterpreter() {
    }

    @Override
    public Object getSubtypeData(ItemStack stack, UidContext context) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return "";
        }
        try {
            CompoundTag tag = data.copyTag();
            if ("entity".equals(tag.getString("TrophyType"))) {
                return "entity/" + tag.getCompound("TrophyEntity").getString("entityType");
            }
            return "item/" + tag.getCompound("TrophyItem").getString("id");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
        return getSubtypeData(stack, context).toString();
    }
}
