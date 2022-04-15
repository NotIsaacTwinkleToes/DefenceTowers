package me.isaac.defencetowers.events;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.tower.Tower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

public class TowerTakeDamage implements Listener {

    DefenceTowersMain main;

    public TowerTakeDamage(DefenceTowersMain main) {
        this.main = main;
    }

    @EventHandler
    public void onTowerTakeDamage(EntityDamageEvent e) {
        if (!e.getEntity().getPersistentDataContainer().has(main.getKeys().turretStand, PersistentDataType.STRING))
            return;
        e.setCancelled(true);

        Tower tower = main.getTower(e.getEntity());

        if (!tower.getTowerOptions().isUsingHealth()) return;

        double armorPoints = tower.getTowerOptions().getTowerArmor(), toughness = tower.getTowerOptions().getTowerToughness();

        double newDamage = e.getDamage() * (1 - ((Math.min(20, Math.max(armorPoints / 5, armorPoints - ((e.getDamage() * 4) / (toughness + 8))))) / 25));

        tower.damage(newDamage);

    }

}
