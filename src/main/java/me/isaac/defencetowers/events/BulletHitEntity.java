package me.isaac.defencetowers.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.events.custom.BulletHitEntityEvent;

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

}
