package me.isaac.defencetowers.events;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.Tower;

public class PlaceTurret implements Listener {

    DefenceTowersMain main;

    public PlaceTurret(DefenceTowersMain main) {
        this.main = main;
    }

    @EventHandler
    public void onPlaceTurret(BlockPlaceEvent e) {

        if (!e.getItemInHand().getItemMeta().getPersistentDataContainer().has(main.getKeys().turretItem, PersistentDataType.STRING)) return;

        e.setCancelled(true);
        String turretName = e.getItemInHand().getItemMeta().getPersistentDataContainer().get(main.getKeys().turretItem, PersistentDataType.STRING);

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) e.getItemInHand().setAmount(e.getItemInHand().getAmount() - 1);

        Tower tower = new Tower(main, turretName, e.getBlockPlaced().getLocation().add(.5, -1.4, .5), false);

        tower.blacklistPlayer(e.getPlayer());

    }

}
