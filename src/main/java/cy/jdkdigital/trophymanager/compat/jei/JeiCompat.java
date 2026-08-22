package cy.jdkdigital.trophymanager.compat.jei;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import cy.jdkdigital.trophymanager.common.crafting.ItemTrophyRecipe;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JeiCompat implements IModPlugin
{
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(TrophyManager.MODID, "jei");

    private static final List<ItemStack> EXAMPLE_SUBJECTS = List.of(
            new ItemStack(Items.DIAMOND),
            new ItemStack(Items.NETHERITE_INGOT),
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE),
            new ItemStack(Items.TOTEM_OF_UNDYING),
            new ItemStack(Items.NETHER_STAR),
            new ItemStack(Items.DRAGON_EGG));

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModBlocks.TROPHY.get().asItem(), TrophySubtypeInterpreter.INSTANCE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ItemTrophyCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemTrophyDisplay> displays = new ArrayList<>();
        for (ItemTrophyRecipe recipe : recipes()) {
            try {
                displays.add(display(recipe, false));
                if (recipe.spin().isPresent()) {
                    displays.add(display(recipe, true));
                }
            } catch (Exception e) {
                TrophyManager.LOGGER.warn("Could not show an item trophy recipe in JEI: " + e.getMessage());
            }
        }
        registration.addRecipes(ItemTrophyCategory.TYPE, displays);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(Blocks.CRAFTING_TABLE, ItemTrophyCategory.TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        try {
            IIngredientManager manager = jeiRuntime.getIngredientManager();
            List<ItemStack> existing = manager.getAllItemStacks().stream()
                    .filter(stack -> stack.is(ModBlocks.TROPHY.get().asItem()))
                    .toList();
            if (!existing.isEmpty()) {
                manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, existing);
            }
            manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(TrophyBlock.createDefaultItemTrophy()));
        } catch (Exception e) {
            TrophyManager.LOGGER.warn("Could not replace the trophy entry in the JEI ingredient list: " + e.getMessage());
        }
    }

    private static List<ItemTrophyRecipe> recipes() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        List<ItemTrophyRecipe> found = new ArrayList<>();
        try {
            for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
                if (holder.value() instanceof ItemTrophyRecipe recipe) {
                    found.add(recipe);
                }
            }
        } catch (Exception e) {
            TrophyManager.LOGGER.warn("Could not read the item trophy recipes for JEI: " + e.getMessage());
        }
        return found;
    }

    private static ItemTrophyDisplay display(ItemTrophyRecipe recipe, boolean spin) {
        List<ItemStack> bases = stacks(recipe.base());
        List<ItemStack> sides = recipe.side().map(JeiCompat::stacks).orElseGet(List::of);
        List<ItemStack> spins = spin ? recipe.spin().map(JeiCompat::stacks).orElseGet(List::of) : List.of();
        Block base = baseBlock(bases);
        HolderLookup.Provider registries = registries();
        List<ItemStack> results = new ArrayList<>();
        results.add(TrophyBlock.createDefaultItemTrophy());
        for (ItemStack subject : EXAMPLE_SUBJECTS) {
            try {
                results.add(registries == null
                        ? TrophyBlock.createDefaultItemTrophy()
                        : TrophyBlock.createItemTrophy(registries, subject, base, spin));
            } catch (Exception e) {
                results.add(TrophyBlock.createDefaultItemTrophy());
            }
        }
        return new ItemTrophyDisplay(EXAMPLE_SUBJECTS, sides, bases, spins, results);
    }

    private static List<ItemStack> stacks(Ingredient ingredient) {
        return List.of(ingredient.getItems());
    }

    private static Block baseBlock(List<ItemStack> bases) {
        for (ItemStack stack : bases) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                return blockItem.getBlock();
            }
        }
        return Blocks.SMOOTH_STONE_SLAB;
    }

    private static HolderLookup.Provider registries() {
        Level level = Minecraft.getInstance().level;
        return level == null ? null : level.registryAccess();
    }
}
