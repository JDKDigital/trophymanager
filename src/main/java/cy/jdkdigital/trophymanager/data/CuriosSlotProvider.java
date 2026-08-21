package cy.jdkdigital.trophymanager.data;

import cy.jdkdigital.trophymanager.TrophyManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.CuriosSlotTypes;

import java.util.concurrent.CompletableFuture;

public class CuriosSlotProvider extends CuriosDataProvider
{
    public CuriosSlotProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(TrophyManager.MODID, output, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries) {
        createEntities("head_slot").addPlayer().addPresetSlots(CuriosSlotTypes.Preset.HEAD);
    }
}
