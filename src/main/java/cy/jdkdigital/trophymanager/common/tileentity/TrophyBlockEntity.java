package cy.jdkdigital.trophymanager.common.tileentity;

import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.init.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TrophyBlockEntity extends TileEntity
{
    private static final Map<Integer, Entity> cachedEntities = new HashMap<>();

    public String trophyType; // item, entity
    public ItemStack item = null;
    public CompoundNBT entityTag = null;
    public double offsetY;
    public float scale;
    public ResourceLocation baseBlock;
    private String name;
    private String identifier = "";

    public TrophyBlockEntity() {
        super(ModBlockEntities.TROPHY.get());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AxisAlignedBB(pos, pos.offset(1, 2, 1));
    }

    @Override
    public void load(@Nonnull BlockState blockState, @Nonnull CompoundNBT tag) {
        super.load(blockState, tag);

        loadData(tag.getCompound("TrophyData"));
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT tag) {
        super.save(tag);

        CompoundNBT trophyTag = new CompoundNBT();

        if (trophyType != null) {
            trophyTag.putString("TrophyType", trophyType);

            if (!identifier.isEmpty()) {
                trophyTag.putString("identifier", identifier);
            }

            if (item != null) {
                trophyTag.put("TrophyItem", item.save(new CompoundNBT()));
            }

            if (entityTag != null) {
                trophyTag.put("TrophyEntity", entityTag);
            }

            trophyTag.putDouble("OffsetY", offsetY);
            trophyTag.putFloat("Scale", scale);
            trophyTag.putString("BaseBlock", baseBlock.toString());
            if (name != null) {
                trophyTag.putString("Name", name);
            }

            tag.put("TrophyData", trophyTag);
        }

        return tag;
    }

    public void loadData(CompoundNBT tag) {
        populateDefaultData(tag);

        trophyType = tag.getString("TrophyType");

        if (tag.contains("identifier")) {
            identifier = tag.getString("identifier");
        }

        if (tag.contains("TrophyItem")) {
            CompoundNBT itemTag = tag.getCompound("TrophyItem");
            if (!itemTag.contains("Count")) {
                itemTag.putDouble("Count", 1D);
            }
            item = ItemStack.of(itemTag);
        }

        if (tag.contains("TrophyEntity")) {
            entityTag = tag.getCompound("TrophyEntity");
            if (identifier.isEmpty()) {
                identifier = entityTag.getString("entityType");
            }
        }

        scale = tag.getFloat("Scale");
        offsetY = tag.getDouble("OffsetY");
        baseBlock = new ResourceLocation(tag.getString("BaseBlock"));

        if (tag.contains("Name")) {
            name = tag.getString("Name");
        }
    }

    public static void populateDefaultData(CompoundNBT tag) {
        if (!tag.contains("TrophyType")) {
            tag.putString("TrophyType", "item");
        }
        if (!tag.contains("TrophyItem") && tag.getString("TrophyType").equals("item")) {
            tag.put("TrophyItem", new ItemStack(Items.ENCHANTED_GOLDEN_APPLE).serializeNBT());
        }
        if (!tag.contains("Scale")) {
            tag.putFloat("Scale", 0.5f);
        }
        if (!tag.contains("OffsetY")) {
            tag.putDouble("OffsetY", TrophyManagerConfig.GENERAL.defaultYOffset.get());
        }
        if (!tag.contains("BaseBlock")) {
            tag.putString("BaseBlock", TrophyManagerConfig.GENERAL.defaultBaseBlock.get());
        }
    }

    public Entity getEntity() {
        return getCachedEntity(entityTag);
    }

    public static Entity getCachedEntity(CompoundNBT tag) {
        int key = tag.hashCode();
        if (!cachedEntities.containsKey(key)) {
            Entity cachedEntity = createEntity(TrophyManager.proxy.getWorld(), tag);
            if (cachedEntity instanceof IAngerable && tag.contains("AngerTime")) {
                ((IAngerable) cachedEntity).setRemainingPersistentAngerTime(tag.getInt("AngerTime"));
            } else if (cachedEntity instanceof ShulkerEntity && tag.contains("Peek")) {
                ((ShulkerEntity) cachedEntity).setRawPeekAmount(tag.getInt("Peek"));
            }
            try {
                addPassengers(cachedEntity, tag);
            } catch (Exception e) {
                // user can fuck it up here, so don't crash
            }
            TrophyBlockEntity.cachedEntities.put(key, cachedEntity);
        }
        return cachedEntities.getOrDefault(key, null);
    }

    private static Entity createEntity(World world, CompoundNBT tag) {
        return createEntity(world, tag.getString("entityType"), tag);
    }

    private static Entity createEntity(World world, String entityType, CompoundNBT tag) {
        EntityType<?> type = EntityType.byString(entityType).orElse(null);
        if (type != null) {
            try {
                Entity loadedEntity = type.create(world);
                if (loadedEntity != null) {
                    loadedEntity.load(tag);
                    return loadedEntity;
                }
            } catch (Exception e) {
                TrophyManager.LOGGER.warn("Unable to load trophy entity " + entityType + ". Please report it to the mod author at https://github.com/JDKDigital/trophymanager/issues");
                TrophyManager.LOGGER.warn("Error: " + e.getMessage());
                TrophyManager.LOGGER.warn("NBT: " + tag);
                return null;
            }
        }
        return null;
    }

    private static void addPassengers(Entity vehicle, CompoundNBT entityTag) {
        if (entityTag.contains("Passengers")) {
            ListNBT passengers = entityTag.getList("Passengers", 10);
            for (int l = 0; l < passengers.size(); ++l) {
                CompoundNBT riderTag = passengers.getCompound(l);
                Entity rider = createEntity(TrophyManager.proxy.getWorld(), riderTag.getString("id"), riderTag);
                if (rider != null) {
                    rider.startRiding(vehicle);
                }
                addPassengers(rider, riderTag);
            }
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.getBlockPos(), -1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        handleUpdateTag(null, pkt.getTag());
    }

    @Override
    @Nonnull
    public CompoundNBT getUpdateTag() {
        return this.serializeNBT();
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        deserializeNBT(tag);
    }

    public ActionResultType equipArmor(ItemStack heldItem) {
        if (!canEquip(getEntity())) {
            return ActionResultType.PASS;
        }

        // Read existing armor items into list
        ListNBT armorList = entityTag.contains("ArmorItems") ? entityTag.getList("ArmorItems", 10) : new ListNBT();
        NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
        for(int l = 0; l < armorItems.size(); ++l) {
            armorItems.set(l, ItemStack.of(armorList.getCompound(l)));
        }
        // Add or remove new armor item
        Item armorItem = heldItem.getItem();
        if (armorItem instanceof ArmorItem) {
            int slot = ((ArmorItem) armorItem).getSlot().getIndex();
            if (armorItems.get(slot).getItem().equals(armorItem)) {
                armorItems.set(slot, ItemStack.EMPTY);
            } else {
                armorItems.set(slot, heldItem);
            }
        }

        // Save armor list in NBT
        ListNBT listnbt = new ListNBT();
        CompoundNBT compoundnbt;
        for(Iterator<ItemStack> var3 = armorItems.iterator(); var3.hasNext(); listnbt.add(compoundnbt)) {
            ItemStack itemstack = var3.next();
            compoundnbt = new CompoundNBT();
            if (!itemstack.isEmpty()) {
                itemstack.save(compoundnbt);
            }
        }

        entityTag.remove("ArmorItems");
        if (!listnbt.isEmpty()) {
            entityTag.put("ArmorItems", listnbt);
        }

        if (level instanceof ServerWorld) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return ActionResultType.CONSUME;
    }

    public ActionResultType equipTool(ItemStack heldItem) {
        if (!canEquip(getEntity())) {
            return ActionResultType.PASS;
        }

        // Read existing armor items into list
        ListNBT handList = entityTag.contains("HandItems") ? entityTag.getList("HandItems", 10) : new ListNBT();
        NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
        for(int l = 0; l < handItems.size(); ++l) {
            handItems.set(l, ItemStack.of(handList.getCompound(l)));
        }
        // Add or remove equipment
        int slot = heldItem.getItem() instanceof ShieldItem ? 1 : 0;
        if (handItems.get(slot).getItem().equals(heldItem.getItem())) {
            handItems.set(slot, ItemStack.EMPTY);
        } else {
            handItems.set(slot, heldItem);
        }

        // Save list in NBT
        ListNBT listnbt = new ListNBT();
        CompoundNBT compoundnbt;
        for(Iterator<ItemStack> var3 = handItems.iterator(); var3.hasNext(); listnbt.add(compoundnbt)) {
            ItemStack itemstack = var3.next();
            compoundnbt = new CompoundNBT();
            if (!itemstack.isEmpty()) {
                itemstack.save(compoundnbt);
            }
        }

        entityTag.remove("HandItems");
        if (!listnbt.isEmpty()) {
            entityTag.put("HandItems", listnbt);
        }

        if (level instanceof ServerWorld) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return ActionResultType.CONSUME;
    }

    public boolean canEquip(Entity cachedEntity) {
        return cachedEntity instanceof MobEntity || cachedEntity instanceof ArmorStandEntity;
    }
}
