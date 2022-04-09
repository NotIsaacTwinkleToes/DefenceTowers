package me.isaac.defencetowers.events;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.StaticUtil;
import me.isaac.defencetowers.Tower;
import me.isaac.defencetowers.events.custom.BulletHitEntityEvent;
import org.bukkit.entity.Projectile;
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
        Projectile entity = e.getEntity();
        Tower tower;

        try {
            tower = StaticUtil.getShooter(entity);
        } catch (IllegalArgumentException ex) {
            return;
        }

        if (!entity.getPersistentDataContainer().has(main.getKeys().bounces, PersistentDataType.INTEGER)) return;
        if (e.getHitBlockFace() == null) return;

        int bounces = entity.getPersistentDataContainer().get(main.getKeys().bounces, PersistentDataType.INTEGER);

        if (bounces <= 0) return;

        Vector velocity = new Vector(e.getEntity().getVelocity().getX(), e.getEntity().getVelocity().getY(), e.getEntity().getVelocity().getZ());

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
//                velocity.setX(0).setY(0).setZ(0);
//                break;
                return;
        }

        Projectile projectile = null;

        switch (tower.getProjectileType()) {
            case ARROW:
                projectile = tower.shootArrow(entity.getLocation(), velocity);
                break;
            case TRIDENT:
                projectile = tower.shootTrident(entity.getLocation(), velocity);
                break;
            case ITEM:
                projectile = tower.shootItem(entity.getLocation(), velocity);
                break;
            case SMALL_FIREBALL:
                projectile = tower.shootFireball(entity.getLocation(), velocity, true);
                break;
            case LARGE_FIREBALL:
                projectile = tower.shootFireball(entity.getLocation(), velocity, false);
                break;
            case WITHER_SKULL:
                projectile = tower.shootWitherSkull(entity.getLocation(), velocity);
                break;
        }

        projectile.setVisualFire(entity.isVisualFire());
        projectile.setShooter(entity.getShooter());
        projectile.setGravity(entity.hasGravity());
        projectile.setSilent(entity.isSilent());

        bounces--;
        projectile.getPersistentDataContainer().set(main.getKeys().bounces, PersistentDataType.INTEGER, bounces);
        projectile.getPersistentDataContainer().set(main.getKeys().bullet, PersistentDataType.STRING, e.getEntity().getPersistentDataContainer().get(main.getKeys().bullet, PersistentDataType.STRING));

        e.getEntity().remove();

        tower.addActiveProjectile(projectile);

//        arrow.setVelocity(velocity);

    }

}
