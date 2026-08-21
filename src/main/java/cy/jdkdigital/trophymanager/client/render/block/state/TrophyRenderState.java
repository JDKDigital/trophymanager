package cy.jdkdigital.trophymanager.client.render.block.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TrophyRenderState extends BlockEntityRenderState
{
    public boolean isOnHead;
    public double offsetY;
    public float rotX;
    public float rotY;
    public float rotZ;
    public float scale = 1.0F;

    public boolean renderItem;
    public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public boolean itemIsBlock;
    public double itemBob;
    public float itemSpin;

    public EntityRenderState entityRenderState;
    public final List<Passenger> passengers = new ArrayList<>();
    public float facingAngle;
    public boolean isEnderDragon;

    public boolean hasBase;
    public final BlockModelRenderState baseModelState = new BlockModelRenderState();

    public static class Passenger
    {
        public EntityRenderState state;
        public Vec3 offset;
    }
}
