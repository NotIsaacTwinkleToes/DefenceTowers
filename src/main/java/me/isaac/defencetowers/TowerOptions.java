package me.isaac.defencetowers;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TowerOptions {

    private DefenceTowersMain main;
    private File file;
    private YamlConfiguration yaml;

    private String display, name;
    private ItemStack turretItem, baseItem, ammunitionItem;
    private Material projectileMaterial;
    private boolean showDisplay, projectileGravity, tailToggle, silentTower, silentProjectiles, visualFire;
    private int projectilesPerShot, shotConsumption, pierceLevel, knockback, fireTicks, maxAmmo, bounces,
    tailRed, tailGreen, tailBlue;
    private double towerRange, projectileDamage, critChance, critMultiplier, critAccuracy, ammunitionPickupRange,
    towerOffset, nameOffset;
    private float towerAccuracy, projectileSpeed, tailSize;
    private long towerDelay, projectileGap;
    private ProjectileType projectileType;
    private final List<EntityType> blacklist = new ArrayList<>();
    private final List<EntityType> whitelist = new ArrayList<>();
    private final List<PotionEffect> potionEffects = new ArrayList<>();

    public TowerOptions(DefenceTowersMain main, String name, boolean create) {
        this.main = main;
        this.name = name;
        file = new File("plugins//DefenceTowers//Towers//" + name + ".yml");
        yaml = YamlConfiguration.loadConfiguration(file);
        display = name;

        if (file.exists()) {
            StaticUtil.checkConfig(name);
            loadFile();
        }

        if (file.exists() || !create)
            return;

        save();

    }

    private void save() {

        yaml.set(ConfigDefaults.DISPLAY_SHOW.key, showDisplay);
        yaml.set(ConfigDefaults.DISPLAY_DISPLAY_NAME.key, display);

        yaml.set(ConfigDefaults.PROJECTILE_PER_SHOT.key, projectilesPerShot);
        yaml.set(ConfigDefaults.PROJECTILE_GAP.key, projectileGap);
        yaml.set(ConfigDefaults.PROJECTILE_GRAVITY.key, projectileGravity);
        yaml.set(ConfigDefaults.PROJECTILE_DAMAGE.key, projectileDamage);
        yaml.set(ConfigDefaults.PROJECTILE_SPEED.key, projectileSpeed);
        yaml.set(ConfigDefaults.PROJECTILE_ACCURACY.key, towerAccuracy);
        yaml.set(ConfigDefaults.PROJECTILE_VISUAL_FIRE.key, visualFire);
        yaml.set(ConfigDefaults.PROJECTILE_FIRE.key, fireTicks);
        yaml.set(ConfigDefaults.PROJECTILE_BOUNCES.key, bounces);

        try {
            yaml.set(ConfigDefaults.PROJECTILE_TYPE.key, projectileType.toString());
        } catch (NullPointerException ex) {
            yaml.set(ConfigDefaults.PROJECTILE_TYPE.key, ProjectileType.ARROW.toString());
        }

        try {
            yaml.set(ConfigDefaults.PROJECTILE_MATERIAL.key, projectileMaterial.toString());
        } catch (NullPointerException ex) {
            yaml.set(ConfigDefaults.PROJECTILE_MATERIAL.key, Material.COAL_BLOCK.toString());
        }

        yaml.set(ConfigDefaults.PROJECTILE_TAIL.key, tailToggle);
        yaml.set(ConfigDefaults.PROJECTILE_TAIL_RED.key, tailRed + 1);
        yaml.set(ConfigDefaults.PROJECTILE_TAIL_GREEN.key, tailGreen + 1);
        yaml.set(ConfigDefaults.PROJECTILE_TAIL_BLUE.key, tailBlue + 1);
        yaml.set(ConfigDefaults.PROJECTILE_TAIL_SIZE.key, tailSize);
        yaml.set(ConfigDefaults.PROJECTILE_PIERCING.key, pierceLevel);// Arrow spicific
        yaml.set(ConfigDefaults.PROJECTILE_KNOCKBACK.key, knockback);

        yaml.set(ConfigDefaults.CRITICAL_CHANCE.key, critChance);
        yaml.set(ConfigDefaults.CRITICAL_MULTIPLIER.key, critMultiplier);
        yaml.set(ConfigDefaults.CRITICAL_ACCURACY.key, critAccuracy);

        yaml.set(ConfigDefaults.RANGE_TARGET.key, towerRange);
        yaml.set(ConfigDefaults.RANGE_PICKUP_AMMUNITION.key, ammunitionPickupRange);

        yaml.set(ConfigDefaults.SILENT_TOWER.key, silentTower);
        yaml.set(ConfigDefaults.SILENT_PROJECTILE.key, silentProjectiles);

        for (PotionEffect effects : potionEffects) {

            yaml.set("Potion Effects." + effects.getType().getName() + ".Amplifier", effects.getAmplifier());
            yaml.set("Potion Effects." + effects.getType().getName() + ".Duration", effects.getDuration());
            yaml.set("Potion Effects." + effects.getType().getName() + ".Is Ambient", effects.isAmbient());
            yaml.set("Potion Effects." + effects.getType().getName() + ".Has Particles", effects.hasParticles());
            yaml.set("Potion Effects." + effects.getType().getName() + ".Has Icon", effects.hasIcon());

        }

        yaml.set(ConfigDefaults.TOWER_CONSUMPTION.key, shotConsumption);
        yaml.set(ConfigDefaults.TOWER_DELAY.key, towerDelay);
        yaml.set(ConfigDefaults.TOWER_MAX_AMMO.key, maxAmmo);
        yaml.set(ConfigDefaults.TOWER_OFFSET.key, towerOffset);
        yaml.set(ConfigDefaults.TOWER_NAME_OFFSET.key, nameOffset);
        yaml.set(ConfigDefaults.TOWER_AMMUNITION_ITEM.key, ammunitionItem);
        yaml.set(ConfigDefaults.TOWER_TURRET.key, turretItem);
        yaml.set(ConfigDefaults.TOWER_BASE.key, baseItem);

        List<String> blacklistNames = new ArrayList<>(), whitelistNames = new ArrayList<>();

        blacklist.forEach(type -> blacklistNames.add(type.toString()));
        whitelist.forEach(type -> whitelistNames.add(type.toString()));

        yaml.set(ConfigDefaults.BLACKLIST.key, blacklistNames);
        yaml.set(ConfigDefaults.WHITELIST.key, whitelistNames);

        try {
            yaml.save(file);
            if (!file.exists())
                file.createNewFile();
        } catch (IOException ignored) {
        }

    }

    private void loadFile() {

        yaml = YamlConfiguration.loadConfiguration(file);

        showDisplay = yaml.getBoolean(ConfigDefaults.DISPLAY_SHOW.key);
        display = yaml.getString(ConfigDefaults.DISPLAY_DISPLAY_NAME.key);

        projectilesPerShot = yaml.getInt(ConfigDefaults.PROJECTILE_PER_SHOT.key);
        projectileGap = yaml.getLong(ConfigDefaults.PROJECTILE_GAP.key);
        projectileGravity = yaml.getBoolean(ConfigDefaults.PROJECTILE_GRAVITY.key);
        projectileDamage = yaml.getDouble(ConfigDefaults.PROJECTILE_DAMAGE.key);
        projectileSpeed = (float) yaml.getDouble(ConfigDefaults.PROJECTILE_SPEED.key);
        towerAccuracy = yaml.getInt(ConfigDefaults.PROJECTILE_ACCURACY.key);
        bounces = yaml.getInt(ConfigDefaults.PROJECTILE_BOUNCES.key);
        visualFire = yaml.getBoolean(ConfigDefaults.PROJECTILE_VISUAL_FIRE.key);
        fireTicks = yaml.getInt(ConfigDefaults.PROJECTILE_FIRE.key);
        tailToggle = yaml.getBoolean(ConfigDefaults.PROJECTILE_TAIL.key);

        tailRed = yaml.getInt(ConfigDefaults.PROJECTILE_TAIL_RED.key) - 1;
        tailGreen = yaml.getInt(ConfigDefaults.PROJECTILE_TAIL_GREEN.key) - 1;
        tailBlue = yaml.getInt(ConfigDefaults.PROJECTILE_TAIL_BLUE.key) - 1;
        tailSize = (float) yaml.getDouble(ConfigDefaults.PROJECTILE_TAIL_SIZE.key);

        if (tailRed < 1) tailRed = 1;
        else if (tailRed > 256) tailRed = 256;
        if (tailGreen < 1) tailGreen = 1;
        else if (tailGreen > 256) tailGreen = 256;
        if (tailBlue < 1) tailBlue = 1;
        else if (tailBlue > 256) tailBlue = 256;
        if (tailSize < 0) tailSize = 0;
        else if (tailSize > 2) tailSize = 2;

        try {
            projectileMaterial = Material.valueOf(yaml.getString(ConfigDefaults.PROJECTILE_MATERIAL.key));
        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Unknown material type. Setting to COAL_BLOCK");
            projectileMaterial = Material.COAL_BLOCK;
        }

        try {
            projectileType = ProjectileType.valueOf(yaml.getString(ConfigDefaults.PROJECTILE_TYPE.key));
        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Unknown projectile type. Setting to ARROW");
            projectileType = ProjectileType.ARROW;
        }

        pierceLevel = yaml.getInt(ConfigDefaults.PROJECTILE_PIERCING.key);
        knockback = yaml.getInt(ConfigDefaults.PROJECTILE_KNOCKBACK.key);

        critChance = yaml.getDouble(ConfigDefaults.CRITICAL_CHANCE.key);
        critMultiplier = yaml.getDouble(ConfigDefaults.CRITICAL_MULTIPLIER.key);
        critAccuracy = yaml.getDouble(ConfigDefaults.CRITICAL_ACCURACY.key);

        maxAmmo = yaml.getInt(ConfigDefaults.TOWER_MAX_AMMO.key);
        towerDelay = yaml.getInt(ConfigDefaults.TOWER_DELAY.key);
        shotConsumption = yaml.getInt(ConfigDefaults.TOWER_CONSUMPTION.key);
        ammunitionItem = yaml.getItemStack(ConfigDefaults.TOWER_AMMUNITION_ITEM.key);
        turretItem = yaml.getItemStack(ConfigDefaults.TOWER_TURRET.key);
        baseItem = yaml.getItemStack(ConfigDefaults.TOWER_BASE.key);
        towerOffset = yaml.getDouble(ConfigDefaults.TOWER_OFFSET.key);
        nameOffset = yaml.getDouble(ConfigDefaults.TOWER_NAME_OFFSET.key);

        towerRange = yaml.getDouble(ConfigDefaults.RANGE_TARGET.key);
        ammunitionPickupRange = yaml.getDouble(ConfigDefaults.RANGE_PICKUP_AMMUNITION.key);

        silentTower = yaml.getBoolean(ConfigDefaults.SILENT_TOWER.key);
        silentProjectiles = yaml.getBoolean(ConfigDefaults.SILENT_PROJECTILE.key);

        blacklist.clear();
        potionEffects.clear();

        for (String type : yaml.getStringList(ConfigDefaults.BLACKLIST.key)) {
            try {
                blacklist.add(EntityType.valueOf(type));
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().log(Level.WARNING,
                        DefenceTowersMain.prefix + type + " is not a valid entity type while loading " + name);
            }
        }

        for (String type : yaml.getStringList(ConfigDefaults.WHITELIST.key)) {
            try {
                whitelist.add(EntityType.valueOf(type));
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().log(Level.WARNING,
                        DefenceTowersMain.prefix + type + " is not a valid entity type while loading " + name);
            }
        }

        try {
            for (String effectType : yaml.getConfigurationSection("Potion Effects").getKeys(false)) {

                int amplifier = yaml.getInt("Potion Effects." + effectType + ".Amplifier");
                int duration = yaml.getInt("Potion Effects." + effectType + ".Duration");
                boolean ambient = yaml.getBoolean("Potion Effects." + effectType + ".Is Ambient");
                boolean hasParticles = yaml.getBoolean("Potion Effects." + effectType + ".Has Particles");
                boolean hasIcon = yaml.getBoolean("Potion Effects." + effectType + ".Has Icon");

                try {
                    potionEffects.add(new PotionEffect(PotionEffectType.getByName(effectType), duration, amplifier, ambient,
                            hasParticles, hasIcon));
                } catch(IllegalArgumentException ex) {
                    Bukkit.getLogger().log(Level.WARNING, "Unknown potion effect type: " + effectType);
                }

            }
        } catch (NullPointerException ignored) {
        }

        if (towerDelay <= 0)
            towerDelay = 1;

    }

    public void setPerShot(int perShot) {
        this.projectilesPerShot = perShot;
        saveUpdate();
    }

    public void setProjectileMaterial(Material material) {
        projectileMaterial = material;
        saveUpdate();
    }

    public void setGap(int gap) {
        this.projectileGap = gap;
        saveUpdate();
    }

    public void setSpeed(float speed) {
        projectileSpeed = speed;
        saveUpdate();
    }

    public void setTowerAccuracy(float accuracy) {
        this.towerAccuracy = accuracy;
        saveUpdate();
    }

    public void setVisualFire(boolean visualFire) {
        this.visualFire = visualFire;
        saveUpdate();
    }

    public void setFireTicks(int ticks) {
        fireTicks = ticks;
        saveUpdate();
    }

    public void setTowerDelay(int delay) {
        this.towerDelay = delay;
        saveUpdate();
    }

    public void setRange(double range) {
        this.towerRange = range;
        saveUpdate();
    }

    public void color(boolean color) {
        tailToggle = color;
        saveUpdate();
    }

    public void setColor(Color color) {
        tailRed = color.getRed();
        tailGreen = color.getGreen();
        tailBlue = color.getBlue();
        saveUpdate();
    }

    public void setBounces(int bounces) {
        this.bounces = bounces;
        saveUpdate();
    }

    public void setPierceLevel(int level) {
        pierceLevel = level;
        saveUpdate();
    }

    public void setKnockback(int level) {
        knockback = level;
        saveUpdate();
    }

    public void whitelist(EntityType type) {
        if (whitelist.contains(type)) return;
        whitelist.add(type);
        saveUpdate();
    }

    public void blacklist(EntityType type) {
        if (blacklist.contains(type)) return;
        blacklist.add(type);
        saveUpdate();
    }

    public void clearBlacklist() {
        blacklist.clear();
        saveUpdate();
    }

    public void clearWhitelist() {
        whitelist.clear();
        saveUpdate();
    }

    public void setProjectileDamage(Double damage) {
        projectileDamage = damage;
        saveUpdate();
    }

    public void setBase(ItemStack base) {
        baseItem = base;
        saveUpdate();
    }

    public void setProjectileType(ProjectileType type) {
        projectileType = type;
        saveUpdate();
    }

    public void setTurret(ItemStack turret) {
        turretItem = turret;
        saveUpdate();
    }

    public void setDisplay(String display) {
        this.display = display;
        saveUpdate();
    }

    public void setCritChance(double chance) {
        critChance = chance;
        saveUpdate();
    }

    public void setCritMultiplier(double multiplier) {
        critMultiplier = multiplier;
        saveUpdate();
    }

    public void setCritAccuracy(double accuracy) {
        critAccuracy = accuracy;
        saveUpdate();
    }

    public void clearPotionEffects() {
        potionEffects.clear();
    }

    public void setColorSize(float size) {
        tailSize = size;
        saveUpdate();
    }

    public ItemStack getAmmunitionItem() {
        return ammunitionItem;
    }

    public void setAmmunitionItem(ItemStack item) {
        ammunitionItem = item;
        saveUpdate();
    }

    public List<EntityType> getWhitelist() {
        return whitelist;
    }

    public List<EntityType> getBlacklist() {
        return blacklist;
    }

    public boolean projectileHasGravity() {
        return projectileGravity;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public double getTowerRange() {
        return towerRange;
    }

    public void addPotionEffect(PotionEffect potion) {
        potionEffects.add(potion);
        saveUpdate();
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public double getProjectileDamage() {
        return projectileDamage;
    }

    public int getProjectilesPerShot() {
        return projectilesPerShot;
    }

    public int getShotConsumption() {
        return shotConsumption;
    }

    public int getPierceLevel() {
        return pierceLevel;
    }

    public int getKnockback() {
        return knockback;
    }

    public float getTowerAccuracy() {
        return towerAccuracy;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public float getTailSize() {
        return tailSize;
    }

    public int getBounces() {
        return bounces;
    }

    public double getAmmunitionPickupRange() {
        return ammunitionPickupRange;
    }

    public boolean shouldShowDisplay() {
        return showDisplay;
    }

    public double getTowerOffset() {
        return towerOffset;
    }

    public double getNameOffset() {
        return nameOffset;
    }

    public ItemStack getBaseItem() {
        return baseItem;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public long getProjectileGap() {
        return projectileGap;
    }

    public Material getProjectileMaterial() {
        return projectileMaterial;
    }

    public double getCritChance() {
        return critChance;
    }

    public double getCritAccuracy() {
        return critAccuracy;
    }

    public double getCritMultiplier() {
        return critMultiplier;
    }

    public int getTailRed() {
        return tailRed;
    }

    public int getTailGreen() {
        return tailGreen;
    }

    public int getTailBlue() {
        return tailBlue;
    }

    public boolean isVisualFire() {
        return visualFire;
    }

    public boolean isSilentProjectiles() {
        return silentProjectiles;
    }

    public boolean isTail() {
        return tailToggle;
    }

    public boolean isSilentTower() {
        return silentTower;
    }

    public long getTowerDelay() {
        return towerDelay;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    private void saveUpdate() {
        save();
        main.updateExistingTowers(name);
    }

    public String getDisplay() {
        return display;
    }

    public ItemStack getTurretItem() {
        return turretItem;
    }

}
