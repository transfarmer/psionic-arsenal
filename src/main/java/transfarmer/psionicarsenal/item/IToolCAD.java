package transfarmer.psionicarsenal.item;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import transfarmer.psionicarsenal.Main;
import vazkii.arl.interf.IItemColorProvider;
import vazkii.arl.interf.IVariantHolder;
import vazkii.arl.network.NetworkHandler;
import vazkii.arl.util.TooltipHandler;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.cad.ICADAssembly;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.internal.TooltipHelper;
import vazkii.psi.api.spell.ISpellSettable;
import vazkii.psi.common.network.message.MessageVisualEffect;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

public interface IToolCAD extends ICAD, ISpellSettable, IItemColorProvider, IVariantHolder {
    Pattern FAKE_PLAYER_PATTERN = Pattern.compile("^(?:\\[.*])|(?:ComputerCraft)$");

    default boolean craft(EntityPlayer player, ItemStack in, ItemStack out) {
        return this.craft(player, CraftingHelper.getIngredient(in), out);
    }

    default boolean craft(EntityPlayer player, String in, ItemStack out) {
        return this.craft(player, CraftingHelper.getIngredient(in), out);
    }

    default boolean craft(EntityPlayer player, Ingredient in, ItemStack out) {
        if (player.world.isRemote) {
            return false;
        }

        final List<EntityItem> items = player.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, player.getEntityBoundingBox().grow(8.0D), (entity) -> entity != null && entity.getDistanceSq(player) <= 64.0D);
        boolean did = false;

        for (final EntityItem item : items) {
            final ItemStack stack = item.getItem();

            if (in.test(stack)) {
                final ItemStack copy = out.copy();

                copy.setCount(stack.getCount());
                item.setItem(copy);
                did = true;
                NetworkHandler.INSTANCE.sendToAllAround(
                        new MessageVisualEffect(1295871, item.posX, item.posY, item.posZ, item.width, item.height, item.getYOffset(), 0),
                        new TargetPoint(item.world.provider.getDimension(), item.posX, item.posY, item.posZ, 32.0D)
                );
            }
        }

        return did;
    }

    static boolean isTruePlayer(final Entity entity) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer) entity;

            return !(player instanceof FakePlayer) && !FAKE_PLAYER_PATTERN.matcher(player.getName()).matches();
        }

        return false;
    }

    @SideOnly(CLIENT)
    default void addInformation(final @NotNull ItemStack stack, final World world, final @NotNull List<String> tooltip, final @NotNull ITooltipFlag advanced) {
        TooltipHelper.tooltipIfShift(tooltip, () -> {
            final String componentName = TooltipHelper.local(ISocketable.getSocketedItemName(stack, "psimisc.none"));
            final EnumCADComponent[] components = EnumCADComponent.class.getEnumConstants();

            TooltipHelper.addToTooltip(tooltip, "psimisc.spellSelected", componentName);

            for (EnumCADComponent component : components) {
                ItemStack componentStack = this.getComponentInSlot(stack, component);
                String name = "psimisc.none";
                if (!componentStack.isEmpty()) {
                    name = componentStack.getDisplayName();
                }

                name = TooltipHelper.local(name);
                String line = TextFormatting.GREEN + TooltipHandler.local(component.getName()) + TextFormatting.GRAY + ": " + name;
                TooltipHelper.addToTooltip(tooltip, line);
                EnumCADStat[] stats = EnumCADStat.class.getEnumConstants();

                for (EnumCADStat stat : stats) {
                    if (stat.getSourceType() == component) {
                        final String statName = stat.getName();
                        final int statValue = this.getStatValue(stack, stat);
                        final String statValStr = statValue == -1 ? "∞" : "" + statValue;

                        line = " " + TextFormatting.AQUA + TooltipHelper.local(statName) + TextFormatting.GRAY + ": " + statValStr;

                        if (!line.isEmpty()) {
                            TooltipHelper.addToTooltip(tooltip, line);
                        }
                    }
                }
            }

        });
    }

    @Override
    default boolean requiresSneakForSpellSet(final ItemStack stack) {
        return true;
    }

    @Override
    default IItemColor getItemColor() {
        return (stack, tintIndex) -> tintIndex == 1 ? this.getSpellColor(stack) : 16777215;
    }

    @Override
    default String getModNamespace() {
        return Main.MOD_ID;
    }

    @Override
    @Nullable
    default ItemMeshDefinition getCustomMeshDefinition() {
        return (stack) -> {
            final ItemStack assemblyStack = ((ICAD) stack.getItem()).getComponentInSlot(stack, EnumCADComponent.ASSEMBLY);

            if (assemblyStack.isEmpty()) {
                return new ModelResourceLocation("missingno");
            }

            return ((ICADAssembly) assemblyStack.getItem()).getCADModel(assemblyStack, stack);
        };
    }
}
