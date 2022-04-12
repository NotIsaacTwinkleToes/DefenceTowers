package me.isaac.defencetowers.events.custom;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.Tower;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class BulletHitEntityEvent extends Event {

    HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    final Tower tower;
    final Projectile projectile;
    final Entity entity;
    final int fireTicks, pierce, knockback;
    final double critical, projectileDamage;
    final String towerName;

    public BulletHitEntityEvent(DefenceTowersMain main, EntityDamageByEntityEvent damageEvent) {

        projectile = (Projectile) damageEvent.getDamager();
        entity = damageEvent.getEntity();
        fireTicks = projectile.getPersistentDataContainer().get(main.getKeys().fire, PersistentDataType.INTEGER);

        if (projectile instanceof Arrow) {
            pierce = ((Arrow) projectile).getPierceLevel();
            knockback = ((Arrow) projectile).getKnockbackStrength();
        } else {
            pierce = projectile.getPersistentDataContainer().get(main.getKeys().pierce, PersistentDataType.INTEGER);
            knockback = projectile.getPersistentDataContainer().get(main.getKeys().knockback, PersistentDataType.INTEGER);
        }

        critical = projectile.getPersistentDataContainer().get(main.getKeys().critical, PersistentDataType.DOUBLE);
        projectileDamage = projectile.getPersistentDataContainer().get(main.getKeys().bulletDamage, PersistentDataType.DOUBLE);
        towerName = projectile.getPersistentDataContainer().get(main.getKeys().bullet, PersistentDataType.STRING);

        damageEvent.setDamage(projectileDamage);

        if (fireTicks > 0)
            damageEvent.getEntity().setFireTicks(fireTicks);

        tower = new Tower(main, towerName, null, false);

        try {
            ((LivingEntity) damageEvent.getEntity()).setNoDamageTicks(0);
            tower.getPotionEffects().forEach(effect -> ((LivingEntity) damageEvent.getEntity()).addPotionEffect(effect));
        } catch (ClassCastException ex) {}

        if (damageEvent.getDamage() == 0) damageEvent.setCancelled(true);
        damageEvent.setDamage(damageEvent.getDamage() * critical);

    }

    public Tower getShooter() {
        return tower;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public Entity getEntity() {
        return entity;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public double getCritical() {
        return critical;
    }

    public double getProjectileDamage() {
        return projectileDamage;
    }

}
