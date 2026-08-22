package cy.jdkdigital.trophymanager.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ItemTrophyDisplay(List<ItemStack> subjects, List<ItemStack> sides, List<ItemStack> bases, List<ItemStack> spins, List<ItemStack> results)
{
    public boolean spinning() {
        return !spins.isEmpty();
    }
}
