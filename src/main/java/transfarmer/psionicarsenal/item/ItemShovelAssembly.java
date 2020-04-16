package transfarmer.psionicarsenal.item;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.cad.EnumCADStat;

import java.util.List;

public class ItemShovelAssembly extends ItemToolAssembly {
    public static final String[] VARIANTS = new String[]{"shovel/shovel_assembly_iron", "shovel/shovel_assembly_gold", "shovel/shovel_assembly_psimetal", "shovel/shovel_assembly_ebony_psimetal", "shovel/shovel_assembly_ivory_psimetal", "shovel/shovel_assembly_creative"};

    public ItemShovelAssembly(String name) {
        super(name, VARIANTS);
    }

    @Override
    public void registerStats() {
        this.addStat(EnumCADStat.EFFICIENCY, 0, 70);
        this.addStat(EnumCADStat.POTENCY, 0, 100);
        this.addStat(EnumCADStat.EFFICIENCY, 1, 65);
        this.addStat(EnumCADStat.POTENCY, 1, 150);
        this.addStat(EnumCADStat.EFFICIENCY, 2, 80);
        this.addStat(EnumCADStat.POTENCY, 2, 250);
        this.addStat(EnumCADStat.EFFICIENCY, 3, 90);
        this.addStat(EnumCADStat.POTENCY, 3, 350);
        this.addStat(EnumCADStat.EFFICIENCY, 4, 95);
        this.addStat(EnumCADStat.POTENCY, 4, 320);
        this.addStat(EnumCADStat.EFFICIENCY, 5, -1);
        this.addStat(EnumCADStat.POTENCY, 5, -1);
    }

    @Override
    public ItemStack createCADStack(ItemStack stack, List<ItemStack> allComponents) {
        return ItemShovelCad.makeCAD(allComponents);
    }

    @Override
    @NotNull
    public String[] getVariants() {
        return VARIANTS;
    }
}
