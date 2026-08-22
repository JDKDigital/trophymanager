package cy.jdkdigital.trophymanager;

import cy.jdkdigital.trophymanager.client.render.block.TrophyBlockEntityRenderer;
import cy.jdkdigital.trophymanager.client.render.entity.PlayerTrophyRenderer;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.common.datamap.DropRateMap;
import cy.jdkdigital.trophymanager.common.datamap.NbtMap;
import cy.jdkdigital.trophymanager.common.datamap.PropertiesMap;
import cy.jdkdigital.trophymanager.compat.CuriosCompat;
import cy.jdkdigital.trophymanager.init.ModBlockEntities;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import cy.jdkdigital.trophymanager.init.ModEntities;
import cy.jdkdigital.trophymanager.init.ModRecipes;
//import cy.jdkdigital.trophymanager.network.Networking;
import cy.jdkdigital.trophymanager.network.PacketOpenGui;
import cy.jdkdigital.trophymanager.network.PacketUpdateTrophy;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TrophyManager.MODID)
public class TrophyManager
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "trophymanager";

    public static final DataMapType<EntityType<?>, NbtMap> NBT_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(MODID, "nbt_map"), Registries.ENTITY_TYPE, NbtMap.CODEC).synced(NbtMap.NBT_CODEC, false).build();
    public static final DataMapType<EntityType<?>, DropRateMap> DROP_RATE_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(MODID, "drop_rate_map"), Registries.ENTITY_TYPE, DropRateMap.CODEC).synced(DropRateMap.DROP_RATE_CODEC, false).build();
    public static final DataMapType<EntityType<?>, PropertiesMap> PROPERTIES_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(MODID, "properties_map"), Registries.ENTITY_TYPE, PropertiesMap.CODEC).synced(PropertiesMap.CODEC, false).build();

    public TrophyManager(IEventBus modEventBus, ModContainer modContainer) {
        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.addListener(this::onEntityDeath);
//        NeoForge.EVENT_BUS.addListener(this::onAdvancementEarned);

        modEventBus.addListener(this::modComms);
        modEventBus.addListener(this::doCommonStuff);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, TrophyManagerConfig.SERVER_CONFIG);
    }

    private void doCommonStuff(final FMLCommonSetupEvent event) {
//        Networking.registerMessages();
    }

    private void modComms(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.register();
        }
    }

    private void onEntityDeath(final LivingDeathEvent event) {
        Entity deadEntity = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (TrophyManagerConfig.GENERAL.dropFromMobs.get() && !(deadEntity instanceof Player) && source instanceof ServerPlayer player && (!(source instanceof FakePlayer) || TrophyManagerConfig.GENERAL.allowFakePlayer.get())) {
            var dropRate = deadEntity.getType().builtInRegistryHolder().getData(DROP_RATE_MAP);
            double chance = dropRate != null ? dropRate.dropRate() : deadEntity.getType().is(Tags.EntityTypes.BOSSES) ? TrophyManagerConfig.GENERAL.dropChanceBoss.get() : TrophyManagerConfig.GENERAL.dropChanceMobs.get();

            boolean willDropTrophy = chance >= deadEntity.level().random.nextDouble();

            if (TrophyManagerConfig.GENERAL.applyLooting.get()) {
                HolderLookup.RegistryLookup<Enchantment> registryLookup = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                // Each level of looting gives an extra roll
                int lootingLevel = EnchantmentHelper.getEnchantmentLevel(registryLookup.getOrThrow(Enchantments.LOOTING), player);
                for (int i = 0; i < (1 + lootingLevel); i++) {
                    willDropTrophy = willDropTrophy || chance >= deadEntity.level().random.nextDouble();
                }
            }

            if (willDropTrophy) {
                CompoundTag entityTag = new CompoundTag();
                deadEntity.saveWithoutId(entityTag);
                ItemStack trophy = TrophyBlock.createTrophy(deadEntity, entityTag);
                Block.popResource(deadEntity.level(), deadEntity.blockPosition(), trophy);
            }
        } else if (TrophyManagerConfig.GENERAL.dropFromPlayers.get() && deadEntity instanceof Player killedPlayer
                && source instanceof ServerPlayer && !(source instanceof FakePlayer)) {
            double chance = TrophyManagerConfig.GENERAL.dropChancePlayers.get();

            boolean willDropTrophy = chance >= deadEntity.level().random.nextDouble();

            if (willDropTrophy) {
                ItemStack trophy = TrophyBlock.createPlayerTrophy(killedPlayer);
                Block.popResource(deadEntity.level(), deadEntity.blockPosition(), trophy);
            }
        }
    }
