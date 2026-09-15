package cy.jdkdigital.trophymanager.common.entity;

import cy.jdkdigital.trophymanager.TrophyManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RenderPlayer extends Zombie
{
    private static final String PROFILE_KEY = "profile";
    private static final String LEGACY_UUID_KEY = "uuid";

    private ResolvableProfile profile;

    public RenderPlayer(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public @Nullable ResolvableProfile getProfile() {
        return profile;
    }

    public void setProfile(ResolvableProfile profile) {
        this.profile = profile;
    }

    public TrophyPose getTrophyPose() {
        return TrophyPose.byPose(getPose());
    }

    public void setTrophyPose(TrophyPose trophyPose) {
        setPose(trophyPose.pose());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        input.read(PROFILE_KEY, ResolvableProfile.CODEC).ifPresentOrElse(this::setProfile, () -> readLegacyProfile(input));
        setTrophyPose(TrophyPose.byName(input.getStringOr(TrophyPose.NBT_KEY, "")));
    }

    private void readLegacyProfile(ValueInput input) {
        String uuid = input.getStringOr(LEGACY_UUID_KEY, "");
        if (uuid.isBlank()) {
            return;
        }
        try {
            setProfile(ResolvableProfile.createUnresolved(UUID.fromString(uuid)));
        } catch (IllegalArgumentException e) {
            TrophyManager.LOGGER.warn("Trophy player id " + uuid + " is not a valid uuid");
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        if (profile != null) {
            output.store(PROFILE_KEY, ResolvableProfile.CODEC, profile);
        }
        output.putString(TrophyPose.NBT_KEY, getTrophyPose().name());
    }
}
