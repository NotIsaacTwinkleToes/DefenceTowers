package me.isaac.defencetowers.tower;

import me.isaac.defencetowers.StaticUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TowerItems {

    private final ItemStack toggleRadius = StaticUtil.fastItem(Material.ENDER_EYE, ChatColor.WHITE + "Tower Radius", new String[] {ChatColor.GRAY + "Displays the towers radius"}, false);
    private final ItemStack blacklist = StaticUtil.fastItem(Material.PAPER, ChatColor.WHITE + "Player Blacklist", new String[] {ChatColor.GRAY + "Players not in the blacklist cannot interact with this tower!", ChatColor.DARK_GRAY + "Players the tower will not shoot at:"}, true);
    private final ItemStack ammunition = StaticUtil.fastItem(Material.ARROW, ChatColor.WHITE + "Ammunition" + ChatColor.GOLD + "0", new String[] {ChatColor.GRAY + "Right Click to switch targeting mode", ChatColor.DARK_GRAY + "Target Mode: " + ChatColor.WHITE + "TargetType", ChatColor.GRAY + "Shift + Click arrows in your inventory to put them in the turret"}, false);
    private final ItemStack ride = StaticUtil.fastItem(Material.SADDLE, ChatColor.WHITE + "Control Tower", new String[] {}, false);

    public ItemStack getToggleRadius() {
        return toggleRadius;
    }
    public ItemStack getBlacklist() {
        return blacklist;
    }
    public ItemStack getAmmunition() {
        return ammunition;
    }

    public ItemStack getRide() {
        return ride;
    }

}
