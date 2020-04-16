package transfarmer.psionicarsenal;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import transfarmer.psionicarsenal.item.ModItems;
import transfarmer.psionicarsenal.util.PsionicArsenalTab;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@Mod(
        modid = Main.MOD_ID,
        name = Main.NAME,
        version = Main.VERSION,
        dependencies = "required-after:psi@[r1.1-75,);required-after:librarianlib;",
        useMetadata = true,
        acceptedMinecraftVersions = "[1.12.2]"
)
public class Main {
    public static final String MOD_ID = "psionicarsenal";
    public static final String NAME = "psionic arsenal";
    public static final String VERSION = "1.1.1";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final CreativeTabs TAB = new PsionicArsenalTab();

    @EventBusSubscriber
    public static class RegistryEventHandler {
        @SubscribeEvent
        public static void onRegisterItem(final Register<Item> event) {
            for (final Item item : ModItems.get()) {
                event.getRegistry().register(item.setCreativeTab(TAB));
            }
        }

        @SideOnly(CLIENT)
        @SubscribeEvent
        public static void onModelRegistryEvent(final ModelRegistryEvent event) {
            for (final Item item : ModItems.get()) {
                final String path = item.getRegistryName().getPath();
                final String prefix;

                if (path.contains("sword")) {
                    prefix = "sword/";
                } else if (path.contains("bow")) {
                    prefix = "bow/";
                } else if (path.contains("pickaxe")) {
                    prefix = "pickaxe/";
                } else if (path.contains("axe")) {
                    prefix = "axe/";
                } else if (path.contains("shovel")) {
                    prefix = "shovel/";
                } else if (path.contains("hoe")) {
                    prefix = "hoe/";
                } else {
                    prefix = "";
                }

                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(new ResourceLocation(Main.MOD_ID, prefix + path), "inventory"));
            }
        }
    }
}
