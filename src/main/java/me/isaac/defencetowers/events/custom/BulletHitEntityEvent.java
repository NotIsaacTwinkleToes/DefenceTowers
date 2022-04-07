package me.isaac.defencetowers.events.custom;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.Tower;

public class BulletHitEntityEvent extends Event {

    HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public BulletHitEntityEvent(DefenceTowersMain main, EntityDamageByEntityEvent damageEvent) {

        damageEvent.setDamage(damageEvent.getDamager().getPersistentDataContainer().get(main.getKeys().bulletDamage, PersistentDataType.DOUBLE));

        if (damageEvent.getDamage() == 0) damageEvent.setCancelled(true);

        Tower tower = new Tower(main, damageEvent.getDamager().getPersistentDataContainer().get(main.getKeys().bullet, PersistentDataType.STRING), null, false);

        try {
            ((LivingEntity) damageEvent.getEntity()).setNoDamageTicks(0);
            tower.getPotionEffects().forEach(effect -> ((LivingEntity) damageEvent.getEntity()).addPotionEffect(effect));
        } catch (ClassCastException ex) {}

    }

}
