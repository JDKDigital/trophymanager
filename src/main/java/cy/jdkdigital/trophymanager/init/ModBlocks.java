package cy.jdkdigital.trophymanager.init;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import cy.jdkdigital.trophymanager.common.item.TrophyItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class ModBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TrophyManager.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TrophyManager.MODID);

    public static final DeferredHolder<Block, ? extends Block> TROPHY = createBlock("trophy", TrophyBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE_SLAB).noOcclusion());

    public static DeferredHolder<Block, ? extends Block> createBlock(String name, Function<BlockBehaviour.Properties, ? extends Block> factory, BlockBehaviour.Properties properties) {
        var block = BLOCKS.registerBlock(name, factory, () -> properties);
        ITEMS.registerItem(name, p -> new TrophyItem(block.get(), p.useBlockDescriptionPrefix()
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setDamageOnHurt(false).build())));
        return block;
    }
}
