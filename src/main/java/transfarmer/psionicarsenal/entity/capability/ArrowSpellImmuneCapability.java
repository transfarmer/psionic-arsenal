package transfarmer.psionicarsenal.entity.capability;

import com.teamwizardry.librarianlib.features.helpers.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import vazkii.psi.api.spell.ISpellImmune;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArrowSpellImmuneCapability implements ISpellImmune, ICapabilityProvider {
    public final Entity entity;

    public ArrowSpellImmuneCapability(Entity entity) {
        this.entity = entity;
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == ISpellImmune.CAPABILITY && NBTHelper.hasKey(this.entity.getEntityData(), "rpsideas-spellimmune");
    }

    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ISpellImmune.CAPABILITY) {
            return NBTHelper.hasKey(this.entity.getEntityData(), "rpsideas-spellimmune") ? ISpellImmune.CAPABILITY.cast(this) : null;
        } else {
            return null;
        }
    }

    public boolean isImmune() {
        return true;
    }
}
