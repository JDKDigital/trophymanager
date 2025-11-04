package cy.jdkdigital.trophymanager.compat;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import cy.jdkdigital.trophymanager.TrophyManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class CobblemonCompat
{
    public static Entity create(Level level, CompoundTag tag) {
        var properties = PokemonProperties.Companion.parse("species=\"" + tag.getCompound("Pokemon").getString("Species") + "\" level=5");
        return properties.createEntity(level);
    }
}
