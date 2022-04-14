package me.isaac.defencetowers.events;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.tower.Tower;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlaceTower implements Listener {

    DefenceTowersMain main;

    private final double x = .5, y = -1.4, z = .5;

    public PlaceTower(DefenceTowersMain main) {
        this.main = main;
    }

    @EventHandler
    public void onPlaceTurret(BlockPlaceEvent e) {

        if (!e.getItemInHand().getItemMeta().getPersistentDataContainer().has(main.getKeys().turretItem, PersistentDataType.STRING)) return;
        e.setCancelled(true);

        for (Tower tower : main.getTowers()) {
            if (tower.getLocation().equals(e.getBlockPlaced().getLocation().add(x, y, z))) return;
        }

        String turretName = e.getItemInHand().getItemMeta().getPersistentDataContainer().get(main.getKeys().turretItem, PersistentDataType.STRING);

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) e.getItemInHand().setAmount(e.getItemInHand().getAmount() - 1);

        Tower tower = new Tower(main, turretName, e.getBlockPlaced().getLocation().add(x, y, z), false);

        tower.blacklistPlayer(e.getPlayer());

    }

}
