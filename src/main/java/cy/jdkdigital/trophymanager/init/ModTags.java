package cy.jdkdigital.trophymanager.init;

import cy.jdkdigital.trophymanager.TrophyManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags
{
    public static final TagKey<Block> TROPHY_BASE = BlockTags.create(new ResourceLocation(TrophyManager.MODID, "trophy_base"));
}
