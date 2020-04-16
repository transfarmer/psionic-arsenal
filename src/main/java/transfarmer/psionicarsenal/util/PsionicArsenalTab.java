package transfarmer.psionicarsenal.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import transfarmer.psionicarsenal.Main;
import transfarmer.psionicarsenal.item.ModItems;

public class PsionicArsenalTab extends CreativeTabs {
    public PsionicArsenalTab() {
        super(Main.MOD_ID);
    }

    @Override
    public @NotNull ItemStack createIcon() {
        return new ItemStack(ModItems.PSIMETAL_BOW);
    }
}
