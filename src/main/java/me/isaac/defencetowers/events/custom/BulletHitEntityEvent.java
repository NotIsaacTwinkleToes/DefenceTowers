package me.isaac.defencetowers.events.custom;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.Tower;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
    final Arrow arrow;
    final Entity entity;

    public BulletHitEntityEvent(DefenceTowersMain main, EntityDamageByEntityEvent damageEvent) {

        arrow = (Arrow) damageEvent.getDamager();
        entity = damageEvent.getEntity();

        damageEvent.setDamage(damageEvent.getDamager().getPersistentDataContainer().get(main.getKeys().bulletDamage, PersistentDataType.DOUBLE));

        if (damageEvent.getDamage() == 0) damageEvent.setCancelled(true);

        tower = new Tower(main, damageEvent.getDamager().getPersistentDataContainer().get(main.getKeys().bullet, PersistentDataType.STRING), null, false);

        try {
            ((LivingEntity) damageEvent.getEntity()).setNoDamageTicks(0);
            tower.getPotionEffects().forEach(effect -> ((LivingEntity) damageEvent.getEntity()).addPotionEffect(effect));
        } catch (ClassCastException ex) {}

    }

    public Tower getShooter() {
        return tower;
    }

    public Arrow getArrow() {
        return arrow;
    }

    public Entity getEntity() {
        return entity;
    }

}
