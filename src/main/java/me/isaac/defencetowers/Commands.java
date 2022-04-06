package me.isaac.defencetowers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands implements TabExecutor {

    DefenceTowersMain main;

    public Commands(DefenceTowersMain main) {
        this.main = main;
    }

    //Add command to edit turret from GUI

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            list.add("get");
            list.add("list");
            list.add("turret");
            list.add("reload");
            list.add("base");
            list.add("?");
        }

        if (args.length == 2) {

            switch (args[0].toLowerCase()) {
                case "get":
                case "reload":
                case "turret":
                case "base":
                    for (File files : DefenceTowersMain.towerFolder.listFiles()) {
                        list.add(files.getName().replace(".yml", ""));
                    }
                    break;
                default:
                    return list;
            }

        }

        return list;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("?")) {

            player.sendMessage("/dt get <tower> - Get tower item");
            player.sendMessage("/dt list - List all towers");
            player.sendMessage("/dt turret <tower> - Set tower's turret item");
            player.sendMessage("/dt base <tower> - Set tower's base item");
            player.sendMessage("/dt reload <tower> - Reload towers file and existing placed towers");
            player.sendMessage("/dt ? - Shows this help info");

            return true;
        }

        switch (args[0].toLowerCase()) {
            case "get":

                String towerName = "";

                for (int i = 1; i < args.length; i++) {
                    towerName = towerName + (towerName.equals("") ? "" : " ") + args[i];
                }

                if (!Tower.exists(towerName)) {
                    player.sendMessage(DefenceTowersMain.prefix + "Unknown tower name!");
                    return true;
                }

                player.getInventory().addItem(new Tower(main, towerName, null).getTurret());

                break;
            case "list":

                for (File towerFile : DefenceTowersMain.towerFolder.listFiles()) {
                    player.sendMessage(towerFile.getName().replace(".yml", ""));
                }
                break;
            case "reload":

                if (args.length == 1) {

                    player.sendMessage(DefenceTowersMain.prefix + "Updating all towers..");

                    for (File towerFiles : DefenceTowersMain.towerFolder.listFiles()) {
                        main.updateExistingTowers(towerFiles.getName().replace(".yml", ""));
                    }
                    return true;
                }

                towerName = "";

                for (int i = 1; i < args.length; i++) {
                    towerName = towerName + (towerName.equals("") ? "" : " ") + args[i];
                }

                if (!Tower.exists(towerName)) {
                    player.sendMessage(DefenceTowersMain.prefix + "Unknown tower name!");
                    return true;
                }

                main.updateExistingTowers(towerName);

                player.sendMessage(DefenceTowersMain.prefix + towerName + " has been reloaded!");

                break;
            case "turret":

                towerName = "";

                for (int i = 1; i < args.length; i++) {
                    towerName = towerName + (towerName.equals("") ? "" : " ") + args[i];
                }

                if (!Tower.exists(towerName)) {
                    player.sendMessage(DefenceTowersMain.prefix + "Unknown tower name!");
                    return true;
                }

                ItemStack item = player.getInventory().getItemInMainHand();

                if (item.getType() == Material.AIR) item = player.getInventory().getItemInOffHand();
                if (item.getType() == Material.AIR) {
                    player.sendMessage(DefenceTowersMain.prefix + "Hold the item you want the turret head to be!");
                    return true;
                }

                Tower tower = new Tower(main, towerName, null);

                tower.setTurret(item);

                player.sendMessage(DefenceTowersMain.prefix + "Tower turret item set!");

                break;

            case "base":
                towerName = "";

                for (int i = 1; i < args.length; i++) {
                    towerName = towerName + (towerName.equals("") ? "" : " ") + args[i];
                }

                if (!Tower.exists(towerName)) {
                    player.sendMessage(DefenceTowersMain.prefix + "Unknown tower name!");
                    return true;
                }

                item = player.getInventory().getItemInMainHand();

                if (item.getType() == Material.AIR) item = player.getInventory().getItemInOffHand();
                if (item.getType() == Material.AIR) {
                    player.sendMessage(DefenceTowersMain.prefix + "Hold the item you want the turret head to be!");
                    return true;
                }

                tower = new Tower(main, towerName, null);

                tower.setBase(item);

                player.sendMessage(DefenceTowersMain.prefix + "Tower turret item set!");

                break;
            default:
                player.sendMessage(DefenceTowersMain.prefix + "Unknown sub-command: " + args[0]);
                return true;
        }

        return true;
    }

}