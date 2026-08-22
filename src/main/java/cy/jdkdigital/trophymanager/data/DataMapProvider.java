package cy.jdkdigital.trophymanager.data;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.common.datamap.DropRateMap;
import cy.jdkdigital.trophymanager.common.datamap.NbtMap;
import cy.jdkdigital.trophymanager.common.datamap.PropertiesMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataMapProvider extends net.neoforged.neoforge.common.data.DataMapProvider
{
    protected DataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        final var nbt = builder(TrophyManager.NBT_MAP);

        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.AXOLOTL), new NbtMap(List.of("Variant")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.CAT), new NbtMap(List.of("variant", "CollarColor")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.WOLF), new NbtMap(List.of("variant", "CollarColor")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.RABBIT), new NbtMap(List.of("RabbitType")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.FROG), new NbtMap(List.of("variant")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.LLAMA), new NbtMap(List.of("Variant")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.TROPICAL_FISH), new NbtMap(List.of("Variant")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.TRADER_LLAMA), new NbtMap(List.of("Variant")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.HORSE), new NbtMap(List.of("Variant")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.FOX), new NbtMap(List.of("Type")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SHEEP), new NbtMap(List.of("Color", "Sheared")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PARROT), new NbtMap(List.of("Variant")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PANDA), new NbtMap(List.of("MainGene", "HiddenGene")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.MOOSHROOM), new NbtMap(List.of("Type")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PUFFERFISH), new NbtMap(List.of("PuffState")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.VILLAGER), new NbtMap(List.of("VillagerData")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SNOW_GOLEM), new NbtMap(List.of("Pumpkin")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE_VILLAGER), new NbtMap(List.of("VillagerData", "IsBaby")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.CREEPER), new NbtMap(List.of("powered")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE), new NbtMap(List.of("IsBaby")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.DROWNED), new NbtMap(List.of("IsBaby")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.HUSK), new NbtMap(List.of("IsBaby")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIFIED_PIGLIN), new NbtMap(List.of("IsBaby")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIGLIN), new NbtMap(List.of("IsBaby")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOGLIN), new NbtMap(List.of("IsBaby")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE_HORSE), new NbtMap(List.of("Age", "ForcedAge")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SHULKER), new NbtMap(List.of("Color", "Peek")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.GOAT), new NbtMap(List.of("HasLeftHorn", "HasRightHorn", "IsScreamingGoat")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE), new NbtMap(List.of("HasNectar")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BOGGED), new NbtMap(List.of("sheared")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.STRIDER), new NbtMap(List.of("Suffocating")), false);
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SNIFFER), new NbtMap(List.of("variant")), false, new ModLoadedCondition("sussysniffers"));
        nbt.add(ResourceLocation.parse("productivebees:configurable_bee"), new NbtMap(List.of("type")), false, new ModLoadedCondition("productivebees"));
        nbt.add(ResourceLocation.parse("productivebees:dye_bee"), new NbtMap(List.of("type")), false, new ModLoadedCondition("productivebees"));
        nbt.add(ResourceLocation.parse("dyenamics:sheep"), new NbtMap(List.of("Color", "Sheared")), false, new ModLoadedCondition("dyenamics"));
        nbt.add(ResourceLocation.parse("infernalexp:shroomloin"), new NbtMap(List.of("ShroomloinType")), false, new ModLoadedCondition("infernalexp"));
        nbt.add(ResourceLocation.parse("infernalexp:basalt_giant"), new NbtMap(List.of("Size")), false, new ModLoadedCondition("infernalexp"));
        nbt.add(ResourceLocation.parse("cobblemon:pokemon"), new NbtMap(List.of("Pokemon", "PoseType")), false, new ModLoadedCondition("cobblemon"));
        nbt.add(ResourceLocation.parse("biomeswevegone:man_o_war"), new NbtMap(List.of("Age", "Color")), false, new ModLoadedCondition("biomeswevegone"));
        nbt.add(ResourceLocation.parse("biomeswevegone:pumpkin_warden"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("biomeswevegone"));
        nbt.add(ResourceLocation.parse("biomeswevegone:oddion"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("biomeswevegone"));
        nbt.add(ResourceLocation.parse("endermanoverhaul:axolotl_pet_enderman"), new NbtMap(List.of("Owner")), false, new ModLoadedCondition("endermanoverhaul"));
        nbt.add(ResourceLocation.parse("endermanoverhaul:hammerhead_pet_enderman"), new NbtMap(List.of("Owner")), false, new ModLoadedCondition("endermanoverhaul"));
        nbt.add(ResourceLocation.parse("endermanoverhaul:pet_enderman"), new NbtMap(List.of("Owner")), false, new ModLoadedCondition("endermanoverhaul"));
        nbt.add(ResourceLocation.parse("livingthings:crab"), new NbtMap(List.of("CrabVariant")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("livingthings:owl"), new NbtMap(List.of("OwlVariant")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("livingthings:seahorse"), new NbtMap(List.of("SeahorseVariant")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("livingthings:shroomie"), new NbtMap(List.of("ShroomieType")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("livingthings:mantaray"), new NbtMap(List.of("MantarayVariant")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("livingthings:peacock"), new NbtMap(List.of("Fluffed")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("livingthings:lion"), new NbtMap(List.of("IsMale", "LionVariant")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("livingthings:snail"), new NbtMap(List.of("SnailVariant", "ShellColorF", "ShellColorB", "PatternType")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("livingthings:giraffe"), new NbtMap(List.of("GiraffeVariant")), false, new ModLoadedCondition("livingthings"));
        nbt.add(ResourceLocation.parse("iceandfire:amphithere"), new NbtMap(List.of("Variant", "Type")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:cyclops"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:deathworm"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:dread_beast"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:dread_lich"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:dread_ghoul"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:hippocampus"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:hippogryph"), new NbtMap(List.of("Variant", "Type")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:hydra"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:sea_serpent"), new NbtMap(List.of("Variant", "Ancient")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:troll"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:fire_dragon"), new NbtMap(List.of("Variant", "Gender")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:ice_dragon"), new NbtMap(List.of("Variant", "Gender")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:lightning_dragon"), new NbtMap(List.of("Variant", "Gender")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("iceandfire:pixie"), new NbtMap(List.of("Color")), false, new ModLoadedCondition("iceandfire"));
        nbt.add(ResourceLocation.parse("arsnouveau:starbuncle"), new NbtMap(List.of("starbuncleData")), false, new ModLoadedCondition("arsnouveau"));
        nbt.add(ResourceLocation.parse("ars_elemental:siren_entity"), new NbtMap(List.of("color")), false, new ModLoadedCondition("ars_elemental"));
        nbt.add(ResourceLocation.parse("creeperoverhaul:ocean_creeper"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("creeperoverhaul"));
        nbt.add(ResourceLocation.parse("variantsandventures:murk"), new NbtMap(List.of("Variant", "Sheared")), false, new ModLoadedCondition("variantsandventures"));

        // Shiny! Mobs copies of vanilla mobs share the same NBT used for their visual state
        nbt.add(ResourceLocation.parse("shiny:shiny_axolotl"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_cat"), new NbtMap(List.of("variant", "CollarColor")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_wolf"), new NbtMap(List.of("variant", "CollarColor")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_rabbit"), new NbtMap(List.of("RabbitType")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_frog"), new NbtMap(List.of("variant")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_llama"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_tropical_fish"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_trader_llama"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_horse"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_fox"), new NbtMap(List.of("Type")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_sheep"), new NbtMap(List.of("Color", "Sheared")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_parrot"), new NbtMap(List.of("Variant")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_panda"), new NbtMap(List.of("MainGene", "HiddenGene")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_mooshroom"), new NbtMap(List.of("Type")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_pufferfish"), new NbtMap(List.of("PuffState")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_villager"), new NbtMap(List.of("VillagerData")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_snow_golem"), new NbtMap(List.of("Pumpkin")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_zombie_villager"), new NbtMap(List.of("VillagerData", "IsBaby")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_creeper"), new NbtMap(List.of("powered")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_zombie"), new NbtMap(List.of("IsBaby")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_drowned"), new NbtMap(List.of("IsBaby")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_husk"), new NbtMap(List.of("IsBaby")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_zombified_piglin"), new NbtMap(List.of("IsBaby")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_piglin"), new NbtMap(List.of("IsBaby")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_zoglin"), new NbtMap(List.of("IsBaby")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_zombie_horse"), new NbtMap(List.of("Age", "ForcedAge")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_shulker"), new NbtMap(List.of("Color", "Peek")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_goat"), new NbtMap(List.of("HasLeftHorn", "HasRightHorn", "IsScreamingGoat")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_bee"), new NbtMap(List.of("HasNectar")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_bogged"), new NbtMap(List.of("sheared")), false, new ModLoadedCondition("shiny"));
        nbt.add(ResourceLocation.parse("shiny:shiny_sniffer"), new NbtMap(List.of("variant")), false, new ModLoadedCondition("shiny"), new ModLoadedCondition("sussysniffers"));
        // The shiny Ender Dragon is the vanilla dragon flagged shiny through a serialized data attachment
        nbt.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ENDER_DRAGON), new NbtMap(List.of("neoforge:attachments")), false, new ModLoadedCondition("shiny"));

        final var dropRates = builder(TrophyManager.DROP_RATE_MAP);

        dropRates.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ENDER_DRAGON), new DropRateMap(1f), false);

        final var properties = builder(TrophyManager.PROPERTIES_MAP);

        properties.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.GHAST), new PropertiesMap(0.4f, 1.4d, 0f), false);
        properties.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.GLOW_SQUID), new PropertiesMap(0.4f, 0.7d, 70f), false);
        properties.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ENDER_DRAGON), new PropertiesMap(0.1f, 0.8d, 180f), false);
        properties.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE), new PropertiesMap(1.0f, 0.8d, 0f), false);
        properties.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PHANTOM), new PropertiesMap(1.0f, 0.8d, 0f), false);
        properties.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.VEX), new PropertiesMap(1.0f, 0.8d, 0f), false);
        properties.add(ResourceLocation.parse("shiny:shiny_ghast"), new PropertiesMap(0.4f, 1.4d, 0f), false, new ModLoadedCondition("shiny"));
        properties.add(ResourceLocation.parse("shiny:shiny_glow_squid"), new PropertiesMap(0.4f, 0.7d, 70f), false, new ModLoadedCondition("shiny"));
        properties.add(ResourceLocation.parse("shiny:shiny_bee"), new PropertiesMap(1.0f, 0.8d, 0f), false, new ModLoadedCondition("shiny"));
        properties.add(ResourceLocation.parse("shiny:shiny_phantom"), new PropertiesMap(1.0f, 0.8d, 0f), false, new ModLoadedCondition("shiny"));
        properties.add(ResourceLocation.parse("shiny:shiny_vex"), new PropertiesMap(1.0f, 0.8d, 0f), false, new ModLoadedCondition("shiny"));
        properties.add(ResourceLocation.parse("twilightforest:ur_ghast"), new PropertiesMap(0.4f, 1.4d, 0f), false, new ModLoadedCondition("twilightforest"));
        properties.add(ResourceLocation.parse("twilightforest:hydra"), new PropertiesMap(0.4f, 0.5d, 0f), false, new ModLoadedCondition("twilightforest"));
        properties.add(ResourceLocation.parse("biomeswevegone:man_o_war"), new PropertiesMap(0.5f, 0.9d, 0f), false, new ModLoadedCondition("biomeswevegone"));
        properties.add(ResourceLocation.parse("iceandfire:fire_dragon"), new PropertiesMap(0.1f, 0.8d, 0f), false, new ModLoadedCondition("iceandfire"));
        properties.add(ResourceLocation.parse("iceandfire:ice_dragon"), new PropertiesMap(0.1f, 0.8d, 0f), false, new ModLoadedCondition("iceandfire"));
        properties.add(ResourceLocation.parse("iceandfire:lightning_dragon"), new PropertiesMap(0.1f, 0.8d, 0f), false, new ModLoadedCondition("iceandfire"));
    }
}
