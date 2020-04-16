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
package transfarmer.psionicarsenal.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.ISpellImmune;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.detonator.IDetonationHandler;
import vazkii.psi.common.entity.EntitySpellCharge;
import vazkii.psi.common.entity.EntitySpellGrenade;
import vazkii.psi.common.entity.EntitySpellMine;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemSpellDrive;

public class EntityPsiArrow extends EntityTippedArrow implements ISpellImmune, IDetonationHandler {
    ItemStack spellBullet;
    SpellContext context;

    public EntityPsiArrow(World worldIn) {
        super(worldIn);
    }

    public EntityPsiArrow(World worldIn, EntityLivingBase shooter) {
        super(worldIn, shooter);
    }

    public EntityPsiArrow(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    protected void castSpell(ItemStack stack, SpellContext context) {
        final ItemStack cad = PsiAPI.getPlayerCAD(context.caster);
        final ItemStack colorizer = ((ICAD) cad.getItem()).getComponentInSlot(cad, EnumCADComponent.DYE);
        final EntitySpellProjectile projectile;

        switch (stack.getItemDamage()) {
            case 3:
                projectile = new EntitySpellProjectile(context.caster.getEntityWorld(), context.caster);
                break;
            case 9:
                projectile = new EntitySpellGrenade(context.caster.getEntityWorld(), context.caster);
                break;
            case 11:
                projectile = new EntitySpellCharge(context.caster.getEntityWorld(), context.caster);
                break;
            case 13:
                projectile = new EntitySpellMine(context.caster.getEntityWorld(), context.caster);
                break;
            default:
                projectile = null;
        }

        if (projectile != null) {
            projectile.setInfo(context.caster, colorizer, stack);
            projectile.setPosition(this.posX, this.posY, this.posZ);
            projectile.context = context;
            projectile.getEntityWorld().spawnEntity(projectile);
        }

    }

    @Override
    protected void onHit(@NotNull final RayTraceResult result) {
        super.onHit(result);

        if (this.spellBullet != null && this.context != null) {
            final Spell spell = ItemSpellDrive.getSpell(this.spellBullet);

            this.castSpell(this.spellBullet, this.context);
        }

    }

    @Override
    public boolean isImmune() {
        return true;
    }

    @Override
    public Vec3d objectLocus() {
        return this.getPositionVector();
    }

    @Override
    public void detonate() {
    }
}
