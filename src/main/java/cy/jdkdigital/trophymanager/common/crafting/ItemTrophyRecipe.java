package cy.jdkdigital.trophymanager.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import cy.jdkdigital.trophymanager.init.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItemTrophyRecipe extends CustomRecipe
{
    private static final int SUBJECT_ROW = 0;
    private static final int BASE_ROW = 1;
    private static final int SPIN_ROW = 2;
    private static final int WIDE = 3;
    private static final int NARROW = 1;

    private final Ingredient base;
    private final Optional<Ingredient> side;
    private final Optional<Ingredient> spin;

    public ItemTrophyRecipe(CraftingBookCategory category, Ingredient base, Optional<Ingredient> side, Optional<Ingredient> spin) {
        super(category);
        this.base = base;
        this.side = side;
        this.spin = spin;
    }

    public Ingredient base() {
        return base;
    }

    public Optional<Ingredient> side() {
        return side;
    }

    public Optional<Ingredient> spin() {
        return spin;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return find(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Match match = find(input);
        if (match == null) {
            return ItemStack.EMPTY;
        }
        try {
            return TrophyBlock.createItemTrophy(registries, match.subject, match.base, match.spin);
        } catch (Exception e) {
            TrophyManager.LOGGER.warn("Could not build a trophy of " + match.subject + ": " + e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= width() && height >= 2;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.ITEM_TROPHY.get();
    }

    private int width() {
        return side.isPresent() ? WIDE : NARROW;
    }

    private @Nullable Match find(CraftingInput input) {
        int width = width();
        if (input.width() != width) {
            return null;
        }
        int centre = width / 2;
        boolean spinning = input.height() == 3;
        if (input.height() != 2 && !spinning) {
            return null;
        }
        if (spinning && spin.isEmpty()) {
            return null;
        }
        if (input.ingredientCount() != (width == WIDE ? 4 : 2) + (spinning ? 1 : 0)) {
            return null;
        }

        if (width == WIDE && (!input.getItem(0, SUBJECT_ROW).isEmpty() || !input.getItem(2, SUBJECT_ROW).isEmpty())) {
            return null;
        }
        ItemStack subject = input.getItem(centre, SUBJECT_ROW);
        if (subject.isEmpty() || subject.is(ModBlocks.TROPHY.get().asItem())) {
            return null;
        }

        if (side.isPresent() && (!side.get().test(input.getItem(0, BASE_ROW)) || !side.get().test(input.getItem(2, BASE_ROW)))) {
            return null;
        }
        Block baseBlock = baseBlock(input.getItem(centre, BASE_ROW));
        if (baseBlock == null) {
            return null;
        }

        if (spinning) {
            if (width == WIDE && (!input.getItem(0, SPIN_ROW).isEmpty() || !input.getItem(2, SPIN_ROW).isEmpty())) {
                return null;
            }
            if (spin.isEmpty() || !spin.get().test(input.getItem(centre, SPIN_ROW))) {
                return null;
            }
        }
        return new Match(subject, baseBlock, spinning);
    }

    private @Nullable Block baseBlock(ItemStack stack) {
        if (base.test(stack) && stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }
        return null;
    }

    private record Match(ItemStack subject, Block base, boolean spin) {}

    public static class Serializer implements RecipeSerializer<ItemTrophyRecipe>
    {
        private static final MapCodec<ItemTrophyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(ItemTrophyRecipe::base),
                Ingredient.CODEC_NONEMPTY.optionalFieldOf("side").forGetter(ItemTrophyRecipe::side),
                Ingredient.CODEC_NONEMPTY.optionalFieldOf("spin").forGetter(ItemTrophyRecipe::spin)
        ).apply(instance, ItemTrophyRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ItemTrophyRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, ItemTrophyRecipe::base,
                ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), ItemTrophyRecipe::side,
                ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), ItemTrophyRecipe::spin,
                ItemTrophyRecipe::new);

        @Override
        public MapCodec<ItemTrophyRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ItemTrophyRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
