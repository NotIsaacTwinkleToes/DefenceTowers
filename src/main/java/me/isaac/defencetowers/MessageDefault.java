package me.isaac.defencetowers;

import java.util.ArrayList;
import java.util.List;

public enum MessageDefault {
    PREFIX("Prefix", "&7[&dDefence Towers&7]&r"),

    TOWER_RADIUS_NAME("Tower.Radius.Name", "&rTower Radius"),
    TOWER_RADIUS_DESCRIPTION("Tower.Radius.Description", "&7Display the towers radius"),
    TOWER_BLACKLIST_NAME("Tower.Blacklist.Name", "&rPlayer Blacklist"),
    TOWER_BLACKLIST_DESCRIPTION("Tower.Blacklist.Description", List.of("&7Players not in the blacklist cannot interact with this tower.", "&8Players the tower will not target:")),
    TOWER_AMMUNITION_NAME("Tower.Ammunition.Name", "&rAmmunition: &6%AMMO%"),
    TOWER_AMMUNITION_DESCRIPTION("Tower.Ammunition.Description", List.of("&7Right-click to switch targeting mode", "&8Target Mode: &f%MODE%", "&7Shift + click arrows in your inventory to put them in the tower")),
    TOWER_RIDE_NAME("Tower.Contorl.Name", "&rControl Tower"),
    TOWER_RIDE_DESCRIPTION("Tower.Control.Description", List.of("")),

    TOWER_HEALTH_MESSAGE("Tower.Health Display", "&cTower Health: &4%HEALTH%♥"),
    TOWER_COOLDOWN_BAR("Tower.Cooldown Bar", "&r%AMMO% %BAR%"),

    TOWER_ITEM_DESCRIPTION("Tower.Item Description", List.of("&7Damage: %PROJECTILE_DAMAGE%", "&8Range: %RANGE_TARGET%", "&7Projectiles Per Shot: %PROJECTILE_PER_SHOT%", "&8Shot Consumption: %TOWER_CONSUMPTION%", "&7Accuracy: %PROJECTILE_ACCURACY%", "&8Projectile Speed: %PROJECTILE_SPEED%", "&7Rate of Fire: %TOWER_DELAY%"));

    public final String path;
    public final Object value;

    MessageDefault(String path, Object value) {
        this.path = path;
        this.value = value;
    }

}
