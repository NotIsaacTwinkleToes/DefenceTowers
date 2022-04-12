package me.isaac.defencetowers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class StaticUtil {

    public static DecimalFormat format = new DecimalFormat("#,###");

    public static ItemStack fastItem(Material material, String name, String[] lore, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();

        assert itemMeta != null;
        itemMeta.setDisplayName(name);

        List<String> loreList = new ArrayList<>(Arrays.asList(lore));

        itemMeta.setLore(loreList);

        if (enchanted) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
        if (enchanted) item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        return item;
    }

    public static List<String> defaultBlacklistTypes() {

        List<String> list = new ArrayList<>();

            list.add(EntityType.AREA_EFFECT_CLOUD.name());
            list.add(EntityType.ARMOR_STAND.name());
            list.add(EntityType.ARROW.name());
            list.add(EntityType.DRAGON_FIREBALL.name());
            list.add(EntityType.DROPPED_ITEM.name());
            list.add(EntityType.EGG.name());
            list.add(EntityType.ENDER_CRYSTAL.name());
            list.add(EntityType.ENDER_PEARL.name());
            list.add(EntityType.ENDER_SIGNAL.name());
            list.add(EntityType.EVOKER_FANGS.name());
            list.add(EntityType.EXPERIENCE_ORB.name());
            list.add(EntityType.FALLING_BLOCK.name());
            list.add(EntityType.FIREBALL.name());
            list.add(EntityType.SMALL_FIREBALL.name());
            list.add(EntityType.FIREWORK.name());
            list.add(EntityType.FISHING_HOOK.name());
            list.add(EntityType.GLOW_ITEM_FRAME.name());
            list.add(EntityType.ITEM_FRAME.name());
            list.add(EntityType.LEASH_HITCH.name());
            list.add(EntityType.LIGHTNING.name());
            list.add(EntityType.LLAMA_SPIT.name());
            list.add(EntityType.MARKER.name());
            list.add(EntityType.MINECART.name());
            list.add(EntityType.MINECART_CHEST.name());
            list.add(EntityType.MINECART_COMMAND.name());
            list.add(EntityType.MINECART_FURNACE.name());
            list.add(EntityType.MINECART_HOPPER.name());
            list.add(EntityType.MINECART_MOB_SPAWNER.name());
            list.add(EntityType.MINECART_TNT.name());
            list.add(EntityType.PAINTING.name());
//		blacklist.add(EntityType.PLAYER); Players can now be blacklisted
            list.add(EntityType.PRIMED_TNT.name());
            list.add(EntityType.SNOWBALL.name());
            list.add(EntityType.SHULKER_BULLET.name());
            list.add(EntityType.SPECTRAL_ARROW.name());
            list.add(EntityType.SPLASH_POTION.name());
            list.add(EntityType.THROWN_EXP_BOTTLE.name());
            list.add(EntityType.TRIDENT.name());
            list.add(EntityType.UNKNOWN.name());
            list.add(EntityType.WITHER_SKULL.name());

            return list;
    }

    public static String locationString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
                + location.getBlockZ();
    }

    public static Location locationString(String string) {

        String[] split = string.split(",");

        if (split.length < 4)
            throw new IllegalArgumentException("Invalid location string!");

        World world = Bukkit.getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);

        return new Location(world, x, y, z);

    }

    public static void checkConfig(String towerName) {

        File towerFile = new File(DefenceTowersMain.towerFolder.getPath() + "//" + towerName + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(towerFile);
        List<String> changes = new ArrayList<>();

        boolean updated = false;

        for (ConfigDefaults defaults : ConfigDefaults.values()) {

            if (yaml.get(defaults.key) != null || yaml.isConfigurationSection(defaults.key)) continue;
            updated = true;

            changes.add(defaults.key + " likely changed to " + defaults.value.toString());

            if (defaults == ConfigDefaults.DISPLAY_DISPLAY_NAME) yaml.set(defaults.key, "&d" + towerName);
            else if (defaults == ConfigDefaults.POTION_EFFECTS) yaml.set(defaults.key, new ArrayList<>());
            else yaml.set(defaults.key, defaults.value);

        }

        try {
            if (updated) {
                yaml.save(towerFile);
                Bukkit.getLogger().log(Level.INFO, towerName + " configuration file was updated, take a look!");
                Bukkit.getLogger().log(Level.INFO, changes.toString());
            }
        } catch (IOException ignored) {}

    }

    @SuppressWarnings("deprecation")
    public static ItemStack getHeadFromValue(String value) {
        UUID id = UUID.nameUUIDFromBytes(value.getBytes());
        int less = (int) id.getLeastSignificantBits();
        int most = (int) id.getMostSignificantBits();
        return Bukkit.getUnsafe().modifyItemStack(new ItemStack(Material.PLAYER_HEAD),
                "{SkullOwner:{Id:[I;" + (less * most) + "," + (less >> 23) + "," + (most / less) + "," + (most * 8731)
                        + "],Properties:{textures:[{Value:\"" + value + "\"}]}}}");
    }

    public static Tower getShooter(Projectile projectile) {

        Tower tower;

        for (Tower towers : DefenceTowersMain.instance.getTowers()) {
            if (towers.towersActiveProjectileList.contains(projectile)) return towers;
        }
        throw new IllegalArgumentException("Projectile was not shot by tower");
    }

}
