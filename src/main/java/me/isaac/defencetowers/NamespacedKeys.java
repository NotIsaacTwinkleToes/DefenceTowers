package me.isaac.defencetowers;

import org.bukkit.NamespacedKey;

public class NamespacedKeys {

    public final NamespacedKey turretItem, turretStand, bullet, bulletDamage, bounces;

    public NamespacedKeys(DefenceTowersMain main) {

        turretItem = new NamespacedKey(main, "turret_item");
        turretStand = new NamespacedKey(main, "turret_stand");

        bullet = new NamespacedKey(main, "turret_bullet");
        bulletDamage = new NamespacedKey(main, "damage");
        bounces = new NamespacedKey(main, "bounces");

    }

}
