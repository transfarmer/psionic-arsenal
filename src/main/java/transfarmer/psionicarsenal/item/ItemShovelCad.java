package transfarmer.psionicarsenal.item;

import com.teamwizardry.librarianlib.features.helpers.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import vazkii.arl.item.ItemModShovel;
import vazkii.arl.network.NetworkHandler;
import vazkii.arl.util.TooltipHandler;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.CADStatEvent;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.cad.ICADAssembly;
import vazkii.psi.api.cad.ICADColorizer;
import vazkii.psi.api.cad.ICADComponent;
import vazkii.psi.api.cad.ICADData;
import vazkii.psi.api.internal.PsiRenderHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.PreSpellCastEvent;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCastEvent;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.BlockProgrammer;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.core.handler.capability.CADData;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.tool.IPsimetalTool;
import vazkii.psi.common.network.message.MessageCADDataSync;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemShovelCad extends ItemModShovel implements IToolCAD {
    private static final Pattern VECTOR_PREFIX_PATTERN = Pattern.compile("^storedVector(\\d+)$");

    public ItemShovelCad(final String name) {
        super(name, ItemMaterials.PSIMETAL_TOOL_MATERIAL);
    }

    public static boolean cast(World world, EntityPlayer player, PlayerData data, ItemStack bullet, ItemStack cad, int cd, int particles, float sound, Consumer<SpellContext> predicate) {
        if (!data.overflowed && data.getAvailablePsi() > 0 && !cad.isEmpty() && !bullet.isEmpty() && ISpellAcceptor.hasSpell(bullet) && IToolCAD.isTruePlayer(player)) {
            ISpellAcceptor spellContainer = ISpellAcceptor.acceptor(bullet);
            Spell spell = spellContainer.getSpell();
            SpellContext context = (new SpellContext()).setPlayer(player).setSpell(spell);
            if (predicate != null) {
                predicate.accept(context);
            }

            if (context.isValid()) {
                if (context.cspell.metadata.evaluateAgainst(cad)) {
                    int cost = getRealCost(cad, bullet, context.cspell.metadata.stats.get(EnumSpellStat.COST));
                    PreSpellCastEvent event = new PreSpellCastEvent(cost, sound, particles, cd, spell, context, player, data, cad, bullet);
                    if (MinecraftForge.EVENT_BUS.post(event)) {
                        String cancelMessage = event.getCancellationMessage();
                        if (cancelMessage != null && !cancelMessage.isEmpty()) {
                            player.sendMessage((new TextComponentTranslation(cancelMessage)).setStyle((new Style()).setColor(TextFormatting.RED)));
                        }

                        return false;
                    }

                    cd = event.getCooldown();
                    particles = event.getParticles();
                    sound = event.getSound();
                    cost = event.getCost();
                    spell = event.getSpell();
                    context = event.getContext();
                    if (cost > 0) {
                        data.deductPsi(cost, cd, true);
                    }

                    if (cost != 0 && sound > 0.0F) {
                        if (!world.isRemote) {
                            world.playSound(null, player.posX, player.posY, player.posZ, PsiSoundHandler.cadShoot, SoundCategory.PLAYERS, sound, (float) (0.5D + Math.random() * 0.5D));
                        } else {
                            int color = Psi.proxy.getColorForCAD(cad);
                            float r = (float) PsiRenderHelper.r(color) / 255.0F;
                            float g = (float) PsiRenderHelper.g(color) / 255.0F;
                            float b = (float) PsiRenderHelper.b(color) / 255.0F;

                            for (int i = 0; i < particles; ++i) {
                                double x = player.posX + (Math.random() - 0.5D) * 2.1D * (double) player.width;
                                double y = player.posY - player.getYOffset();
                                double z = player.posZ + (Math.random() - 0.5D) * 2.1D * (double) player.width;
                                float grav = -0.15F - (float) Math.random() * 0.03F;
                                Psi.proxy.sparkleFX(x, y, z, r, g, b, grav, 0.25F, 15);
                            }

                            double x = player.posX;
                            double y = player.posY + (double) player.getEyeHeight() - 0.1D;
                            double z = player.posZ;
                            Vector3 lookOrig = new Vector3(player.getLookVec());

                            for (int i = 0; i < 25; ++i) {
                                Vector3 look = lookOrig.copy();
                                double spread = 0.25D;
                                look.x += (Math.random() - 0.5D) * spread;
                                look.y += (Math.random() - 0.5D) * spread;
                                look.z += (Math.random() - 0.5D) * spread;
                                look.normalize().multiply(0.15D);
                                Psi.proxy.sparkleFX(x, y, z, r, g, b, (float) look.x, (float) look.y, (float) look.z, 0.3F, 5);
                            }
                        }
                    }

                    if (!world.isRemote) {
                        spellContainer.castSpell(context);
                    }

                    MinecraftForge.EVENT_BUS.post(new SpellCastEvent(spell, context, player, data, cad, bullet));
                    return true;
                }

                if (!world.isRemote) {
                    player.sendMessage((new TextComponentTranslation("psimisc.weakCad")).setStyle((new Style()).setColor(TextFormatting.RED)));
                }
            }
        }

        return false;
    }

    public static int getRealCost(ItemStack stack, ItemStack bullet, int cost) {
        if (!stack.isEmpty() && stack.getItem() instanceof ICAD) {
            int eff = ((ICAD) stack.getItem()).getStatValue(stack, EnumCADStat.EFFICIENCY);
            if (eff == -1) {
                return -1;
            } else if (eff == 0) {
                return cost;
            } else {
                double effPercentile = (double) eff / 100.0D;
                double procCost = (double) cost / effPercentile;
                if (!bullet.isEmpty() && ISpellAcceptor.isContainer(bullet)) {
                    procCost *= ISpellAcceptor.acceptor(bullet).getCostModifier();
                }

                return (int) procCost;
            }
        } else {
            return cost;
        }
    }

    public static void setComponent(ItemStack stack, ItemStack componentStack) {
        if (stack.getItem() instanceof ICAD) {
            ((ICAD) stack.getItem()).setCADComponent(stack, componentStack);
        }

    }

    public static ItemStack makeCAD(ItemStack... components) {
        return makeCAD(Arrays.asList(components));
    }

    public static ItemStack makeCADWithAssembly(ItemStack assembly, List<ItemStack> components) {
        ItemStack cad = assembly.getItem() instanceof ICADAssembly ? ((ICADAssembly) assembly.getItem()).createCADStack(assembly, components) : new ItemStack(ModItems.SHOVEL_CAD);
        return makeCAD(cad, components);
    }

    public static ItemStack makeCAD(List<ItemStack> components) {
        return makeCAD(new ItemStack(ModItems.SHOVEL_CAD), components);
    }

    public static ItemStack makeCAD(ItemStack base, List<ItemStack> components) {
        ItemStack stack = base.copy();

        for (final ItemStack component : components) {
            setComponent(stack, component);
        }

        return stack;
    }

    @SideOnly(Side.CLIENT)
    public static String local(String s) {
        return TooltipHandler.local(s);
    }

    private ICADData getCADData(ItemStack stack) {
        return ICADData.hasData(stack) ? ICADData.data(stack) : new CADData();
    }

    @Override
    public boolean hitEntity(@NotNull ItemStack stack, @NotNull EntityLivingBase target, @NotNull EntityLivingBase attacker) {
        if (attacker instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) attacker;
            PlayerData data = PlayerDataHandler.get(entityplayer);
            int cost = 150 / (1 + EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack));
            data.deductPsi(cost * 2, 0, true, false);
        }

        return true;
    }

    public void castOnBlockBreak(ItemStack itemstack, EntityPlayer player) {
        PlayerData data = PlayerDataHandler.get(player);
        ItemStack playerCad = PsiAPI.getPlayerCAD(player);
        if (!playerCad.isEmpty()) {
            ItemStack bullet = this.getBulletInSocket(itemstack, this.getSelectedSlot(itemstack));
            ItemCAD.cast(player.getEntityWorld(), player, data, bullet, playerCad, 5, 10, 0.05F, (context) -> {
                context.tool = itemstack;
                context.positionBroken = IPsimetalTool.raytraceFromEntity(player.getEntityWorld(), player, false, player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE).getAttributeValue());
            });
        }

    }

    @Override
    public boolean onBlockStartBreak(@NotNull ItemStack itemstack, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        super.onBlockStartBreak(itemstack, pos, player);
        this.castOnBlockBreak(itemstack, player);
        return false;
    }

    @Override
    public boolean onBlockDestroyed(@NotNull ItemStack stack, @NotNull World worldIn, IBlockState state, @NotNull BlockPos pos, @NotNull EntityLivingBase entityLiving) {
        if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
            if (entityLiving instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityLiving;
                PlayerData data = PlayerDataHandler.get(entityplayer);
                int cost = 150 / (1 + EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack));
                data.deductPsi(cost, 0, true, false);
            }

            return true;
        } else {
            return true;
        }
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound nbt) {
        CADData data = new CADData();
        if (nbt != null && nbt.hasKey("Parent", 10)) {
            data.deserializeNBT(nbt.getCompoundTag("Parent"));
        }

        return data;
    }

    @Override
    public void onUpdate(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        NBTTagCompound compound = NBTHelper.getOrCreateNBT(stack);
        if (ICADData.hasData(stack)) {
            ICADData data = ICADData.data(stack);
            if (compound.hasKey("time", 99)) {
                data.setTime(compound.getInteger("time"));
                data.markDirty(true);
                compound.removeTag("time");
            }

            if (compound.hasKey("storedPsi", 99)) {
                data.setBattery(compound.getInteger("storedPsi"));
                data.markDirty(true);
                compound.removeTag("storedPsi");
            }

            Set<String> keys = new HashSet<>(compound.getKeySet());

            for (final String key : keys) {
                Matcher matcher = VECTOR_PREFIX_PATTERN.matcher(key);
                if (matcher.find()) {
                    NBTTagCompound vec = compound.getCompoundTag(key);
                    compound.removeTag(key);
                    int memory = Integer.parseInt(matcher.group(1));
                    Vector3 vector = new Vector3(vec.getDouble("x"), vec.getDouble("y"), vec.getDouble("z"));
                    data.setSavedVector(memory, vector);
                }
            }

            if (entityIn instanceof EntityPlayerMP && data.isDirty()) {
                NetworkHandler.INSTANCE.sendTo(new MessageCADDataSync(data), (EntityPlayerMP) entityIn);
                data.markDirty(false);
            }
        }

    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(EntityPlayer player, World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        Block block = world.getBlockState(pos).getBlock();
        return block == ModBlocks.programmer ? ((BlockProgrammer) block).setSpell(world, pos, player, stack) : EnumActionResult.PASS;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@NotNull World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack itemStackIn = player.getHeldItem(hand);
        PlayerData data = PlayerDataHandler.get(player);
        ItemStack playerCad = PsiAPI.getPlayerCAD(player);
        if (playerCad != itemStackIn) {
            if (!world.isRemote) {
                player.sendMessage((new TextComponentTranslation("psimisc.multipleCads")).setStyle((new Style()).setColor(TextFormatting.RED)));
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
        } else {
            ItemStack bullet = this.getBulletInSocket(itemStackIn, this.getSelectedSlot(itemStackIn));
            boolean did = cast(world, player, data, bullet, itemStackIn, 40, 25, 0.5F, (ctx) -> ctx.castFrom = hand);
            if (!data.overflowed && bullet.isEmpty() && craft(player, "dustRedstone", new ItemStack(vazkii.psi.common.item.base.ModItems.material))) {
                world.playSound(null, player.posX, player.posY, player.posZ, PsiSoundHandler.cadShoot, SoundCategory.PLAYERS, 0.5F, (float) (0.5D + Math.random() * 0.5D));
                data.deductPsi(100, 60, true);
                if (data.level == 0) {
                    data.levelUp();
                }

                did = true;
            }

            return new ActionResult<>(did ? EnumActionResult.SUCCESS : EnumActionResult.PASS, itemStackIn);
        }
    }

    @Override
    public void setSpell(EntityPlayer player, ItemStack stack, Spell spell) {
        int slot = this.getSelectedSlot(stack);
        ItemStack bullet = this.getBulletInSocket(stack, slot);
        if (!bullet.isEmpty() && ISpellAcceptor.isAcceptor(bullet)) {
            ISpellAcceptor.acceptor(bullet).setSpell(player, spell);
            this.setBulletInSocket(stack, slot, bullet);
            player.getCooldownTracker().setCooldown(stack.getItem(), 10);
        }

    }

    @Override
    public ItemStack getComponentInSlot(ItemStack stack, EnumCADComponent type) {
        String name = "component" + type.name();
        NBTTagCompound cmp = NBTHelper.getCompound(stack, name);
        return cmp == null ? ItemStack.EMPTY : new ItemStack(cmp);
    }

    @Override
    public int getStatValue(ItemStack stack, EnumCADStat stat) {
        int statValue = 0;
        ItemStack componentStack = this.getComponentInSlot(stack, stat.getSourceType());
        if (!componentStack.isEmpty() && componentStack.getItem() instanceof ICADComponent) {
            ICADComponent component = (ICADComponent) componentStack.getItem();
            statValue = component.getCADStatValue(componentStack, stat);
        }

        CADStatEvent event = new CADStatEvent(stat, stack, componentStack, statValue);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getStatValue();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpellColor(ItemStack stack) {
        ItemStack dye = this.getComponentInSlot(stack, EnumCADComponent.DYE);
        return !dye.isEmpty() && dye.getItem() instanceof ICADColorizer ? ((ICADColorizer) dye.getItem()).getColor(dye) : 1295871;
    }

    @Override
    public boolean isSocketSlotAvailable(ItemStack stack, int slot) {
        int sockets = this.getStatValue(stack, EnumCADStat.SOCKETS);
        if (sockets == -1 || sockets > 12) {
            sockets = 12;
        }

        return slot < sockets;
    }

    @Override
    public ItemStack getBulletInSocket(ItemStack stack, int slot) {
        String name = "bullet" + slot;
        NBTTagCompound cmp = NBTHelper.getCompound(stack, name);
        return cmp == null ? ItemStack.EMPTY : new ItemStack(cmp);
    }

    @Override
    public void setBulletInSocket(ItemStack stack, int slot, ItemStack bullet) {
        String name = "bullet" + slot;
        NBTTagCompound cmp = new NBTTagCompound();
        if (!bullet.isEmpty()) {
            bullet.writeToNBT(cmp);
        }

        NBTHelper.setCompound(stack, name, cmp);
    }

    @Override
    public int getSelectedSlot(ItemStack stack) {
        return NBTHelper.getInt(stack, "selectedSlot", 0);
    }

    @Override
    public void setSelectedSlot(ItemStack stack, int slot) {
        NBTHelper.setInt(stack, "selectedSlot", slot);
    }

    @Override
    public int getTime(ItemStack stack) {
        return this.getCADData(stack).getTime();
    }

    @Override
    public void incrementTime(ItemStack stack) {
        ICADData data = this.getCADData(stack);
        data.setTime(data.getTime() + 1);
    }

    @Override
    public int getStoredPsi(ItemStack stack) {
        int maxPsi = this.getStatValue(stack, EnumCADStat.OVERFLOW);
        return Math.min(this.getCADData(stack).getBattery(), maxPsi);
    }

    @Override
    public void regenPsi(ItemStack stack, int psi) {
        int maxPsi = this.getStatValue(stack, EnumCADStat.OVERFLOW);
        if (maxPsi != -1) {
            int currPsi = this.getStoredPsi(stack);
            int endPsi = Math.min(currPsi + psi, maxPsi);
            if (endPsi != currPsi) {
                ICADData data = this.getCADData(stack);
                data.setBattery(endPsi);
                data.markDirty(true);
            }

        }
    }

    @Override
    public int consumePsi(ItemStack stack, int psi) {
        if (psi == 0) {
            return 0;
        } else {
            int currPsi = this.getStoredPsi(stack);
            if (currPsi == -1) {
                return 0;
            } else {
                ICADData data = this.getCADData(stack);
                if (currPsi >= psi) {
                    data.setBattery(currPsi - psi);
                    data.markDirty(true);
                    return 0;
                } else {
                    data.setBattery(0);
                    data.markDirty(true);
                    return psi - currPsi;
                }
            }
        }
    }

    @Override
    public int getMemorySize(ItemStack stack) {
        int sockets = this.getStatValue(stack, EnumCADStat.SOCKETS);
        return sockets == -1 ? 255 : sockets / 3;
    }

    @Override
    public void setStoredVector(ItemStack stack, int memorySlot, Vector3 vec) throws SpellRuntimeException {
        int size = this.getMemorySize(stack);
        if (memorySlot >= 0 && memorySlot < size) {
            this.getCADData(stack).setSavedVector(memorySlot, vec);
        } else {
            throw new SpellRuntimeException("psi.spellerror.memoryoutofbounds");
        }
    }

    @Override
    public Vector3 getStoredVector(ItemStack stack, int memorySlot) throws SpellRuntimeException {
        int size = this.getMemorySize(stack);
        if (memorySlot >= 0 && memorySlot < size) {
            return this.getCADData(stack).getSavedVector(memorySlot);
        } else {
            throw new SpellRuntimeException("psi.spellerror.memoryoutofbounds");
        }
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems) {
        if (this.isInCreativeTab(tab)) {
            subItems.add(makeCAD(new ItemStack(ModItems.SHOVEL_ASSEMBLY, 1, 0), new ItemStack(vazkii.psi.common.item.base.ModItems.cadCore, 1, 0), new ItemStack(vazkii.psi.common.item.base.ModItems.cadSocket, 1, 0), new ItemStack(vazkii.psi.common.item.base.ModItems.cadBattery, 1, 0)));
            subItems.add(makeCAD(new ItemStack(ModItems.SHOVEL_ASSEMBLY, 1, 1), new ItemStack(vazkii.psi.common.item.base.ModItems.cadCore, 1, 0), new ItemStack(vazkii.psi.common.item.base.ModItems.cadSocket, 1, 0), new ItemStack(vazkii.psi.common.item.base.ModItems.cadBattery, 1, 0)));
            subItems.add(makeCAD(new ItemStack(ModItems.SHOVEL_ASSEMBLY, 1, 2), new ItemStack(vazkii.psi.common.item.base.ModItems.cadCore, 1, 1), new ItemStack(vazkii.psi.common.item.base.ModItems.cadSocket, 1, 1), new ItemStack(vazkii.psi.common.item.base.ModItems.cadBattery, 1, 1)));
            subItems.add(makeCAD(new ItemStack(ModItems.SHOVEL_ASSEMBLY, 1, 3), new ItemStack(vazkii.psi.common.item.base.ModItems.cadCore, 1, 3), new ItemStack(vazkii.psi.common.item.base.ModItems.cadSocket, 1, 3), new ItemStack(vazkii.psi.common.item.base.ModItems.cadBattery, 1, 2)));
            subItems.add(makeCAD(new ItemStack(ModItems.SHOVEL_ASSEMBLY, 1, 4), new ItemStack(vazkii.psi.common.item.base.ModItems.cadCore, 1, 3), new ItemStack(vazkii.psi.common.item.base.ModItems.cadSocket, 1, 3), new ItemStack(vazkii.psi.common.item.base.ModItems.cadBattery, 1, 2)));
            subItems.add(makeCAD(new ItemStack(ModItems.SHOVEL_ASSEMBLY, 1, 5), new ItemStack(vazkii.psi.common.item.base.ModItems.cadCore, 1, 3), new ItemStack(vazkii.psi.common.item.base.ModItems.cadSocket, 1, 3), new ItemStack(vazkii.psi.common.item.base.ModItems.cadBattery, 1, 2)));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack itemStack, World player, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
        IToolCAD.super.addInformation(itemStack, player, tooltip, advanced);
    }

    @Override
    @Nonnull
    public EnumRarity getRarity(@NotNull ItemStack stack) {
        return EnumRarity.RARE;
    }
}
