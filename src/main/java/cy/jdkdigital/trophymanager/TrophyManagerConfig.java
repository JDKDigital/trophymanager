package cy.jdkdigital.trophymanager;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TrophyManagerConfig
{
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SERVER_CONFIG;
    public static final General GENERAL = new General(SERVER_BUILDER);

    static {
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static class General
    {
        public final ModConfigSpec.BooleanValue dropFromPlayers;
        public final ModConfigSpec.BooleanValue dropFromMobs;
        public final ModConfigSpec.BooleanValue applyLooting;
        public final ModConfigSpec.BooleanValue allowFakePlayer;
        public final ModConfigSpec.DoubleValue dropChancePlayers;
        public final ModConfigSpec.DoubleValue dropChanceBoss;
        public final ModConfigSpec.DoubleValue dropChanceMobs;
        public final ModConfigSpec.BooleanValue allowNonOpEdit;
        public final ModConfigSpec.DoubleValue maxSize;
        public final ModConfigSpec.DoubleValue maxYOffset;
        public final ModConfigSpec.ConfigValue<? extends String> defaultBaseBlock;
        public final ModConfigSpec.DoubleValue defaultYOffset;
        public final ModConfigSpec.DoubleValue defaultScale;
        public final ModConfigSpec.ConfigValue<String> nbtMap;

        public General(ModConfigSpec.Builder builder) {
            builder.push("General");

            dropFromPlayers = builder
                    .comment("[WIP] Should trophies drop from players when killed by another player?")
                    .define("dropFromPlayers", false);

            dropFromMobs = builder
                    .comment("Should trophies drop from mobs when killed by a player?")
                    .define("dropFromMobs", true);

            applyLooting = builder
                    .comment("Looting enchant will increase drop change")
                    .define("applyLooting", true);

            allowFakePlayer = builder
                    .comment("Allow fake players (machines) to get trophy drops")
                    .define("allowFakePlayer", true);

            dropChancePlayers = builder
                    .comment("Drop chance for trophies when a player is killed by a player.")
                    .defineInRange("dropChancePlayers", 1.0, 0, 1);

            dropChanceBoss = builder
                    .comment("Drop chance for trophies when a boss entity is killed by a player.")
                    .defineInRange("dropChanceBoss", 0.20, 0, 1);

            dropChanceMobs = builder
                    .comment("Drop chance for trophies when a normal entity is killed by a player.")
                    .defineInRange("dropChanceMobs", 0.02, 0, 1);

            allowNonOpEdit = builder
                    .comment("Allow non opped players to change the settings for a trophy.")
                    .define("allowNonOpEdit", true);

            maxSize = builder
                    .comment("Maximum size multiplier for a trophy.")
                    .defineInRange("maxSize", 20.00, 0, Integer.MAX_VALUE);

            maxYOffset = builder
                    .comment("Maximum Y offset for a trophy.")
                    .defineInRange("maxYOffset", 2000.00, 0, Integer.MAX_VALUE);

            defaultBaseBlock = builder
                    .comment("Block to use for trophies dropped when killing a mob.")
                    .define("defaultBaseBlock", "minecraft:smooth_stone_slab");

            defaultYOffset = builder
                    .comment("Default YOffset for trophies dropped when killing a mob. If defaultBaseBlock is a full block this should be 1.0, slabs 0.5 and carpets 0.1")
                    .defineInRange("defaultYOffset", 0.5, 0, Integer.MAX_VALUE);

            defaultScale = builder
                    .comment("Default size for trophies dropped when killing a mob.")
                    .defineInRange("defaultScale", 0.5, 0, Integer.MAX_VALUE);

            nbtMap = builder
                    .comment("This config has been moved to a datamap at /data/trophymanager/data_maps/entity_type/nbt_map.json")
                    .define("nbtMap", "");

            builder.pop();
        }
    }
}