package me.isaac.defencetowers;

import org.bukkit.NamespacedKey;

public class NamespacedKeys {

    public final NamespacedKey turretItem, turretStand, bullet,
            bulletDamage, bounces, fire, tail, critical, pierce,
            knockback, splits;

    public NamespacedKeys(DefenceTowersMain main) {
        // Turret in world data
        turretItem = new NamespacedKey(main, "turret_item");
        turretStand = new NamespacedKey(main, "turret_stand");
        bullet = new NamespacedKey(main, "turret_bullet");

        // Bullet data
        bulletDamage = new NamespacedKey(main, "damage");
        bounces = new NamespacedKey(main, "bounces");
        splits = new NamespacedKey(main, "splits");
        fire = new NamespacedKey(main, "fire_ticks");
        tail = new NamespacedKey(main, "tail_particles");
        critical = new NamespacedKey(main, "critical");
        pierce = new NamespacedKey(main, "pierce");
        knockback = new NamespacedKey(main, "knockback");

    }

}
