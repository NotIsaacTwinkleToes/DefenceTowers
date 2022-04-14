package me.isaac.defencetowers.events;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.StaticUtil;
import me.isaac.defencetowers.events.custom.ProjectileHitEntityEvent;
import me.isaac.defencetowers.tower.Tower;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class ProjectileHitEvents implements Listener {

    DefenceTowersMain main;

    public ProjectileHitEvents(DefenceTowersMain main) {
        this.main = main;
    }

    @EventHandler
    public void onProjectileHitEntity(EntityDamageByEntityEvent e) {

        if (!e.getDamager().getPersistentDataContainer().has(main.getKeys().bulletDamage, PersistentDataType.DOUBLE)) return;

        main.getServer().getPluginManager().callEvent(new ProjectileHitEntityEvent(main, e));

    }

    @EventHandler
    public void onProjectileHitBlock(ProjectileHitEvent e) {
        BlockFace hitFace = e.getHitBlockFace() == null ? BlockFace.UP : e.getHitBlockFace();
        Projectile sourceProjectile = e.getEntity();
        Tower tower;

        try {
            tower = StaticUtil.getShooter(sourceProjectile);
        } catch (IllegalArgumentException ex) {
            return;
        }

        int splits = sourceProjectile.getPersistentDataContainer().get(main.getKeys().splits, PersistentDataType.INTEGER),
                bounces = sourceProjectile.getPersistentDataContainer().get(main.getKeys().bounces, PersistentDataType.INTEGER);

        switch ((tower.getTowerOptions().getRandomHitType())) {
            case BREAK:
                sourceProjectile.remove();
                break;
            case SPLIT:

                if (!sourceProjectile.getPersistentDataContainer().has(main.getKeys().splits, PersistentDataType.INTEGER)) return;
                if (splits <= 0) return;

                Projectile projectile = StaticUtil.bounceProjectile(sourceProjectile, tower, hitFace); // Original projectile
                projectile.getPersistentDataContainer().set(main.getKeys().splits, PersistentDataType.INTEGER, splits - 1);
                tower.addActiveProjectile(projectile);
                for (int split = 0; split < tower.getTowerOptions().getSplitAmount(); split++) {
                    Projectile splitProjectile = StaticUtil.bounceProjectile(sourceProjectile, tower, hitFace);
                    splitProjectile.setVelocity(offsetVector(projectile.getVelocity(), .7));
                    splitProjectile.getPersistentDataContainer().set(main.getKeys().splits, PersistentDataType.INTEGER, splits - 1);
                    splitProjectile.getPersistentDataContainer().set(main.getKeys().bounces, PersistentDataType.INTEGER, bounces);
                    tower.addActiveProjectile(splitProjectile);
                }

                break;
            case BOUNCE:

                if (!sourceProjectile.getPersistentDataContainer().has(main.getKeys().bounces, PersistentDataType.INTEGER)) return;
                if (bounces <= 0) return;
                projectile = StaticUtil.bounceProjectile(sourceProjectile, tower, hitFace);
                projectile.getPersistentDataContainer().set(main.getKeys().bounces, PersistentDataType.INTEGER, bounces - 1);
                projectile.getPersistentDataContainer().set(main.getKeys().splits, PersistentDataType.INTEGER, splits);
                tower.addActiveProjectile(projectile);

                break;
        }

    }

    private Vector offsetVector(Vector vector, double offset) {
        return new Vector(vector.getX() + randomOffset(offset), vector.getY() + randomOffset(offset), vector.getZ() + randomOffset(offset));
    }

    private double randomOffset(double offset) {
        return ThreadLocalRandom.current().nextDouble(-offset, offset);
    }

    @EventHandler
    public void onShootTower(ProjectileHitEvent e) {
        if (e.getHitEntity() == null) return;
        if (!e.getHitEntity().getPersistentDataContainer().has(main.getKeys().turretStand, PersistentDataType.STRING)) return;
        Projectile entity = e.getEntity();
        if (!entity.getPersistentDataContainer().has(main.getKeys().bullet, PersistentDataType.STRING)) return;

        Tower tower;

        try {
            tower = StaticUtil.getShooter(entity);
        } catch (IllegalArgumentException ex) {
            return;
        }

        tower.freeProjectile(tower.getTowerOptions().getProjectileType(), e.getHitEntity().getLocation().add(0, e.getHitEntity().getHeight(), 0), entity.getVelocity());
        entity.remove();
    }

}
