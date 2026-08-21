package cy.jdkdigital.trophymanager.init;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.entity.RenderPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TrophyManager.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RenderPlayer>> PLAYER = ENTITIES.register("player",
            () -> EntityType.Builder.of(RenderPlayer::new, MobCategory.MISC).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(TrophyManager.MODID, "player"))));
}
