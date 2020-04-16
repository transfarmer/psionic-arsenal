package transfarmer.psionicarsenal.util;

import com.teamwizardry.librarianlib.features.helpers.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import transfarmer.psionicarsenal.entity.EntityPsiArrow;
import transfarmer.psionicarsenal.entity.capability.ArrowSpellImmuneCapability;
import vazkii.psi.common.entity.EntitySpellProjectile;

@EventBusSubscriber(modid = "psionicarsenal")
public class EventHandler {
    @SubscribeEvent
    public static void attachArrowSpellImmunity(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityArrow) {
            event.addCapability(new ResourceLocation("psionicarsenal", "psionicarsenalspellimmunearrow"), new ArrowSpellImmuneCapability(event.getObject()));
        }

    }

    @SubscribeEvent
    public static void arrowHit(final ProjectileImpactEvent event) {
        final Entity projectile = event.getEntity();

        if (projectile instanceof EntityArrow && !(projectile instanceof EntityPsiArrow) && NBTHelper.hasKey(projectile.getEntityData(), "rpsideas-spellimmune")) {
            for (final Entity rider : projectile.getPassengers()) {
                if (rider instanceof EntitySpellProjectile) {
                    rider.dismountRidingEntity();
                    rider.setPosition(projectile.posX, projectile.posY, projectile.posZ);
                    rider.motionX = projectile.motionX;
                    rider.motionY = projectile.motionY;
                    rider.motionZ = projectile.motionZ;
                    rider.velocityChanged = true;
                }
            }
        }
    }
}
