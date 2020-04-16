package transfarmer.psionicarsenal.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import transfarmer.psionicarsenal.Main;
import vazkii.arl.interf.IExtraVariantHolder;
import vazkii.arl.util.ModelHandler;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICADAssembly;
import vazkii.psi.common.item.component.ItemCADComponent;

public abstract class ItemToolAssembly extends ItemCADComponent implements ICADAssembly, IExtraVariantHolder {
    public ItemToolAssembly(final String name, final String... variants) {
        super(name, variants);
    }

    @Override
    public String getModNamespace() {
        return Main.MOD_ID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelResourceLocation getCADModel(ItemStack stack, ItemStack cad) {
        return ModelHandler.getModelLocation(cad);
    }

    @Override
    public EnumCADComponent getComponentType(ItemStack stack) {
        return EnumCADComponent.ASSEMBLY;
    }

    @Override
    public String[] getExtraVariants() {
        return new String[0];
    }
}
