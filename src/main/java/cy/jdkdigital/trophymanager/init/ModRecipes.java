package cy.jdkdigital.trophymanager.init;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.crafting.ItemTrophyRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, TrophyManager.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ItemTrophyRecipe>> ITEM_TROPHY =
            RECIPE_SERIALIZERS.register("item_trophy", () -> new RecipeSerializer<>(ItemTrophyRecipe.MAP_CODEC, ItemTrophyRecipe.STREAM_CODEC));
}
