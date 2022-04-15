package me.isaac.defencetowers.tower;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.MessageDefault;
import me.isaac.defencetowers.StaticUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TowerItems {

    //TODO Make tower repair items.

    private final ItemStack toggleRadius;
    private final ItemStack blacklist;
    private final ItemStack ammunition;
    private final ItemStack ride;
    DefenceTowersMain main;
    public TowerItems(DefenceTowersMain main) {
        this.main = main;

        toggleRadius = StaticUtil.fastItem(Material.ENDER_EYE, main.messagesYaml.getString(MessageDefault.TOWER_RADIUS_NAME.path), new String[]{main.messagesYaml.getString(MessageDefault.TOWER_RADIUS_DESCRIPTION.path)}, false);
        blacklist = StaticUtil.fastItem(Material.PAPER, main.messagesYaml.getString(MessageDefault.TOWER_BLACKLIST_NAME.path), main.messagesYaml.getStringList(MessageDefault.TOWER_BLACKLIST_DESCRIPTION.path), true);
        ammunition = StaticUtil.fastItem(Material.ARROW, main.messagesYaml.getString(MessageDefault.TOWER_AMMUNITION_NAME.path), main.messagesYaml.getStringList(MessageDefault.TOWER_AMMUNITION_DESCRIPTION.path), false);
        ride = StaticUtil.fastItem(Material.SADDLE, main.messagesYaml.getString(MessageDefault.TOWER_RIDE_NAME.path), main.messagesYaml.getStringList(MessageDefault.TOWER_RIDE_DESCRIPTION.path), false);

    }

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
