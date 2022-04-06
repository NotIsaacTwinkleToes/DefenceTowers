package me.isaac.defencetowers.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.isaac.defencetowers.DefenceTowersMain;

public class PlayerLeave implements Listener {

    DefenceTowersMain main;

    public PlayerLeave(DefenceTowersMain main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {

        if (!main.getInteractTowerInstance().editingTower.containsKey(e.getPlayer())) return;
        main.getInteractTowerInstance().editingTower.remove(e.getPlayer());

    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerChatLeave(AsyncPlayerChatEvent e) {

        if (!main.getInteractTowerInstance().editingTower.containsKey(e.getPlayer())) return;
        e.setCancelled(true);
        if (!e.getMessage().equalsIgnoreCase("cancel")) {

            OfflinePlayer player;
            String[] split = e.getMessage().split("\\s+");

            int whatArgument = 0;

            if (split[0].equalsIgnoreCase("remove")) whatArgument = 1;

            player = Bukkit.getOfflinePlayer(split[whatArgument]);

            if (player == null) {
                e.getPlayer().sendMessage(DefenceTowersMain.prefix + "Issue finding player!");
                return;
            }

            if (whatArgument == 1) {

                if (!main.getInteractTowerInstance().editingTower.get(e.getPlayer()).getBlacklistedPlayers().contains(player.getUniqueId())) {
                    e.getPlayer().sendMessage(DefenceTowersMain.prefix + "Player is not part of the towers blacklist!");
                    return;
                }

                main.getInteractTowerInstance().editingTower.get(e.getPlayer()).getBlacklistedPlayers().remove(player.getUniqueId());
                e.getPlayer().sendMessage(DefenceTowersMain.prefix + player.getName() + " removed from tower blacklist!");

            } else {
                main.getInteractTowerInstance().editingTower.get(e.getPlayer()).getBlacklistedPlayers().add(player.getUniqueId());
                e.getPlayer().sendMessage(DefenceTowersMain.prefix + player.getName() + " added to tower blacklist!");
            }



            return;
        }

        main.getInteractTowerInstance().editingTower.remove(e.getPlayer());
        e.getPlayer().sendMessage(DefenceTowersMain.prefix + "No longer setting blacklist");

    }

}
