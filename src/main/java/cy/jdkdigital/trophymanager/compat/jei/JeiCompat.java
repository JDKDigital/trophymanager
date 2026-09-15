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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JeiCompat implements IModPlugin
{
    private static final Identifier UID = Identifier.fromNamespaceAndPath(TrophyManager.MODID, "jei");

    private static final List<Item> EXAMPLE_SUBJECTS = List.of(
            Items.DIAMOND,
            Items.NETHERITE_INGOT,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.TOTEM_OF_UNDYING,
            Items.NETHER_STAR,
            Items.DRAGON_EGG);

    @Override
    public Identifier getPluginUid() {
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
        for (ItemTrophyRecipe recipe : TrophyRecipeCache.itemTrophyRecipes()) {
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

    private static List<ItemStack> stacks(Ingredient ingredient) {
        List<ItemStack> items = new ArrayList<>();
        ingredient.items().forEach(holder -> {
            ItemStack stack = new ItemStack(holder.value());
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        });
        return items;
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

    private static ItemTrophyDisplay display(ItemTrophyRecipe recipe, boolean spin) {
        List<ItemStack> bases = stacks(recipe.base());
        List<ItemStack> sides = recipe.side().map(JeiCompat::stacks).orElseGet(List::of);
        List<ItemStack> spins = spin ? recipe.spin().map(JeiCompat::stacks).orElseGet(List::of) : List.<ItemStack>of();

        Block base = baseBlock(bases);
        HolderLookup.Provider registries = registries();
        List<ItemStack> subjects = new ArrayList<>();
        List<ItemStack> results = new ArrayList<>();
        results.add(TrophyBlock.createDefaultItemTrophy());
        for (Item item : EXAMPLE_SUBJECTS) {
            ItemStack subject = new ItemStack(item);
            subjects.add(subject);
            try {
                results.add(registries == null
                        ? TrophyBlock.createDefaultItemTrophy()
                        : TrophyBlock.createItemTrophy(registries, subject, base, spin));
            } catch (Exception e) {
                results.add(TrophyBlock.createDefaultItemTrophy());
            }
        }
        return new ItemTrophyDisplay(subjects, sides, bases, spins, results);
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
