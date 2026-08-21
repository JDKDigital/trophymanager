package cy.jdkdigital.trophymanager.common.blockentity;

import com.mojang.logging.LogUtils;
import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TrophyBlockEntity extends BlockEntity
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Integer, Entity> cachedEntities = new HashMap<>();

    public String trophyType = "item"; // item, entity
    public ItemStack item = null;
    public CompoundTag entity = null;
    public double offsetY = 0.0D;
    public float rotX = 0.0F;
    public float rotY = 0.0F;
    public float rotZ = 0.0F;
    public float scale = 1.0F;
    public Identifier baseBlock;
    public boolean isOnHead = false;
    private String name = "";

    public TrophyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TROPHY.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag trophyTag = input.read("TrophyData", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        loadData(trophyTag, input.lookup());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ValueOutput trophy = output.child("TrophyData");
        trophy.putString("TrophyType", trophyType);
        if (item != null && !item.isEmpty()) {
            trophy.store("TrophyItem", ItemStack.CODEC, item);
        }
        if (entity != null) {
            trophy.store("TrophyEntity", CompoundTag.CODEC, entity);
        }
        trophy.putDouble("OffsetY", offsetY);
        trophy.putFloat("RotX", rotX);
        trophy.putFloat("RotY", rotY);
        trophy.putFloat("RotZ", rotZ);
        trophy.putFloat("Scale", scale);
        if (baseBlock != null) {
            trophy.putString("BaseBlock", baseBlock.toString());
        }
        if (name != null) {
            trophy.putString("Name", name);
        }
    }

    public void loadData(CompoundTag tag, HolderLookup.Provider pRegistries) {
        this.trophyType = tag.getStringOr("TrophyType", "item");

        if (tag.contains("TrophyItem")) {
            this.item = ItemStack.OPTIONAL_CODEC.parse(pRegistries.createSerializationContext(NbtOps.INSTANCE), tag.getCompoundOrEmpty("TrophyItem")).result().orElse(ItemStack.EMPTY);
        } else if (this.trophyType.equals("item")) {
            this.item = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        }

        if (tag.contains("TrophyEntity")) {
            this.entity = tag.getCompoundOrEmpty("TrophyEntity");
        }

        this.scale = tag.contains("Scale") ? tag.getFloatOr("Scale", 1.0F) : TrophyManagerConfig.GENERAL.defaultScale.get().floatValue();
        this.rotX = tag.getFloatOr("RotX", 0.0F);
        this.rotY = tag.getFloatOr("RotY", 0.0F);
        this.rotZ = tag.getFloatOr("RotZ", 0.0F);
        this.offsetY = tag.contains("OffsetY") ? tag.getDoubleOr("OffsetY", 0.0D) : TrophyManagerConfig.GENERAL.defaultYOffset.get();
        this.baseBlock = Identifier.parse(tag.contains("BaseBlock") ? tag.getStringOr("BaseBlock", "") : TrophyManagerConfig.GENERAL.defaultBaseBlock.get());

        if (tag.contains("Name")) {
            this.name = tag.getStringOr("Name", "");
        }
    }

    public Entity getCachedEntity() {
        return getCachedEntity(false);
    }

    public Entity getCachedEntity(boolean forceRefresh) {
        if (entity != null) {
            int key = entity.hashCode();
            if (!cachedEntities.containsKey(key) || forceRefresh) {
                Entity cachedEntity = createEntity(level, entity);
                if (cachedEntity != null) {
                    if (cachedEntity instanceof NeutralMob neutralMob && entity.contains("AngerTime")) {
                        neutralMob.setTimeToRemainAngry(entity.getIntOr("AngerTime", 0));
                    } else if (cachedEntity instanceof Shulker shulker && entity.contains("Peek")) {
                        float peek = Mth.clamp(entity.getByteOr("Peek", (byte) 0) * 0.01F, 0.0F, 1.0F);
                        shulker.currentPeekAmount = peek;
                        shulker.currentPeekAmountO = peek;
                    }
                    try {
                        addPassengers(cachedEntity, entity);
                    } catch (Exception e) {
                        // user can fuck it up here, so don't crash
                    }
                } else {
                    TrophyManager.LOGGER.info("Unable to create trophy entity " + entity);
                }
                cachedEntities.put(key, cachedEntity);
            }
            return cachedEntities.getOrDefault(key, null);
        }
        return null;
    }

    private static Entity createEntity(Level level, CompoundTag tag) {
        return createEntity(level, tag.getStringOr("entityType", ""), tag);
    }

    private static Entity createEntity(Level level, String entityType, CompoundTag tag) {
        EntityType<?> type = EntityType.byString(entityType).orElse(null);
        if (type != null) {
            try {
                if (ModList.get().isLoaded("cobblemon") && entityType.contains("cobblemon:")) {
                    return null;
                }
                Entity loadedEntity = type.create(level, EntitySpawnReason.NATURAL);
                if (loadedEntity != null) {
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(loadedEntity.problemPath(), LOGGER)) {
                        loadedEntity.load(TagValueInput.create(reporter, level.registryAccess(), tag));
                    }
                    return loadedEntity;
                }
            } catch (Exception e) {
                TrophyManager.LOGGER.warn("Unable to load trophy entity " + entityType + ". Please report it to the mod author at https://github.com/JDKDigital/trophymanager/issues");
                TrophyManager.LOGGER.warn("Error: " + e.getMessage());
                TrophyManager.LOGGER.warn("Tag: " + tag);
                return null;
            }
        }
        return null;
    }

    private static void addPassengers(Entity vehicle, CompoundTag entityTag) {
        if (entityTag.contains("Passengers")) {
            ListTag passengers = entityTag.getListOrEmpty("Passengers");
            for (int l = 0; l < passengers.size(); ++l) {
                CompoundTag riderTag = passengers.getCompoundOrEmpty(l);
                Entity rider = createEntity(vehicle.level(), riderTag.getStringOr("id", ""), riderTag);
                if (rider != null) {
                    rider.startRiding(vehicle);
                    addPassengers(rider, riderTag);
                }
            }
        }
    }

    public Block getBaseBlock() {
        return baseBlock == null ? null : BuiltInRegistries.BLOCK.get(baseBlock).map(Holder::value).orElse(null);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        super.onDataPacket(net, input);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
        return saveWithoutMetadata(lookupProvider);
    }

    public InteractionResult equip(ItemStack heldItem, EquipmentSlot slot) {
        if (!canEquip(getCachedEntity()) || level == null || entity == null) {
            return InteractionResult.PASS;
        }

        var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        EntityEquipment equipment = entity.getCompound("equipment")
                .flatMap(t -> EntityEquipment.CODEC.parse(ops, t).result())
                .orElseGet(EntityEquipment::new);

        if (ItemStack.isSameItem(equipment.get(slot), heldItem)) {
            equipment.set(slot, ItemStack.EMPTY);
        } else {
            equipment.set(slot, heldItem.copyWithCount(1));
        }

        if (equipment.isEmpty()) {
            entity.remove("equipment");
        } else {
            entity.put("equipment", EntityEquipment.CODEC.encodeStart(ops, equipment).getOrThrow());
        }

        getCachedEntity(true);

        if (level instanceof ServerLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return InteractionResult.CONSUME;
    }

    private boolean canEquip(Entity cachedEntity) {
        return cachedEntity instanceof Mob || cachedEntity instanceof ArmorStand;
    }
}
