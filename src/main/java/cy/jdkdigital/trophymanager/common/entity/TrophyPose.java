package cy.jdkdigital.trophymanager.common.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;

import java.util.List;
import java.util.Locale;

public enum TrophyPose
{
    STANDING(Pose.STANDING),
    SITTING(Pose.SITTING),
    FLYING(Pose.FALL_FLYING);

    public static final String NBT_KEY = "Pose";

    private static final List<TrophyPose> CYCLE = List.of(values());

    private final Pose pose;

    TrophyPose(Pose pose) {
        this.pose = pose;
    }

    public Pose pose() {
        return pose;
    }

    public Component label() {
        return Component.translatable("gui.trophy.pose." + name().toLowerCase(Locale.ROOT));
    }

    public TrophyPose cycle(int direction) {
        int index = (CYCLE.indexOf(this) + direction) % CYCLE.size();
        return CYCLE.get(index < 0 ? index + CYCLE.size() : index);
    }

    public static TrophyPose byName(String name) {
        for (TrophyPose trophyPose : CYCLE) {
            if (trophyPose.name().equals(name)) {
                return trophyPose;
            }
        }
        return STANDING;
    }

    public static TrophyPose byPose(Pose pose) {
        for (TrophyPose trophyPose : CYCLE) {
            if (trophyPose.pose == pose) {
                return trophyPose;
            }
        }
        return STANDING;
    }
}
