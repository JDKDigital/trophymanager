package cy.jdkdigital.trophymanager.compat.jei;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.crafting.ItemTrophyRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Caches the client-side {@link RecipeMap} delivered by {@link RecipesReceivedEvent}.
 * <p>
 * The client only sees the recipe types opted into by {@code OnDatapackSyncEvent#sendRecipes}
 * on the server. JEI refreshes its plugins when recipes arrive, so {@link JeiCompat} reads the
 * cache while building its displays.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = TrophyManager.MODID)
public final class TrophyRecipeCache
{
    private static RecipeMap recipeMap = RecipeMap.EMPTY;

    private TrophyRecipeCache() {}

    public static List<ItemTrophyRecipe> itemTrophyRecipes() {
        List<ItemTrophyRecipe> recipes = new ArrayList<>();
        for (RecipeHolder<?> holder : recipeMap.byType(RecipeType.CRAFTING)) {
            if (holder.value() instanceof ItemTrophyRecipe recipe) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        recipeMap = RecipeMap.EMPTY;
    }
}
