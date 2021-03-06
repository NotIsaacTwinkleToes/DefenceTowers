package me.isaac.defencetowers.events.custom;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.tower.Tower;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class ProjectileHitEntityEvent extends Event {

    final Tower tower;
    final Projectile projectile;
    final Entity entity;
    final int fireTicks, pierce, knockback;
    final double critical, projectileDamage;
    final String towerName;
    HandlerList handlers = new HandlerList();
    public ProjectileHitEntityEvent(DefenceTowersMain main, EntityDamageByEntityEvent damageEvent) {

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
            tower.getTowerOptions().getPotionEffects().forEach(effect -> ((LivingEntity) damageEvent.getEntity()).addPotionEffect(effect));
        } catch (ClassCastException ignored) {
        }

        if (damageEvent.getDamage() == 0) damageEvent.setCancelled(true);
        damageEvent.setDamage(damageEvent.getDamage() * critical);

    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
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
