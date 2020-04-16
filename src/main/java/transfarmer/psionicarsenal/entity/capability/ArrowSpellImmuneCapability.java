/*Copyright 2019 Dudblockman

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.*/
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