//
//    private void onAdvancementEarned(final AdvancementEvent.AdvancementEarnEvent event) {
//        if (ModList.get().isLoaded("the_bumblezone")) {
//            ItemStack trophy = null;
//            if (event.getAdvancement().getId().equals(new ResourceLocation("the_bumblezone", "the_bumblezone/the_queens_desire/journeys_end"))) {
//                trophy = TrophyBlock.createTrophy("the_bumblezone:bee_queen", new CompoundTag(), "Queen Bee");
//            } else if (event.getAdvancement().getId().equals(new ResourceLocation("the_bumblezone", "the_bumblezone/beehemoth/queen_beehemoth"))) {
//                trophy = TrophyBlock.createTrophy("the_bumblezone:beehemoth", new CompoundTag(), "Beehemoth");
//            }
//
//            if (trophy != null && event.getEntity().addItem(trophy)) {
//                event.getEntity().drop(trophy, false);
//            }
//        }
//    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public class ClientSetup
    {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.TROPHY.get(), TrophyBlockEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.PLAYER.get(), PlayerTrophyRenderer::new);
        }
    }

    @EventBusSubscriber(modid = MODID)
    public static class ModEventHandler
    {
        @SubscribeEvent
        public static void onEntityAttributeCreate(EntityAttributeCreationEvent event) {
            event.put(ModEntities.PLAYER.get(), Zombie.createAttributes().build());
        }


        @SubscribeEvent
        public static void payloadHandler(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar(MODID).versioned("1").optional();
            registrar.playToClient(
                    PacketOpenGui.TYPE,
                    PacketOpenGui.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            PacketOpenGui::clientHandle,
                            PacketOpenGui::serverHandle
                    )
            );
            registrar.playToServer(
                    PacketUpdateTrophy.TYPE,
                    PacketUpdateTrophy.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            PacketUpdateTrophy::clientHandle,
                            PacketUpdateTrophy::serverHandle
                    )
            );
        }

        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey().equals(CreativeModeTabs.OP_BLOCKS)) {
                String[] entities = {"allay", "axolotl", "bat", "bee", "blaze", "camel", "cat", "cave_spider", "chicken", "cow", "creeper", "dolphin", "donkey", "drowned", "elder_guardian", "ender_dragon", "enderman", "endermite", "evoker", "fox", "frog", "ghast", "glow_squid", "goat", "guardian", "hoglin", "horse", "husk", "illusioner", "iron_golem", "llama", "magma_cube", "mule", "mooshroom", "ocelot", "panda", "parrot", "phantom", "pig", "piglin", "piglin_brute", "pillager", "polar_bear", "pufferfish", "rabbit", "ravager", "sheep", "shulker", "silverfish", "skeleton", "skeleton_horse", "slime", "snow_golem", "spider", "squid", "stray", "strider", "tadpole", "trader_llama", "tropical_fish", "turtle", "vex", "villager", "vindicator", "wandering_trader", "warden", "witch", "wither", "wither_skeleton", "wolf", "zoglin", "zombie", "zombie_horse", "zombie_villager", "zombified_piglin", "sniffer", "bogged", "breeze"};

                for (String entityId : entities) {
                    BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.withDefaultNamespace(entityId)).ifPresent(holder ->
                            event.accept(TrophyBlock.createTrophy(holder, new CompoundTag(), holder.value().getDescriptionId())));
                }
            }
        }

        @SubscribeEvent
        private static void registerDataMap(final RegisterDataMapTypesEvent event) {
            event.register(NBT_MAP);
            event.register(DROP_RATE_MAP);
            event.register(PROPERTIES_MAP);
        }
    }


    @EventBusSubscriber(modid = MODID)
    public static class EventHandler
    {
        @SubscribeEvent
        private static void levelUnload(final LevelEvent.Unload event) {
            TrophyBlockEntity.cachedEntities.clear();
        }
    }

}
