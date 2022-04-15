package me.isaac.defencetowers.tower;

import me.isaac.defencetowers.ConfigDefaults;
import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.ProjectileType;
import me.isaac.defencetowers.StaticUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class TowerOptions {

    private final List<EntityType> blacklist = new ArrayList<>();
    private final List<EntityType> whitelist = new ArrayList<>();
    private final List<PotionEffect> potionEffects = new ArrayList<>();
    private final HashMap<HitType, Double> hitTypes = new HashMap<>();
    private DefenceTowersMain main;
    private File file;
    private YamlConfiguration yaml;
    private String display, name;
    private ItemStack turretItem, baseItem, ammunitionItem;
    private Material projectileMaterial;
    private boolean showDisplay, projectileGravity, tailToggle, silentTower, silentProjectiles, visualFire, useHealth;
    private int projectilesPerShot, shotConsumption, pierceLevel, knockback, fireTicks, maxAmmo, bounces, splits, splitAmount,
            tailRed, tailGreen, tailBlue, towerArmor, towerToughness;
    private double towerRange, projectileDamage, critChance, critMultiplier, critAccuracy, ammunitionPickupRange,
            towerOffset, nameOffset, bounceBoost, splitAccuracy, towerMaxHealth, regenAmount;
    private float towerAccuracy, projectileSpeed, tailSize;
    private long towerDelay, projectileGap, regenDelay;
    private ProjectileType projectileType;

    //TODO Animating towers when shooting via custom model data.

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
        yaml.set(ConfigDefaults.PROJECTILE_SPLITS.key, splits);
        yaml.set(ConfigDefaults.PROJECTILE_SPLIT_AMOUNT.key, splitAmount);
        yaml.set(ConfigDefaults.PROJECTILE_SPLIT_ACCURACY.key, splitAccuracy);
        yaml.set(ConfigDefaults.PROJECTILE_BOUNCE_BOOST.key, bounceBoost);

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
        yaml.set(ConfigDefaults.PROJECTILE_PIERCING.key, pierceLevel); // Doesnt work for all projectiles
        yaml.set(ConfigDefaults.PROJECTILE_KNOCKBACK.key, knockback);  // ^

        ArrayList<String> hitTypeStrings = new ArrayList<>();
        hitTypes.forEach((type, chance) -> {
            hitTypeStrings.add(type.toString() + " " + chance);
        });

        yaml.set(ConfigDefaults.PROJECTILE_HIT_TYPES.key, hitTypeStrings);

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
        yaml.set(ConfigDefaults.TOWER_USE_HEALTH.key, useHealth);
        yaml.set(ConfigDefaults.TOWER_MAX_HEALTH.key, towerMaxHealth);
        yaml.set(ConfigDefaults.TOWER_REGEN_DELAY.key, regenDelay);
        yaml.set(ConfigDefaults.TOWER_REGEN_AMOUNT.key, regenAmount);
        yaml.set(ConfigDefaults.TOWER_ARMOR.key, towerArmor);
        yaml.set(ConfigDefaults.TOWER_TOUGHNESS.key, towerToughness);
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
        useHealth = yaml.getBoolean(ConfigDefaults.TOWER_USE_HEALTH.key);

        projectilesPerShot = yaml.getInt(ConfigDefaults.PROJECTILE_PER_SHOT.key);
        projectileGap = yaml.getLong(ConfigDefaults.PROJECTILE_GAP.key);
        projectileGravity = yaml.getBoolean(ConfigDefaults.PROJECTILE_GRAVITY.key);
        projectileDamage = yaml.getDouble(ConfigDefaults.PROJECTILE_DAMAGE.key);
        projectileSpeed = (float) yaml.getDouble(ConfigDefaults.PROJECTILE_SPEED.key);
        towerAccuracy = yaml.getInt(ConfigDefaults.PROJECTILE_ACCURACY.key);
        bounces = yaml.getInt(ConfigDefaults.PROJECTILE_BOUNCES.key);
        splits = yaml.getInt(ConfigDefaults.PROJECTILE_SPLITS.key);
        splitAmount = yaml.getInt(ConfigDefaults.PROJECTILE_SPLIT_AMOUNT.key);
        bounceBoost = yaml.getDouble(ConfigDefaults.PROJECTILE_BOUNCE_BOOST.key);
        visualFire = yaml.getBoolean(ConfigDefaults.PROJECTILE_VISUAL_FIRE.key);
        fireTicks = yaml.getInt(ConfigDefaults.PROJECTILE_FIRE.key);
        splitAccuracy = yaml.getDouble(ConfigDefaults.PROJECTILE_SPLIT_ACCURACY.key);
        towerMaxHealth = yaml.getDouble(ConfigDefaults.TOWER_MAX_HEALTH.key);
        regenAmount = yaml.getDouble(ConfigDefaults.TOWER_REGEN_AMOUNT.key);

        tailToggle = yaml.getBoolean(ConfigDefaults.PROJECTILE_TAIL.key);
        tailRed = Math.min(256, Math.max(1, yaml.getInt(ConfigDefaults.PROJECTILE_TAIL_RED.key) - 1));
        tailGreen = Math.min(256, Math.max(1, yaml.getInt(ConfigDefaults.PROJECTILE_TAIL_GREEN.key) - 1));
        tailBlue = Math.min(256, Math.max(1, yaml.getInt(ConfigDefaults.PROJECTILE_TAIL_BLUE.key) - 1));
        tailSize = (float) Math.min(2, Math.max(.01, yaml.getDouble(ConfigDefaults.PROJECTILE_TAIL_SIZE.key)));

        regenDelay = Math.max(yaml.getLong(ConfigDefaults.TOWER_REGEN_DELAY.key), 1);

        for (String hitTypeStr : yaml.getStringList(ConfigDefaults.PROJECTILE_HIT_TYPES.key)) {
            String[] split = hitTypeStr.split("\\s+");
            try {
                double value = Math.max(.01, Double.parseDouble(split[1]));
                hitTypes.put(HitType.valueOf(split[0]), value);
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().log(Level.WARNING,
                        DefenceTowersMain.prefix + split[0] + " is not a valid hit type while loading " + name);
            }
        }

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
        towerDelay = Math.max(1, yaml.getInt(ConfigDefaults.TOWER_DELAY.key));
        shotConsumption = yaml.getInt(ConfigDefaults.TOWER_CONSUMPTION.key);
        ammunitionItem = yaml.getItemStack(ConfigDefaults.TOWER_AMMUNITION_ITEM.key);
        turretItem = yaml.getItemStack(ConfigDefaults.TOWER_TURRET.key);
        baseItem = yaml.getItemStack(ConfigDefaults.TOWER_BASE.key);
        towerOffset = yaml.getDouble(ConfigDefaults.TOWER_OFFSET.key);
        nameOffset = yaml.getDouble(ConfigDefaults.TOWER_NAME_OFFSET.key);
        towerArmor = yaml.getInt(ConfigDefaults.TOWER_ARMOR.key);
        towerToughness = yaml.getInt(ConfigDefaults.TOWER_TOUGHNESS.key);

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
                } catch (IllegalArgumentException ex) {
                    Bukkit.getLogger().log(Level.WARNING, "Unknown potion effect type: " + effectType);
                }

            }
        } catch (NullPointerException ignored) {
        }

        if (hitTypes.size() == 0) {
            hitTypes.put(HitType.BREAK, 1d);
            Bukkit.getLogger().log(Level.WARNING, ConfigDefaults.PROJECTILE_HIT_TYPES.key + " was empty, assuming hitType BREAK 1");
        }

        save();

    }

    public HashMap<HitType, Double> getHitTypes() {
        return hitTypes;
    }

    public HitType getRandomHitType() {

        double maxChance = 0, randomChance;
        for (double chances : hitTypes.values()) {
            maxChance += chances;
        }

        randomChance = ThreadLocalRandom.current().nextDouble(maxChance);
        maxChance = 0;

        for (HitType hitType : hitTypes.keySet()) {
            maxChance += hitTypes.get(hitType);
            if (randomChance <= maxChance) return hitType;
        }

        throw new ArithmeticException(randomChance + " out of bounds " + maxChance);

    }

    private void saveUpdate() {
        save();
        main.updateExistingTowers(name);
    }

    public int getProjectilesPerShot() {
        return projectilesPerShot;
    }

    public void setProjectilesPerShot(int projectilesPerShot) {
        this.projectilesPerShot = projectilesPerShot;
        saveUpdate();
    }

    public void setGap(int gap) {
        this.projectileGap = gap;
        saveUpdate();
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public void setFireTicks(int ticks) {
        fireTicks = ticks;
        saveUpdate();
    }

    public int getSplitAmount() {
        return splitAmount;
    }

    public void setSplitAmount(int splitAmount) {
        this.splitAmount = splitAmount;
        saveUpdate();
    }

    public int getBounces() {
        return bounces;
    }

    public void setBounces(int bounces) {
        this.bounces = bounces;
        saveUpdate();
    }

    public int getPierceLevel() {
        return pierceLevel;
    }

    public void setPierceLevel(int level) {
        pierceLevel = level;
        saveUpdate();
    }

    public int getKnockback() {
        return knockback;
    }

    public void setKnockback(int level) {
        knockback = level;
        saveUpdate();
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public void setMaxAmmo(int maxAmmo) {
        this.maxAmmo = maxAmmo;
        saveUpdate();
    }

    public int getSplits() {
        return splits;
    }

    public void setSplits(int splits) {
        this.splits = splits;
    }

    public int getShotConsumption() {
        return shotConsumption;
    }

    public void setShotConsumption(int shotConsumption) {
        this.shotConsumption = shotConsumption;
        saveUpdate();
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

    public void setColor(Color color) {
        tailRed = color.getRed();
        tailGreen = color.getGreen();
        tailBlue = color.getBlue();
        saveUpdate();
    }

    public Material getProjectileMaterial() {
        return projectileMaterial;
    }

    public void setProjectileMaterial(Material material) {
        projectileMaterial = material;
        saveUpdate();
    }

    public void setBase(ItemStack base) {
        baseItem = base;
        saveUpdate();
    }

    public void setTurret(ItemStack turret) {
        turretItem = turret;
        saveUpdate();
    }

    public void clearPotionEffects() {
        potionEffects.clear();
    }

    public ItemStack getAmmunitionItem() {
        return ammunitionItem;
    }

    public void setAmmunitionItem(ItemStack item) {
        ammunitionItem = item;
        saveUpdate();
    }

    public void addPotionEffect(PotionEffect potion) {
        potionEffects.add(potion);
        saveUpdate();
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public ItemStack getBaseItem() {
        return baseItem;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public void setProjectileType(ProjectileType type) {
        projectileType = type;
        saveUpdate();
    }

    public long getProjectileGap() {
        return projectileGap;
    }

    public long getTowerDelay() {
        return towerDelay;
    }

    public void setTowerDelay(int delay) {
        this.towerDelay = delay;
        saveUpdate();
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
        saveUpdate();
    }

    public ItemStack getTurretItem() {
        return turretItem;
    }

    public void setSpeed(float speed) {
        projectileSpeed = speed;
        saveUpdate();
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public float getTowerAccuracy() {
        return towerAccuracy;
    }

    public void setTowerAccuracy(float accuracy) {
        this.towerAccuracy = accuracy;
        saveUpdate();
    }

    public void setColorSize(float size) {
        tailSize = size;
        saveUpdate();
    }

    public float getTailSize() {
        return tailSize;
    }

    public boolean projectileHasGravity() {
        return projectileGravity;
    }

    public boolean shouldShowDisplay() {
        return showDisplay;
    }

    public boolean isVisualFire() {
        return visualFire;
    }

    public void setVisualFire(boolean visualFire) {
        this.visualFire = visualFire;
        saveUpdate();
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

    public void color(boolean color) {
        tailToggle = color;
        saveUpdate();
    }

    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double chance) {
        critChance = chance;
        saveUpdate();
    }

    public double getCritAccuracy() {
        return critAccuracy;
    }

    public void setCritAccuracy(double accuracy) {
        critAccuracy = accuracy;
        saveUpdate();
    }

    public double getCritMultiplier() {
        return critMultiplier;
    }

    public void setCritMultiplier(double multiplier) {
        critMultiplier = multiplier;
        saveUpdate();
    }

    public double getProjectileDamage() {
        return projectileDamage;
    }

    public void setProjectileDamage(Double damage) {
        projectileDamage = damage;
        saveUpdate();
    }

    public double getAmmunitionPickupRange() {
        return ammunitionPickupRange;
    }

    public double getTowerOffset() {
        return towerOffset;
    }

    public double getBounceBoost() {
        return bounceBoost;
    }

    public void setBounceBoost(double bounceBoost) {
        this.bounceBoost = bounceBoost;
        saveUpdate();
    }

    public double getNameOffset() {
        return nameOffset;
    }

    public void setRange(double range) {
        this.towerRange = range;
        saveUpdate();
    }

    public double getTowerRange() {
        return towerRange;
    }

    public void addHitType(HitType hitType, double chance) {
        hitTypes.put(hitType, chance);
        saveUpdate();
    }

    public void whitelist(EntityType type) {
        if (whitelist.contains(type)) return;
        whitelist.add(type);
        saveUpdate();
    }

    public List<EntityType> getWhitelist() {
        return whitelist;
    }

    public void clearWhitelist() {
        whitelist.clear();
        saveUpdate();
    }

    public void blacklist(EntityType type) {
        if (blacklist.contains(type)) return;
        blacklist.add(type);
        saveUpdate();
    }

    public List<EntityType> getBlacklist() {
        return blacklist;
    }

    public void clearBlacklist() {
        blacklist.clear();
        saveUpdate();
    }

    public double getSplitAccuracy() {
        return splitAccuracy;
    }

    public void setSplitAccuracy(double accuracy) {
        this.splitAccuracy = accuracy;
        saveUpdate();
    }

    public void useHealth(boolean useHealth) {
        this.useHealth = useHealth;
        saveUpdate();
    }

    public boolean isUsingHealth() {
        return useHealth;
    }

    public double getMaxHealth() {
        return towerMaxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.towerMaxHealth = maxHealth;
        saveUpdate();
    }

    public double getRegenAmount() {
        return regenAmount;
    }

    public void setRegenAmount(double regenAmount) {
        this.regenAmount = regenAmount;
        saveUpdate();
    }

    public long getRegenDelay() {
        return regenDelay;
    }

    public void setRegenDelay(long regenDelay) {
        this.regenDelay = regenDelay;
        saveUpdate();
    }

    public int getTowerArmor() {
        return towerArmor;
    }

    public void setTowerArmor(int towerArmor) {
        this.towerArmor = towerArmor;
        saveUpdate();
    }

    public int getTowerToughness() {
        return towerToughness;
    }

    public void setTowerToughness(int towerToughness) {
        this.towerToughness = towerToughness;
    }

}
