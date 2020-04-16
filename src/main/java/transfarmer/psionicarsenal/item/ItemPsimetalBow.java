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
package transfarmer.psionicarsenal.item;

import com.teamwizardry.librarianlib.features.helpers.NBTHelper;
import com.teamwizardry.librarianlib.features.utilities.client.TooltipHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;
import transfarmer.psionicarsenal.Main;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.common.item.tool.IPsimetalTool;

import java.util.List;

public class ItemPsimetalBow extends ItemBow implements IPsimetalTool {
    public ItemPsimetalBow(final String name) {
        super();

        this.setRegistryName(Main.MOD_ID, name);
        this.setTranslationKey(String.format("%s.%s", Main.MOD_ID, name));
        this.setMaxDamage(575);
        this.addPropertyOverride(new ResourceLocation("pull"), (stack, world, entity) ->
                entity != null && entity.getActiveItemStack().getItem() == this ? (float) (stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / 20.0F : 0.0F);
    }

    public static void castSpell(final EntityPlayer player, final ItemStack itemStack, final Vec3d pos, final EntityArrow arrow) {
        final PlayerData data = PlayerDataHandler.get(player);
        final ItemStack playerCad = PsiAPI.getPlayerCAD(player);

        if (itemStack.getItem() instanceof ItemPsimetalBow) {
            final ItemPsimetalBow bow = (ItemPsimetalBow) itemStack.getItem();

            if (!playerCad.isEmpty()) {
                final ItemStack bullet = bow.getBulletInSocket(itemStack, bow.getSelectedSlot(itemStack));

                if (bullet.getItem() instanceof ItemSpellBullet & bullet.getItemDamage() != 1) {
                    NBTTagCompound entityCmp = arrow.getEntityData();
                    NBTTagCompound bulletCmp = new NBTTagCompound();
                    entityCmp.setTag("rpsideas-spellimmune", bulletCmp);
                }

                ItemCAD.cast(player.world, player, data, bullet, playerCad, 5, 10, 0.05F, (context) -> context.tool = itemStack);

                final float radius = 0.2F;
                final AxisAlignedBB region = new AxisAlignedBB(player.posX - (double) radius, player.posY + (double) player.eyeHeight - (double) radius, player.posZ - (double) radius,
                        player.posX + (double) radius, player.posY + (double) player.eyeHeight + (double) radius, player.posZ + (double) radius);
                final List<EntitySpellProjectile> spells = player.world.getEntitiesWithinAABB(EntitySpellProjectile.class, region, (e) ->
                        e != null && e.context.caster == player && e.ticksExisted <= 1);

                for (final EntitySpellProjectile spell : spells) {
                    spell.startRiding(arrow, true);
                }
            }
        }

    }

    public static void regenPsi(ItemStack stack, Entity entityIn, boolean isSelected) {
        if (entityIn instanceof EntityPlayer && stack.getItemDamage() > 0 && !isSelected) {
            final EntityPlayer player = (EntityPlayer) entityIn;
            final PlayerData data = PlayerDataHandler.get(player);
            final int regenTime = NBTHelper.getInt(stack, "regenTime", 0);

            if (!data.overflowed && regenTime % 80 == 0 && (float) data.getAvailablePsi() / (float) data.getTotalPsi() > 0.5F) {
                data.deductPsi(600, 5, true);
                stack.setItemDamage(stack.getItemDamage() - 1);
            }

            NBTHelper.setInt(stack, "regenTime", regenTime + 1);
        }

    }

    @Override
    @NotNull
    protected ItemStack findAmmo(final EntityPlayer player) {
        if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        }

        if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        }

        for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            final ItemStack itemstack = player.inventory.getStackInSlot(i);

            if (this.isArrow(itemstack)) {
                return itemstack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void onPlayerStoppedUsing(final @NotNull ItemStack itemStack, final @NotNull World world, final @NotNull EntityLivingBase enity, final int timeLeft) {
        if (enity instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer) enity;
            final boolean infinity = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, itemStack) > 0;
            ItemStack itemstack = this.findAmmo(player);
            final int i = ForgeEventFactory.onArrowLoose(itemStack, world, player, this.getMaxItemUseDuration(itemStack) - timeLeft, !itemstack.isEmpty() || infinity);

            if (i < 0) {
                return;
            }

            if (!itemstack.isEmpty() || infinity) {
                if (itemstack.isEmpty()) {
                    itemstack = new ItemStack(Items.ARROW);
                }

                float f = getArrowVelocity(i);
                if ((double) f >= 0.1D) {
                    boolean flag1 = player.capabilities.isCreativeMode || itemstack.getItem() instanceof ItemArrow && ((ItemArrow) itemstack.getItem()).isInfinite(itemstack, itemStack, player);
                    if (!world.isRemote) {
                        final ItemArrow itemarrow = (ItemArrow) (itemstack.getItem() instanceof ItemArrow ? itemstack.getItem() : Items.ARROW);
                        final EntityArrow entityarrow = itemarrow.createArrow(world, itemstack, player);

                        entityarrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, f * 3.0F, 1.0F);

                        if (f == 1.0F) {
                            entityarrow.setIsCritical(true);
                        }

                        final int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, itemStack);

                        if (power > 0) {
                            entityarrow.setDamage(entityarrow.getDamage() + (double) power * 0.5D + 0.5D);
                        }

                        final int punch = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, itemStack);

                        if (punch > 0) {
                            entityarrow.setKnockbackStrength(punch);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, itemStack) > 0) {
                            entityarrow.setFire(100);
                        }

                        itemStack.damageItem(1, player);
                        if (flag1 || player.capabilities.isCreativeMode && (itemstack.getItem() == Items.SPECTRAL_ARROW || itemstack.getItem() == Items.TIPPED_ARROW)) {
                            entityarrow.pickupStatus = PickupStatus.CREATIVE_ONLY;
                        }

                        castSpell(player, itemStack, new Vec3d(player.posX, player.posY, player.posZ), entityarrow);
                        world.spawnEntity(entityarrow);
                    }

                    world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!flag1 && !player.capabilities.isCreativeMode) {
                        itemstack.shrink(1);
                        if (itemstack.isEmpty()) {
                            player.inventory.deleteStack(itemstack);
                        }
                    }

                    player.addStat(StatList.getObjectUseStats(this));
                }
            }
        }

    }

    @Override
    public void onUpdate(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        regenPsi(stack, entityIn, isSelected);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, World playerIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
        String componentName = TooltipHelper.local(ISocketable.getSocketedItemName(stack, "psimisc.none"));
        TooltipHelper.addToTooltip(tooltip, "psimisc.spellSelected", componentName);
    }

    @Override
    public boolean getIsRepairable(@NotNull ItemStack toRepair, @NotNull ItemStack repairItem) {
        return OreDictionary.containsMatch(false, OreDictionary.getOres("ingotPsi"), repairItem) || super.getIsRepairable(toRepair, repairItem);
    }

    @Override
    public boolean requiresSneakForSpellSet(ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }
}
