package cy.jdkdigital.trophymanager.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class RenderPlayer extends Zombie
{
    private static final String PROFILE_KEY = "profile";

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

        input.read(PROFILE_KEY, ResolvableProfile.CODEC).ifPresent(this::setProfile);
        setTrophyPose(TrophyPose.byName(input.getStringOr(TrophyPose.NBT_KEY, "")));
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
