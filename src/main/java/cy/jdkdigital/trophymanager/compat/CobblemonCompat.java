package cy.jdkdigital.trophymanager.compat;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures;
import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CobblemonCompat
{
    public static Entity create(Level level, CompoundTag tag) {
        ListTag features = tag.getCompound("Pokemon").getList("Features", ListTag.TAG_COMPOUND);
        Set<String> extra = new HashSet<>();
        if (!features.isEmpty()) {
            for (Tag t : features) {
                CompoundTag feature = (CompoundTag)t;
                String featureName = feature.getString("cobblemon:feature_id");
                var speciesFeature = SpeciesFeatures.getFeature(featureName);
                if (speciesFeature != null && speciesFeature.invoke(feature) instanceof FlagSpeciesFeature) {
                    if (feature.getBoolean(featureName)) {
                        extra.add(featureName);
                    }
                }
            }
        }
        if (tag.getCompound("Pokemon").contains("Shiny") && tag.getCompound("Pokemon").getBoolean("Shiny")) {
            extra.add("shiny");
        }
        var properties = PokemonProperties.Companion.parse("species=\"" + tag.getCompound("Pokemon").getString("Species") + "\" level=5");
        var entity = properties.createEntity(level);
        entity.getEntityData().set(PokemonEntity.Companion.getPOSE_TYPE(), PoseType.valueOf(tag.getString("PoseType")));
        entity.getEntityData().set(PokemonEntity.Companion.getASPECTS(), extra);
        return entity;
    }

    public static void changePose(TrophyBlockEntity trophy, int i) {
        if (trophy.entity.contains("PoseType")) {
            int currentPoseIndex = poses().indexOf(PoseType.valueOf(trophy.entity.getString("PoseType")));
            int nextPoseIndex = currentPoseIndex == 0 && i < 0 ? poses().size() -1 : currentPoseIndex == poses().size() -1 ? 0 : currentPoseIndex + i;
            trophy.entity.putString("PoseType", poses().get(nextPoseIndex).name());
        }
    }

    public static boolean isPokemonTrophy(TrophyBlockEntity trophy) {
        return trophy.trophyType.equals("entity") && trophy.entity.getString("entityType").contains("cobblemon:");
    }

    private static List<PoseType> poses() {
        return List.of(
                PoseType.SLEEP,
                PoseType.SWIM,
                PoseType.FLOAT,
                PoseType.FLY,
                PoseType.HOVER,
                PoseType.WALK,
                PoseType.STAND
        );
    }

    public static String getPose(TrophyBlockEntity trophy) {
        return trophy.entity.contains("PoseType") ? trophy.entity.getString("PoseType") : "NO POSE";
    }
}
