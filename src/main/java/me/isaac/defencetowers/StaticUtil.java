package me.isaac.defencetowers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StaticUtil {

    public static DecimalFormat format = new DecimalFormat("#,###");

    public static ItemStack fastItem(Material material, String name, String[] lore, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemm = item.getItemMeta();

        itemm.setDisplayName(name);

        List<String> loreList = new ArrayList<>();

        for (String str : lore) {
            loreList.add(str);
        }

        itemm.setLore(loreList);

        if (enchanted) itemm.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemm);
        if (enchanted) item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        return item;
    }

}
