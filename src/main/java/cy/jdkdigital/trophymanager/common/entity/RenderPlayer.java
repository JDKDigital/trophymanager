package cy.jdkdigital.trophymanager.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RenderPlayer extends Zombie
{
    private static final String PROFILE_KEY = "profile";
    private static final int FALL_FLYING_FLAG = 7;
    private static final int FALL_FLYING_TICKS = 20;

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
        setSharedFlag(FALL_FLYING_FLAG, trophyPose == TrophyPose.FLYING);
    }

    @Override
    public int getFallFlyingTicks() {
        return hasPose(Pose.FALL_FLYING) ? FALL_FLYING_TICKS : super.getFallFlyingTicks();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains(PROFILE_KEY)) {
            ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, tag.get(PROFILE_KEY)).result().ifPresent(this::setProfile);
        }

        setTrophyPose(TrophyPose.byName(tag.getString(TrophyPose.NBT_KEY)));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (profile != null) {
            ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, profile).result().ifPresent(encoded -> tag.put(PROFILE_KEY, encoded));
        }

        tag.putString(TrophyPose.NBT_KEY, getTrophyPose().name());
    }
}
