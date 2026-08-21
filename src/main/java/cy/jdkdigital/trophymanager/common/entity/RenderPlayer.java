package cy.jdkdigital.trophymanager.common.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RenderPlayer extends Zombie
{
    static final EntityDataAccessor<String> DATA_UUID = SynchedEntityData.defineId(RenderPlayer.class, EntityDataSerializers.STRING);

    public RenderPlayer(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_UUID, "");
    }

    public void setUUIDData(String uuid) {
        this.getEntityData().set(DATA_UUID, uuid);
    }

    public String getUUIDData() {
        return this.getEntityData().get(DATA_UUID);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setUUIDData(input.getStringOr("uuid", ""));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (!this.getUUIDData().isEmpty()) {
            output.putString("uuid", this.getUUIDData());
        }
    }
}
