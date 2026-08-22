package cy.jdkdigital.trophymanager.common.blockentity;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.block.TrophyBlock;
import cy.jdkdigital.trophymanager.compat.CobblemonCompat;
import cy.jdkdigital.trophymanager.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TrophyBlockEntity extends BlockEntity
{
    public static final Map<Integer, Entity> cachedEntities = new HashMap<>();

    public String trophyType = "item"; // item, entity
    public ItemStack item = null;
    public CompoundTag entity = null;
    public double offsetY = 0.0D;
    public float rotX = 0.0F;
    public float rotY = 0.0F;
    public float rotZ = 0.0F;
    public float scale = 1.0F;
    public ResourceLocation baseBlock;
    public boolean spin = false;
    public boolean isOnHead = false;
    private String name = "";
    private String subject = "";

    public TrophyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TROPHY.get(), pos, state);
    }

//    @Override
//    public AABB getRenderBoundingBox() {
//        BlockPos pos = getBlockPos();
//        return new AABB(pos, pos.offset(1, 2, 1));
//    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        loadData(pTag.getCompound("TrophyData"), pRegistries);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);

        CompoundTag trophyTag = new CompoundTag();

        trophyTag.putString("TrophyType", trophyType);

        if (item != null) {
            trophyTag.put("TrophyItem", item.save(pRegistries, new CompoundTag()));
        }

        if (entity != null) {
            trophyTag.put("TrophyEntity", entity);
        }

        trophyTag.putDouble("OffsetY", offsetY);
        trophyTag.putFloat("RotX", rotX);
        trophyTag.putFloat("RotY", rotY);
        trophyTag.putFloat("RotZ", rotZ);
        trophyTag.putFloat("Scale", scale);
        trophyTag.putBoolean("Spin", spin);
        if (baseBlock != null) {
            trophyTag.putString("BaseBlock", baseBlock.toString());
        }
        if (name != null) {
            trophyTag.putString("Name", name);
        }
        if (subject != null) {
            trophyTag.putString("Subject", subject);
        }

        pTag.put("TrophyData", trophyTag);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        if (level == null) {
            return;
        }
        try {
            components.set(DataComponents.CUSTOM_DATA, CustomData.of(saveWithoutMetadata(level.registryAccess()).getCompound("TrophyData")));
        } catch (Exception e) {
            TrophyManager.LOGGER.warn("Could not read the trophy data at " + worldPosition + ": " + e.getMessage());
        }
    }

    public void loadData(CompoundTag tag, HolderLookup.Provider pRegistries) {
        this.trophyType = tag.contains("TrophyType") ? tag.getString("TrophyType") : "item";

        if (tag.contains("TrophyItem")) {
            CompoundTag itemTag = tag.getCompound("TrophyItem");
            if (!itemTag.contains("Count")) {
                itemTag.putDouble("Count", 1D);
            }
            this.item = ItemStack.parse(pRegistries, itemTag).orElse(ItemStack.EMPTY);
        } else if (this.trophyType.equals("item")) {
            // Default
            this.item = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        }

        if (tag.contains("TrophyEntity")) {
            this.entity = tag.getCompound("TrophyEntity");
            resolveProfile();
        }

        if (tag.contains("Scale")) {
            this.scale = tag.getFloat("Scale");
        } else {
            this.scale = TrophyManagerConfig.GENERAL.defaultScale.get().floatValue();
        }

        if (tag.contains("RotX")) {
            this.rotX = tag.getFloat("RotX");
        } else {
            this.rotX = 0.0f;
        }

        if (tag.contains("RotY")) {
            this.rotY = tag.getFloat("RotY");
        } else {
            this.rotY = 0.0f;
        }

        if (tag.contains("RotZ")) {
            this.rotZ = tag.getFloat("RotZ");
        } else {
            this.rotZ = 0.0f;
        }

        this.spin = tag.getBoolean("Spin");

        if (tag.contains("OffsetY")) {
            this.offsetY = tag.getDouble("OffsetY");
        } else {
            this.offsetY = TrophyManagerConfig.GENERAL.defaultYOffset.get();
        }

        if (tag.contains("BaseBlock")) {
            this.baseBlock = ResourceLocation.parse(tag.getString("BaseBlock"));
        } else {
            this.baseBlock = ResourceLocation.parse(TrophyManagerConfig.GENERAL.defaultBaseBlock.get());
        }

        if (tag.contains("Name")) {
            this.name = tag.getString("Name");
        }

        if (tag.contains("Subject")) {
            this.subject = tag.getString("Subject");
        }
    }

    private void resolveProfile() {
        if (entity == null || !entity.contains("profile")) {
            return;
        }
        try {
            ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, entity.get("profile")).result().ifPresent(profile -> {
                if (!profile.isResolved()) {
                    profile.resolve()
                            .thenAcceptAsync(this::storeResolvedProfile, SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR)
                            .exceptionally(throwable -> {
                                TrophyManager.LOGGER.warn("Could not resolve trophy player profile: " + throwable.getMessage());
                                return null;
                            });
                }
            });
        } catch (Exception e) {
            TrophyManager.LOGGER.warn("Malformed player profile on trophy at " + worldPosition + ": " + e.getMessage());
        }
    }

    private void storeResolvedProfile(ResolvableProfile profile) {
        if (entity == null) {
            return;
        }
        try {
            ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, profile).result().ifPresent(encoded -> {
                entity.put("profile", encoded);
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            });
        } catch (Exception e) {
            TrophyManager.LOGGER.warn("Could not store resolved player profile on trophy at " + worldPosition + ": " + e.getMessage());
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
                    if (cachedEntity instanceof NeutralMob && entity.contains("AngerTime")) {
                        ((NeutralMob) cachedEntity).setRemainingPersistentAngerTime(entity.getInt("AngerTime"));
                    } else if (cachedEntity instanceof Shulker && entity.contains("Peek")) {
                        float peek = Mth.clamp(entity.getByte("Peek") * 0.01F, 0.0F, 1.0F);
                        ((Shulker) cachedEntity).currentPeekAmount = peek;
                        ((Shulker) cachedEntity).currentPeekAmountO = peek;
                    } else if (cachedEntity instanceof Strider strider && entity.contains("Suffocating")) {
                        strider.setSuffocating(entity.getBoolean("Suffocating"));
                    }
                    try {
                        addPassengers(cachedEntity, entity);
                    } catch (Exception e) {
                        // user can fuck it up here, so don't crash
                    }
                } else {
                    TrophyManager.LOGGER.info("Unable to create trophy entity " + entity);
                }
                try {
                    addPassengers(cachedEntity, entity);
                } catch (Exception e) {
                    // user can fuck it up here, so don't crash
                }
                TrophyBlockEntity.cachedEntities.put(key, cachedEntity);
            }
            return cachedEntities.getOrDefault(key, null);
        }
        return null;
    }

    private static Entity createEntity(Level level, CompoundTag tag) {
        return createEntity(level, tag.getString("entityType"), tag);
    }

    private static Entity createEntity(Level level, String entityType, CompoundTag tag) {
        EntityType<?> type = EntityType.byString(entityType).orElse(null);
        if (type != null) {
            try {
                Entity loadedEntity;
                if (ModList.get().isLoaded("cobblemon") && entityType.contains("cobblemon:")) {
                    return CobblemonCompat.create(level, tag);
                } else {
                    loadedEntity = type.create(level);
                    if (loadedEntity != null) {
                        loadedEntity.load(tag);
                        return loadedEntity;
                    }
                }
            } catch (Exception e) {
                TrophyManager.LOGGER.warn("Unable to load trophy entity " + entityType + ". Please report it to the mod author at https://github.com/JDKDigital/trophymanager/issues");
                TrophyManager.LOGGER.warn("Error: " + e.getMessage());
                TrophyManager.LOGGER.warn("Tag: " + tag);
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private static void addPassengers(Entity vehicle, CompoundTag entityTag) {
        if (entityTag.contains("Passengers")) {
            ListTag passengers = entityTag.getList("Passengers", 10);
            for (int l = 0; l < passengers.size(); ++l) {
                CompoundTag riderTag = passengers.getCompound(l);
                Entity rider = createEntity(vehicle.level(), riderTag.getString("id"), riderTag);
                if (rider != null) {
                    rider.startRiding(vehicle);
                    addPassengers(rider, riderTag);
                }
            }
        }
    }

    public Block getBaseBlock() {
        return BuiltInRegistries.BLOCK.get(baseBlock);
    }

    public Component getDisplayName() {
        return TrophyBlock.trophyName(subject, name);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        handleUpdateTag(pkt.getTag(), lookupProvider);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
        return saveWithoutMetadata(lookupProvider);
    }

    public ItemInteractionResult equipArmor(ItemStack heldItem) {
        if (!canEquip(getCachedEntity()) || level == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Read existing armor items into list
        ListTag armorList = entity.contains("ArmorItems") ? entity.getList("ArmorItems", 10) : new ListTag();
        NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
        for(int l = 0; l < armorItems.size(); ++l) {
            armorItems.set(l, ItemStack.parse(level.registryAccess(), armorList.getCompound(l)).orElse(ItemStack.EMPTY));
        }
        // Add or remove new armor item
        Item armorItem = heldItem.getItem();
        if (armorItem instanceof ArmorItem) {
            int slot = ((ArmorItem) armorItem).getEquipmentSlot().getIndex();
            if (armorItems.get(slot).getItem().equals(armorItem)) {
                armorItems.set(slot, ItemStack.EMPTY);
            } else {
                armorItems.set(slot, heldItem);
            }
        }

        // Save armor list in NBT
        ListTag listnbt = new ListTag();
        CompoundTag compoundnbt;
        for(Iterator<ItemStack> var3 = armorItems.iterator(); var3.hasNext(); listnbt.add(compoundnbt)) {
            ItemStack itemstack = var3.next();
            compoundnbt = new CompoundTag();
            if (!itemstack.isEmpty()) {
                itemstack.save(level.registryAccess(), compoundnbt);
            }
        }

        entity.put("ArmorItems", listnbt);

        if (level instanceof ServerLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return ItemInteractionResult.CONSUME;
    }

    public ItemInteractionResult equipTool(ItemStack heldItem) {
        if (!canEquip(getCachedEntity()) || level == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Read existing armor items into list
        ListTag handList = entity.contains("HandItems") ? entity.getList("HandItems", 10) : new ListTag();
        NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
        for(int l = 0; l < handItems.size(); ++l) {
            handItems.set(l, ItemStack.parse(level.registryAccess(), handList.getCompound(l)).orElse(ItemStack.EMPTY));
        }
        // Add or remove equipment
        int slot = heldItem.getItem() instanceof ShieldItem ? 1 : 0;
        if (handItems.get(slot).getItem().equals(heldItem.getItem())) {
            handItems.set(slot, ItemStack.EMPTY);
        } else {
            handItems.set(slot, heldItem);
        }

        // Save list in NBT
        ListTag listnbt = new ListTag();
        CompoundTag compoundnbt;
        for(Iterator<ItemStack> var3 = handItems.iterator(); var3.hasNext(); listnbt.add(compoundnbt)) {
            ItemStack itemstack = var3.next();
            compoundnbt = new CompoundTag();
            if (!itemstack.isEmpty()) {
                itemstack.save(level.registryAccess(), compoundnbt);
            }
        }

        entity.put("HandItems", listnbt);

        if (level instanceof ServerLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return ItemInteractionResult.CONSUME;
    }

    private boolean canEquip(Entity cachedEntity) {
        return cachedEntity instanceof Mob || cachedEntity instanceof ArmorStand;
    }
}
