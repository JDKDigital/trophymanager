package cy.jdkdigital.trophymanager.common.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cy.jdkdigital.trophymanager.TrophyManager;
import cy.jdkdigital.trophymanager.TrophyManagerConfig;
import cy.jdkdigital.trophymanager.common.blockentity.TrophyBlockEntity;
import cy.jdkdigital.trophymanager.init.ModBlocks;
import cy.jdkdigital.trophymanager.init.ModTags;
//import cy.jdkdigital.trophymanager.network.Networking;
import cy.jdkdigital.trophymanager.network.PacketOpenGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class TrophyBlock extends BaseEntityBlock implements SimpleWaterloggedBlock
{
    public static final MapCodec<TrophyBlock> CODEC = simpleCodec(TrophyBlock::new);

    protected static final VoxelShape SLAB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.9D, 16.0D);

    public TrophyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean useShapeForLightOcclusion(@Nonnull BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(BlockStateProperties.WATERLOGGED).add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SLAB;
    }

    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean isPathfindable(BlockState pState, PathComputationType pPathComputationType) {
        return false;
    }

    @Override
    public void setPlacedBy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity player, @Nonnull ItemStack stack) {
        // Read data from stack
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (!level.isClientSide() && tileEntity instanceof TrophyBlockEntity && stack.has(DataComponents.CUSTOM_DATA)) {
            ((TrophyBlockEntity) tileEntity).loadData(stack.get(DataComponents.CUSTOM_DATA).copyTag(), level.registryAccess());
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrophyBlockEntity(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = new ItemStack(ModBlocks.TROPHY.get());
        if (level.getBlockEntity(pos) instanceof TrophyBlockEntity trophyTile && trophyTile.getLevel() != null) {
            try {
                CompoundTag tag = trophyTile.saveWithoutMetadata(trophyTile.getLevel().registryAccess());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.getCompound("TrophyData")));
            } catch (Exception e) {
                // Crash can happen here if the server is shutting down as the client (WAILA) is trying to read the data
            }
        }
        return stack;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pStack.getItem() instanceof BlockItem) {
            Block heldBlock = ((BlockItem) pStack.getItem()).getBlock();
            if (heldBlock.defaultBlockState().is(ModTags.TROPHY_BASE)) {
                final BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
                if (blockEntity instanceof TrophyBlockEntity) {
                    ((TrophyBlockEntity) blockEntity).baseBlock = BuiltInRegistries.BLOCK.getKey(heldBlock);
                    if (!pLevel.isClientSide()) {
                        pLevel.setBlockAndUpdate(pPos, pState);
                    }
                    return ItemInteractionResult.CONSUME;
                }
            }
        }

        if (pStack.getItem() instanceof ArmorItem) {
            final BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof TrophyBlockEntity) {
                var res = ((TrophyBlockEntity) blockEntity).equipArmor(pStack);
                if (!pLevel.isClientSide() && res.equals(ItemInteractionResult.CONSUME)) {
                    pLevel.setBlockAndUpdate(pPos, pState);
                }
                return res;
            }
        }

        if (pStack.getItem() instanceof TieredItem || pStack.getItem() instanceof ShieldItem) {
            final BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof TrophyBlockEntity) {
                var res = ((TrophyBlockEntity) blockEntity).equipTool(pStack);
                if (!pLevel.isClientSide() && res.equals(ItemInteractionResult.CONSUME)) {
                    pLevel.setBlockAndUpdate(pPos, pState);
                }
                return res;
            }
        }

        return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        TrophyManager.LOGGER.info("useWithoutItem");
        if (TrophyManagerConfig.GENERAL.allowNonOpEdit.get() || pPlayer.hasPermissions(2)) {
            final BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof TrophyBlockEntity && pPlayer instanceof ServerPlayer serverPlayer) {
                if (!pLevel.isClientSide()) {
                    PacketDistributor.sendToPlayer(serverPlayer, new PacketOpenGui(blockEntity.getBlockPos()));
                }
                return InteractionResult.SUCCESS_NO_ITEM_USED;
            }
        }

        return super.useWithoutItem(pState, pLevel, pPos, pPlayer, pHitResult);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean condition) {
        if (!level.isClientSide) {
            if (level.hasNeighborSignal(pos)) {
                BlockEntity te = level.getBlockEntity(pos);
                if (te instanceof TrophyBlockEntity) {
                    if (((TrophyBlockEntity) te).trophyType.equals("entity")) {
                        String entity = ((TrophyBlockEntity) te).entity.getString("entityType");
                        switch (entity) {
                            case "minecraft:creeper":
                                level.playSound(null, pos, SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:iron_golem":
                                level.playSound(null, pos, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.NEUTRAL, 1.0F, 1.0F);
                                break;
                            case "minecraft:snow_golem":
                                level.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:ender_dragon":
                                level.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:bee":
                                level.playSound(null, pos, SoundEvents.BEEHIVE_WORK, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:hoglin":
                                level.playSound(null, pos, SoundEvents.HOGLIN_ANGRY, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:zoglin":
                                level.playSound(null, pos, SoundEvents.ZOGLIN_ANGRY, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:slime":
                                level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:turtle":
                                level.playSound(null, pos, SoundEvents.TURTLE_AMBIENT_LAND, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:llama":
                            case "minecraft:trader_llama":
                                if (level.random.nextInt(10) == 1) {
                                    level.playSound(null, pos, SoundEvents.LLAMA_SPIT, SoundSource.HOSTILE, 1.0F, 1.0F);
                                    break;
                                }
                            case "minecraft:tropical_fish":
                            case "minecraft:pufferfish":
                                level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 1.0F, 1.0F);
                                break;
                            case "minecraft:ghast":
                                if (level.random.nextInt(10) == 1) {
                                    level.playSound(null, pos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
                                } else {
                                    level.playSound(null, pos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 1.0F, 1.0F);
                                }
                                break;
                            case "minecraft:goat":
                                if (level.random.nextInt(2) == 1) {
                                    level.playSound(null, pos, SoundEvents.GOAT_SCREAMING_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
                                } else {
                                    level.playSound(null, pos, SoundEvents.GOAT_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
                                }
                                break;
                            default:
                                Entity e = ((TrophyBlockEntity) te).getCachedEntity();
                                if (e instanceof Mob) {
                                    SoundEvent sound = ((Mob) e).getAmbientSound();
                                    if (sound != null) {
                                        level.playSound(null, pos, sound, SoundSource.HOSTILE, 1.0F, 1.0F);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    public static ItemStack createPlayerTrophy(Player player) {
        CompoundTag trophyTag = new CompoundTag();
        ItemStack trophy = new ItemStack(ModBlocks.TROPHY.get());
        trophyTag.putString("TrophyType", "entity");

        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("entityType", "trophymanager:player");
        entityTag.putString("uuid", player.getUUID().toString());
        trophyTag.putString("Name", player.getDisplayName().getString() + " trophy");
        trophyTag.put("TrophyEntity", entityTag);

        trophy.set(DataComponents.CUSTOM_DATA, CustomData.of(trophyTag));

        return trophy;
    }

    public static ItemStack createTrophy(Entity entity, CompoundTag tag) {
        Component name = entity.getDisplayName();
        return createTrophy(entity.getEncodeId(), tag, name.getString());
    }

    public static ItemStack createTrophy(String entityId, CompoundTag tag, String name) {
        CompoundTag entityTag = new CompoundTag();
        Arrays.asList(TrophyManagerConfig.GENERAL.nbtMap.get().split(",")).forEach(s -> {
            String[] nbtInfo = s.split(":");
            if (nbtInfo.length == 3 && (nbtInfo[0] + ":" + nbtInfo[1]).equals(entityId) && tag.contains(nbtInfo[2]) && tag.get(nbtInfo[2]) != null) {
                entityTag.put(nbtInfo[2], tag.get(nbtInfo[2]));
            }
        });

        CompoundTag trophyTag = new CompoundTag();
        ItemStack trophy = new ItemStack(ModBlocks.TROPHY.get());
        trophyTag.putString("TrophyType", "entity");
        entityTag.putString("entityType", entityId);
        if (tag.contains("Age")) {
            if (tag.getInt("Age") < 0) {
                entityTag.putInt("Age", -1);
            }
        }
        switch (entityId) {
            case "axolotl" -> entityTag.putInt("Variant", 4);
            case "ender_dragon" -> {
                trophyTag.putFloat("Scale", 0.1f);
                trophyTag.putDouble("OffsetY", 0.8d);
            }
            case "ghast" -> {
                trophyTag.putFloat("Scale", 0.4f);
                trophyTag.putDouble("OffsetY", 1.4d);
            }
            case "bee", "phantom", "vex" -> trophyTag.putDouble("OffsetY", 0.8d);
            case "pufferfish" -> entityTag.putInt("PuffState", 1);
            case "shulker" -> {
                entityTag.putInt("Color", 2);
                entityTag.putInt("Peek", 30);
            }
            case "glow_squid" -> {
                trophyTag.putFloat("Scale", 0.4f);
                trophyTag.putFloat("RotX", 70f);
                trophyTag.putDouble("OffsetY", 0.7d);
            }
        }

        trophyTag.put("TrophyEntity", entityTag);
        trophyTag.putString("Name", name + " trophy");

        trophy.set(DataComponents.CUSTOM_DATA, CustomData.of(trophyTag));

        return trophy;
    }
}
