package cy.jdkdigital.trophymanager.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import cy.jdkdigital.trophymanager.init.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemTrophyRecipe extends CustomRecipe
{
    public static final MapCodec<ItemTrophyRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("base").forGetter(ItemTrophyRecipe::base),
            Ingredient.CODEC.optionalFieldOf("side").forGetter(ItemTrophyRecipe::side),
            Ingredient.CODEC.optionalFieldOf("spin").forGetter(ItemTrophyRecipe::spin)
    ).apply(instance, ItemTrophyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTrophyRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ItemTrophyRecipe::base,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, ItemTrophyRecipe::side,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, ItemTrophyRecipe::spin,
            ItemTrophyRecipe::new);

    private static final HolderLookup.Provider BUILT_IN_REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    private static final int SUBJECT_ROW = 0;
    private static final int BASE_ROW = 1;
    private static final int SPIN_ROW = 2;
    private static final int WIDE = 3;
    private static final int NARROW = 1;

    private static final List<Item> EXAMPLE_SUBJECTS = List.of(
            Items.DIAMOND,
            Items.NETHERITE_INGOT,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.TOTEM_OF_UNDYING,
            Items.NETHER_STAR,
            Items.DRAGON_EGG);

    private final Ingredient base;
    private final Optional<Ingredient> side;
    private final Optional<Ingredient> spin;

    public ItemTrophyRecipe(Ingredient base, Optional<Ingredient> side, Optional<Ingredient> spin) {
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
    public ItemStack assemble(CraftingInput input) {
        Match match = find(input);
        if (match == null) {
            return ItemStack.EMPTY;
        }
        try {
            return TrophyBlock.createItemTrophy(BUILT_IN_REGISTRIES, match.subject, match.base, match.spin);
        } catch (Exception e) {
            TrophyManager.LOGGER.warn("Could not build a trophy of " + match.subject + ": " + e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    @Override
    public RecipeSerializer<ItemTrophyRecipe> getSerializer() {
        return ModRecipes.ITEM_TROPHY.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        List<RecipeDisplay> displays = new ArrayList<>();
        displays.add(shaped(false));
        if (spin.isPresent()) {
            displays.add(shaped(true));
        }
        return displays;
    }

    private RecipeDisplay shaped(boolean spinning) {
        int width = width();
        int height = spinning ? 3 : 2;
        List<SlotDisplay> slots = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                slots.add(slot(row, column, width));
            }
        }
        return new ShapedCraftingRecipeDisplay(width, height, slots, resultDisplay(),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE.builtInRegistryHolder()));
    }

    private SlotDisplay slot(int row, int column, int width) {
        boolean centre = column == width / 2;
        if (row == SUBJECT_ROW) {
            return centre ? subjectDisplay() : SlotDisplay.Empty.INSTANCE;
        }
        if (row == BASE_ROW) {
            if (centre) {
                return base.display();
            }
            return side.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE);
        }
        if (centre) {
            return spin.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE);
        }
        return SlotDisplay.Empty.INSTANCE;
    }

    private static SlotDisplay subjectDisplay() {
        List<SlotDisplay> examples = new ArrayList<>();
        for (Item item : EXAMPLE_SUBJECTS) {
            examples.add(new SlotDisplay.ItemSlotDisplay(item.builtInRegistryHolder()));
        }
        return new SlotDisplay.Composite(examples);
    }

    private static SlotDisplay resultDisplay() {
        ItemStack trophy = TrophyBlock.createDefaultItemTrophy();
        return trophy.isEmpty()
                ? SlotDisplay.Empty.INSTANCE
                : new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(trophy));
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
}
