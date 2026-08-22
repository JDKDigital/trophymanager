package cy.jdkdigital.trophymanager.compat.jei;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ItemTrophyCategory implements IRecipeCategory<ItemTrophyDisplay>
{
    public static final RecipeType<ItemTrophyDisplay> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(TrophyManager.MODID, "item_trophy"), ItemTrophyDisplay.class);

    private static final int LEFT_X = 4;
    private static final int CENTRE_X = 22;
    private static final int RIGHT_X = 40;
    private static final int RESULT_X = 86;
    private static final int SUBJECT_Y = 1;
    private static final int BASE_Y = 19;
    private static final int SPIN_Y = 37;

    private final IDrawable icon;

    public ItemTrophyCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(TrophyBlock.createDefaultItemTrophy());
    }

    @Override
    public RecipeType<ItemTrophyDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.trophy.jei.item_trophy");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 110;
    }

    @Override
    public int getHeight() {
        return 58;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemTrophyDisplay recipe, IFocusGroup focuses) {
        builder.addInputSlot(CENTRE_X, SUBJECT_Y)
                .setStandardSlotBackground()
                .addItemStacks(recipe.subjects())
                .addRichTooltipCallback(ItemTrophyCategory::anyItemTooltip);

        if (!recipe.sides().isEmpty()) {
            builder.addInputSlot(LEFT_X, BASE_Y).setStandardSlotBackground().addItemStacks(recipe.sides());
            builder.addInputSlot(RIGHT_X, BASE_Y).setStandardSlotBackground().addItemStacks(recipe.sides());
        }
        builder.addInputSlot(CENTRE_X, BASE_Y).setStandardSlotBackground().addItemStacks(recipe.bases());

        if (recipe.spinning()) {
            builder.addInputSlot(CENTRE_X, SPIN_Y).setStandardSlotBackground().addItemStacks(recipe.spins());
        }
        builder.addOutputSlot(RESULT_X, BASE_Y).setOutputSlotBackground().addItemStacks(recipe.results());
    }

    private static void anyItemTooltip(IRecipeSlotView slotView, ITooltipBuilder tooltip) {
        tooltip.add(Component.translatable("gui.trophy.jei.any_item").withStyle(ChatFormatting.GRAY));
    }
}
