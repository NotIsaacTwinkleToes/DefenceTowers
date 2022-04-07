package me.isaac.defencetowers.events;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.events.custom.BulletHitEntityEvent;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class BulletHitEntity implements Listener {

    DefenceTowersMain main;

    public BulletHitEntity(DefenceTowersMain main) {
        this.main = main;
    }

    @EventHandler
    public void onBulletHitEntity(EntityDamageByEntityEvent e) {

        if (!e.getDamager().getPersistentDataContainer().has(main.getKeys().bulletDamage, PersistentDataType.DOUBLE)) return;

        main.getServer().getPluginManager().callEvent(new BulletHitEntityEvent(main, e));

    }

    @EventHandler
    public void onBulletBounce(ProjectileHitEvent e) {
        if (!e.getEntity().getPersistentDataContainer().has(main.getKeys().bounces, PersistentDataType.INTEGER)) return;
        if (e.getHitBlockFace() == null) return;
        if (e.getEntity().getPersistentDataContainer().get(main.getKeys().bounces, PersistentDataType.INTEGER) <= 0) return;

        Vector velocity = new Vector(e.getEntity().getVelocity().getX(), e.getEntity().getVelocity().getY(), e.getEntity().getVelocity().getZ());

        Arrow arrow = (Arrow) e.getEntity().getLocation().getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.ARROW),
            entityArrow = (Arrow) e.getEntity();

        arrow.setBounce(false);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setPierceLevel(entityArrow.getPierceLevel());
        arrow.setKnockbackStrength(entityArrow.getKnockbackStrength());
        arrow.setVisualFire(entityArrow.isVisualFire());
        arrow.setSilent(entityArrow.isSilent());
        arrow.setGravity(entityArrow.hasGravity());
        arrow.setFireTicks(entityArrow.getFireTicks());
        arrow.setColor(entityArrow.getColor());
        arrow.setCritical(entityArrow.isCritical());

        arrow.getPersistentDataContainer().set(main.getKeys().bounces, PersistentDataType.INTEGER, entityArrow.getPersistentDataContainer().get(main.getKeys().bounces, PersistentDataType.INTEGER) - 1);
        arrow.getPersistentDataContainer().set(main.getKeys().bulletDamage, PersistentDataType.DOUBLE, entityArrow.getPersistentDataContainer().get(main.getKeys().bulletDamage, PersistentDataType.DOUBLE));
        arrow.getPersistentDataContainer().set(main.getKeys().bullet, PersistentDataType.STRING, entityArrow.getPersistentDataContainer().get(main.getKeys().bullet, PersistentDataType.STRING));

        e.getEntity().remove();

        switch(e.getHitBlockFace()) {
            case UP:
            case DOWN:
                velocity.setY(-velocity.getY() * .75);
                break;
            case EAST:
            case WEST:
                velocity.setX(-velocity.getX());
                break;
            case NORTH:
            case SOUTH:
                velocity.setZ(-velocity.getZ());
                break;
            default:
                velocity.setX(0).setY(0).setZ(0);
                break;
        }

        arrow.setVelocity(velocity);

    }

}
