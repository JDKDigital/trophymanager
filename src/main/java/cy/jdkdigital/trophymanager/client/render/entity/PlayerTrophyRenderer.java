package cy.jdkdigital.trophymanager.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class PlayerTrophyRenderer extends ZombieRenderer
{
    public PlayerTrophyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull Identifier getTextureLocation(ZombieRenderState state) {
        return DefaultPlayerSkin.getDefaultTexture();
    }
}
