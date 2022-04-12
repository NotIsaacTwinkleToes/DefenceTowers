package me.isaac.defencetowers;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public enum ConfigDefaults {
    DISPLAY_SHOW("Display.Show",true),
    DISPLAY_DISPLAY_NAME("Display.Display Name", "%NAME%"),

    PROJECTILE_PER_SHOT("Projectile.Per Shot", 1),
    PROJECTILE_GAP("Projectile.Gap", 0),
    PROJECTILE_GRAVITY("Projectile.Gravity", true),
    PROJECTILE_DAMAGE("Projectile.Damage", 2),
    PROJECTILE_SPEED("Projectile.Speed", 1d),
    PROJECTILE_ACCURACY("Projectile.Accuracy", 10f),
    PROJECTILE_VISUAL_FIRE("Projectile.Visual Fire", false),
    PROJECTILE_FIRE("Projectile.Fire Ticks", 0),
    PROJECTILE_BOUNCES("Projectile.Bounces", 0),
    PROJECTILE_TYPE("Projectile.Type", ProjectileType.ARROW.name()),
    PROJECTILE_MATERIAL("Projectile.Material", Material.COAL_BLOCK.name()),
    PROJECTILE_PIERCING("Projectile.Piercing", 0),
    PROJECTILE_KNOCKBACK("Projectile.Knockback", 0),
    PROJECTILE_TAIL("Projectile.Tail.Toggle", false),
    PROJECTILE_TAIL_RED("Projectile.Tail.Red", 256),
    PROJECTILE_TAIL_GREEN("Projectile.Tail.Green", 256),
    PROJECTILE_TAIL_BLUE("Projectile.Tail.Blue", 256),
    PROJECTILE_TAIL_SIZE("Projectile.Tail.Size", 1),

    CRITICAL_CHANCE("Critical.Chance", .3),
    CRITICAL_MULTIPLIER("Critical.Multiplier", 2),
    CRITICAL_ACCURACY("Critical.Accuracy", .5),

    RANGE_TARGET("Range.Target", 10),
    RANGE_PICKUP_AMMUNITION("Range.Pickup Ammunition", 1),

    SILENT_TOWER("Silent.Tower", false),
    SILENT_PROJECTILE("Silent.Projectile", false),

    TOWER_CONSUMPTION("Tower.Consumption", 1),
    TOWER_DELAY("Tower.Delay", 20),
    TOWER_MAX_AMMO("Tower.Max Ammo", 0),
    TOWER_OFFSET("Tower.Offset", .55),
    TOWER_NAME_OFFSET("Tower.Name Offset", 1.85),
    TOWER_AMMUNITION_ITEM("Tower.Ammunition Item", new ItemStack(Material.ARROW)),
    TOWER_TURRET("Tower.Turret", StaticUtil.getHeadFromValue(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2MxMWEwZDkwYzM3ZWI2OTVjOGE1MjNkODYwMWFhMWM4NWZhZDA5YTRkMjIzMmQwNGVkMjNhYzkwZTQzMjVjMiJ9fX0=")),
    TOWER_BASE("Tower.Base", new ItemStack(Material.BEDROCK)),

    BLACKLIST("Blacklist", StaticUtil.defaultBlacklistTypes()),
    WHITELIST("Whitelist", new ArrayList<EntityType>()),
    POTION_EFFECTS("Potion Effects", new ArrayList<>());

    final String key;
    final Object value;

    ConfigDefaults(String key, Object value) {
        this.key = key;
        this.value = value;
    }

}
