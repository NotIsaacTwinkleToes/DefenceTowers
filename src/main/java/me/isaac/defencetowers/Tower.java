package me.isaac.defencetowers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class Tower {

    private String name;
    private final Inventory inventory;
    private final DefenceTowersMain main;
    private File file;
    private YamlConfiguration yaml;

    private Location location;
    private Vector direction;
    private Player operator = null;
    private ArmorStand stand = null, baseStand = null;

    private int lastTick = 0;

    private String display;
    private ItemStack turret, base, ammunitionItem;
    private Material projectileMaterial;
    private boolean displaying = false, showDisplay, bulletGravity, canShoot = false, tailToggle,
            silentTower, silentArrows, visualFire;
    private int bulletsPerShot, shotConsumption, pierceLevel, knockback, fireTicks, currentAmmo = 0,
            maxAmmo, bounces, taskID, tailRed, tailGreen, tailBlue;
    private double towerRange, projectileDamage, critChance, critMultiplier, critAccuracy, arrowPickupRange;
    private float towerAccuracy, projectileSpeed, tailSize;
    private long towerDelay, projectileGap, delay = 0;
    private double towerOffset;
    private ProjectileType projectileType;
    private final List<EntityType> blacklist = new ArrayList<>();
    private final List<EntityType> whitelist = new ArrayList<>();
    private final List<UUID> blacklistedPlayers = new ArrayList<>();
    private final List<PotionEffect> potionEffects = new ArrayList<>();

    private TargetType targetType = TargetType.CLOSEST;

    public Tower(DefenceTowersMain main, String name, Location location, boolean create) {
        this.main = main;
        this.name = name;
        display = name;
        file = new File("plugins//DefenceTowers//Towers//" + name + ".yml");
        yaml = YamlConfiguration.loadConfiguration(file);

        inventory = Bukkit.createInventory(null, InventoryType.HOPPER,
                ChatColor.translateAlternateColorCodes('&', display));

        if (file.exists()) {
            StaticUtil.checkConfig(this.getName());
            loadFile();
        }

        if (location != null) {
            this.location = location;
            startStand();
        }

        if (file.exists() || !create)
            return;

        save();

    }

    public ArmorStand getBaseStand() {
        return baseStand;
    }

    public ArmorStand getStand() {
        return stand;
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

    public static boolean exists(String name) {
        return new File("plugins//DefenceTowers//Towers//" + name + ".yml").exists();
    }

    public boolean kickOperator() {
        if (operator == null)
            return false;
        operator.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
        operator = null;
        if (stand.getPassengers().contains(operator))
            stand.removePassenger(operator);
        return true;
    }

    public Player getOperator() {
        return operator;
    }

    public void setOperator(Player player) {
        operator = player;
        stand.addPassenger(player);
    }

    public boolean getCanShoot() {
        return canShoot;
    }

    public void setCanShoot(boolean canShoot) {
        this.canShoot = canShoot;
    }

    public void startStand() {

        baseStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        baseStand.setGravity(false);
        baseStand.getEquipment().setHelmet(base);
        baseStand.setMarker(true);
        baseStand.setInvisible(true);

        stand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, towerOffset, 0),
                EntityType.ARMOR_STAND);

        stand.setGravity(false);
        stand.getEquipment().setHelmet(turret);
        stand.setCustomName(ChatColor.translateAlternateColorCodes('&', display));
        stand.setCustomNameVisible(showDisplay);
        stand.getPersistentDataContainer().set(main.getKeys().turretStand, PersistentDataType.STRING, name);
        stand.setInvulnerable(true);
        stand.setVisible(false);

        direction = stand.getLocation().getDirection();

        main.addTowerStand(stand, this);

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
            public void run() {

                if (!stand.isValid()) {
                    main.removeTower(stand);
                    displaying = false;
                    Bukkit.getScheduler().cancelTask(taskID);
                }

                lastTick++;

                if (currentAmmo < maxAmmo) {
                    for (Entity entity : stand.getNearbyEntities(arrowPickupRange, arrowPickupRange, arrowPickupRange)) {
                        if (entity instanceof Item) {
                            Item item = (Item) entity;
                            if (item.getItemStack().getType() != Material.ARROW) continue;

                            int amount = item.getItemStack().getAmount();

                            if (maxAmmo > 0 && currentAmmo + amount > maxAmmo) {
                                amount -= maxAmmo - currentAmmo;
                                setAmmo(maxAmmo);
                            } else {
                                setAmmo(currentAmmo + amount);
                                amount = 0;
                            }

                            item.getItemStack().setAmount(amount);

                        }
                    }
                }

                canShoot = delay >= towerDelay;

                if (operator == null) {
                    boolean target = true;
                    try {
                        direction = noRiderOperation();
                    } catch (Exception e) {
                        target = false;
                    }

                    if (canShoot) if (target) shoot(projectileType, direction);

                } else
                    direction = operator.getLocation().getDirection();

//                towersActiveProjectileList.forEach(projectile -> projectile.getWorld().spawnParticle(Particle.REDSTONE, projectile.getLocation(), 1, new DustOptions(Color.fromRGB(tailRed, tailGreen, tailBlue), tailSize)));

                for (int i = 0; i < towersActiveProjectileList.size(); i++) {

                    Projectile projectile = towersActiveProjectileList.get(i);

                    try {
                        if (!projectile.isValid()) {
                            towersActiveProjectileList.remove(i);
                            continue;
                        }
                    } catch (NullPointerException ex) {
                        towersActiveProjectileList.remove(i);
                    }

                    projectile.getWorld().spawnParticle(Particle.REDSTONE, projectile.getLocation(), 1, new DustOptions(Color.fromRGB(tailRed, tailGreen, tailBlue), tailSize));

                }

                delay++;

                if (operator != null) displayShootCooldown();

                setHeadDirection(direction);

                if (displaying) {
                    aimingDraw(direction);

                    boolean containsPlayer = false;

                    for (Entity entity : stand.getNearbyEntities(towerRange * 1.5, towerRange * 1.5, towerRange * 1.5)) {
                        if (entity instanceof Player) {
                            containsPlayer = true;
                            break;
                        }
                    }

                    if (!containsPlayer)
                        displaying = false;
                }

            }
        }, 0, 1);

    }

    public void displayShootCooldown() {
        if (operator == null) return;
        String cooldownBar = "";

        float progress = (float) delay / towerDelay;

        int i = 0;

        while (i < progress * 10 && i < 10) {
            cooldownBar += ChatColor.AQUA + "█";
            i++;
        }

        while (i < 10) {
            cooldownBar += ChatColor.DARK_GRAY + "█";
            i++;
        }

        operator.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColor.WHITE + StaticUtil.format.format(currentAmmo) + " " + ChatColor.AQUA + cooldownBar));

    }

    public void setHeadDirection(Vector direction) {
        Location clone = stand.getLocation().clone();
        try {
            clone.setDirection(direction);
        } catch (NullPointerException ignored) {}
        stand.setHeadPose(new EulerAngle(Math.toRadians(clone.getPitch()), Math.toRadians(clone.getYaw()), 0));

    }

    public void aimingDraw(Vector direction) {
        Location particleLocation = location.clone().add(0, 2, 0);

        double particleOffset = towerAccuracy / 100;

        while (true) {
            location.getWorld().spawnParticle(Particle.WAX_OFF, particleLocation, 1, particleOffset, particleOffset,
                    particleOffset, 0);
            particleLocation.add(direction.normalize().multiply(.4));
            if (particleLocation.distance(location) > towerRange)
                return;
        }

    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public void setProjectileType(ProjectileType projectileType) {
        this.projectileType = projectileType;
    }

    List<Projectile> towersActiveProjectileList = new ArrayList<>();

    public void addActiveProjectile(Projectile projectile) {
        towersActiveProjectileList.add(projectile);
    }

    public void shoot(ProjectileType type, Vector direction) {

        if (direction == null) throw new IllegalArgumentException("Direction cannot be null");

        if (!canShoot)
            return;
        if (currentAmmo < shotConsumption) {
            if (!silentTower && lastTick >= 20) {
                stand.getWorld().playSound(stand.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, .7f, 1);
                lastTick = 0;
            }
            return;
        }

        delay = 0;
        setAmmo(currentAmmo - shotConsumption);

        for (int i = 0; i < bulletsPerShot; i++) {

            new BukkitRunnable() {

                Projectile projectile;

                public void run() {

                    switch (type) {
                        case ARROW:
                            projectile = shootArrow(stand.getEyeLocation(), direction);
                            break;
                        case ITEM:
                            projectile = shootItem(stand.getEyeLocation(), direction);
                            break;
                        case TRIDENT:
                            projectile = shootTrident(stand.getEyeLocation(), direction);
                            break;
                        case LARGE_FIREBALL:
                            projectile = shootFireball(stand.getEyeLocation(), direction, false);
                            break;
                        case SMALL_FIREBALL:
                            projectile = shootFireball(stand.getEyeLocation(), direction, true);
                            break;
                        case WITHER_SKULL:
                            projectile = shootWitherSkull(stand.getEyeLocation(), direction);
                            break;
                    }

                    towersActiveProjectileList.add(projectile);

                }
            }.runTaskLater(main, i * projectileGap);

        }

    }

    private double criticalMultiplier = 1;

    public Projectile shootArrow(Location location, Vector direction) {
        Arrow arrow = location.getWorld().spawnArrow(location, direction, (float) projectileSpeed, towerAccuracy);
        arrow.setRotation(location.getYaw(), location.getPitch());
        updateProjectile(arrow);
        return arrow;
    }

    public Projectile shootWitherSkull(Location location, Vector direction) {
        Arrow arrow = location.getWorld().spawnArrow(location, direction, (float) projectileSpeed, towerAccuracy);
        Vector velocity = arrow.getVelocity();
        arrow.remove();

        WitherSkull skull = (WitherSkull) location.getWorld().spawnEntity(location, EntityType.WITHER_SKULL);

        skull.setCharged(false);
        skull.setDirection(velocity);
        skull.setGravity(bulletGravity);
        skull.setRotation(stand.getLocation().getYaw(), stand.getLocation().getPitch());

        return skull;

    }

    public Projectile shootTrident(Location location, Vector direction) {
        Arrow arrow = location.getWorld().spawnArrow(location, direction, (float) projectileSpeed, towerAccuracy);
        Vector velocity = arrow.getVelocity();
        arrow.remove();

        Trident trident = (Trident) location.getWorld().spawnEntity(location, EntityType.TRIDENT);
        trident.setVelocity(velocity);
        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        trident.setPierceLevel(pierceLevel);
        trident.setKnockbackStrength(knockback);
        trident.setRotation(stand.getLocation().getYaw(), stand.getLocation().getPitch());
        updateProjectile(trident);

        return trident;
    }

    public Projectile shootItem(Location location, Vector direction) {
        Arrow arrow = location.getWorld().spawnArrow(location, direction, (float) projectileSpeed, towerAccuracy);
        Vector velocity = arrow.getVelocity();
        arrow.remove();
        Snowball snowball = (Snowball) location.getWorld().spawnEntity(location, EntityType.SNOWBALL);
        snowball.setVelocity(velocity);
        snowball.setItem(new ItemStack(projectileMaterial));
        updateProjectile(snowball);
        return snowball;
    }

    public Projectile shootFireball(Location location, Vector direction, boolean small) {
        Arrow arrow = stand.getWorld().spawnArrow(location, direction, (float) projectileSpeed, towerAccuracy);
        Vector velocity = arrow.getVelocity();
        arrow.remove();

        Fireball fireball;
        if (small) fireball = (Fireball) location.getWorld().spawnEntity(location, EntityType.SMALL_FIREBALL);
        else fireball = (Fireball) location.getWorld().spawnEntity(location, EntityType.FIREBALL);
        fireball.setDirection(velocity);
        fireball.setVelocity(velocity);
        updateProjectile(fireball);
        return fireball;
    }

    private void updateProjectile(Projectile projectile) {

        PersistentDataContainer pdc = projectile.getPersistentDataContainer();

        double tempDamage = projectileDamage;

        if (Math.random() <= getCritChance())
            criticalMultiplier = (Math.random() >= .5 ? 1 : -1) * (Math.random() * getCritAccuracy() - getCritAccuracy()) + getCritMultiplier();

        pdc.set(main.getKeys().critical, PersistentDataType.DOUBLE, criticalMultiplier);

        pdc.set(main.getKeys().bullet, PersistentDataType.STRING, name);
        pdc.set(main.getKeys().bulletDamage, PersistentDataType.DOUBLE, projectileDamage);
        pdc.set(main.getKeys().bounces, PersistentDataType.INTEGER, bounces);
        pdc.set(main.getKeys().fire, PersistentDataType.INTEGER, fireTicks);
        pdc.set(main.getKeys().tail, PersistentDataType.STRING, tailToggle + " " + tailRed + " " + tailGreen + " " + tailBlue);

        if (projectile instanceof Arrow) {
            ((Arrow) projectile).setPierceLevel(pierceLevel);
            ((Arrow) projectile).setKnockbackStrength(knockback);
        } else {
            pdc.set(main.getKeys().pierce, PersistentDataType.INTEGER, pierceLevel);
            pdc.set(main.getKeys().knockback, PersistentDataType.INTEGER, knockback);
        }

        projectile.setVisualFire(visualFire);
        projectile.setSilent(silentArrows);

    }

    public void addPotionEffect(PotionEffect potionEffect) {
        potionEffects.add(potionEffect);
        save();
        main.updateExistingTowers(name);
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public Inventory getInventory() {

        updateItems();

        return inventory;

    }

    public void setTargetType(TargetType type) {
        targetType = type;
        updateItems();
    }

    public TargetType getTargetType() {
        return targetType;
    }

    private void updateItems() {
        ItemStack ammunitionItem = this.ammunitionItem.clone();
        ItemMeta ammunitionMeta = main.towerItems.getAmmunition().getItemMeta();
        ammunitionMeta.setDisplayName(ChatColor.WHITE + "Ammunition: " + ChatColor.GOLD + StaticUtil.format.format(currentAmmo));
        List<String> lore = ammunitionMeta.getLore();

        lore.set(1, org.bukkit.ChatColor.DARK_GRAY + "Target Mode: " + org.bukkit.ChatColor.WHITE + targetType.toString());

        ammunitionMeta.setLore(lore);
        ammunitionItem.setItemMeta(ammunitionMeta);
        ammunitionItem.setAmount(currentAmmo < 1 ? 1 : (currentAmmo > 64 ? 64 : currentAmmo));

        ItemStack blacklistItem = main.towerItems.getBlacklist().clone();
        ItemMeta blacklistMeta = blacklistItem.getItemMeta();
        List<String> blacklistLore = blacklistMeta.getLore();
        boolean color = true;
        blacklistedPlayers.forEach(id -> {
            blacklistLore.add((color ? ChatColor.DARK_GRAY : ChatColor.GRAY) + Bukkit.getOfflinePlayer(id).getName());
        });
        blacklistMeta.setLore(blacklistLore);
        blacklistItem.setItemMeta(blacklistMeta);

        inventory.setItem(1, main.towerItems.getToggleRadius());
        inventory.setItem(2, blacklistItem);
        inventory.setItem(3, ammunitionItem);
        inventory.setItem(4, main.towerItems.getRide());
    }

    public boolean isDisplaying() {
        return displaying;
    }

    public void displayRange(boolean display) {

        displaying = display;

        if (!display)
            return;

        new BukkitRunnable() {
            final int points = (int) (towerRange * 5);

            public void run() {

                Location particleLocation = location.clone().add(0, 2.7, 0);

                Color color = Color.fromRGB(ThreadLocalRandom.current().nextInt(256),
                        ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256));

                for (double i = 0; i < points; i += .1) {

                    double x = towerRange * Math.sin(i);
                    double z = towerRange * Math.cos(i);

                    location.getWorld().spawnParticle(Particle.REDSTONE, particleLocation.clone().add(x, 0, z), 1,
                            new DustOptions(color, 3));
                }

                if (!displaying)
                    cancel();
            }
        }.runTaskTimer(main, 0, 25);

    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public int getAmmo() {
        return currentAmmo;
    }

    public void setAmmo(int ammo) {

        this.currentAmmo = ammo;

        if (maxAmmo > 0 && this.currentAmmo > maxAmmo)
            this.currentAmmo = maxAmmo;

        updateItems();

    }

    public void setDisplay(String display) {
        this.display = display;
        save();
    }

    public String getName() {
        return name;
    }

    public ItemStack getTurret() {

        ItemStack turretItem = turret.clone();
        ItemMeta itemm = turretItem.getItemMeta();

        itemm.setDisplayName(ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', display));
        itemm.getPersistentDataContainer().set(main.getKeys().turretItem, PersistentDataType.STRING, name);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Damage: " + projectileDamage);
        lore.add(ChatColor.DARK_GRAY + "Range: " + towerRange);
        lore.add(ChatColor.GRAY + "Bullets Per Shot: " + bulletsPerShot);
        lore.add(ChatColor.DARK_GRAY + "Shot Consumption: " + shotConsumption);
        lore.add(ChatColor.GRAY + "Pierce: " + pierceLevel);
        lore.add(ChatColor.DARK_GRAY + "Knockback: " + knockback);
        lore.add(ChatColor.GRAY + "Accuracy: " + towerAccuracy);
        lore.add(ChatColor.DARK_GRAY + "Bullet Speed: " + projectileSpeed);
        lore.add(ChatColor.GRAY + "Rate of Fire: " + towerDelay);
        if (maxAmmo > 0)
            lore.add(ChatColor.DARK_GRAY + "Max Ammo: " + maxAmmo);

        itemm.setLore(lore);

        turretItem.setItemMeta(itemm);

        return turretItem;

    }

    public void setTurret(ItemStack item) {

        turret = item;

        save();

        main.updateExistingTowers(name);

    }

    public ItemStack getBase() {
        return base;
    }

    public void setBase(ItemStack base) {
        this.base = base;

        save();

        main.updateExistingTowers(name);

    }

    public double getProjectileDamage() {
        return projectileDamage;
    }

    public void setProjectileDamage(double projectileDamage) {
        this.projectileDamage = projectileDamage;
        save();
        main.updateExistingTowers(name);
    }

    public double getCritChance() {
        return critChance;
    }

    public double getCritMultiplier() {
        return critMultiplier;
    }

    public double getCritAccuracy() {
        return critAccuracy;
    }

    public void restart() {

        Bukkit.getScheduler().cancelTask(taskID);
        stand.remove();
        baseStand.remove();
        main.removeTower(stand);
        loadFile();
        startStand();

    }

    public void remove() {
        remove(true);
    }

    public void remove(boolean drop) {

        Bukkit.getScheduler().cancelTask(taskID);
        main.removeTower(stand);
        if (drop)
            location.getWorld().dropItemNaturally(stand.getEyeLocation(), getTurret());

        ItemStack arrows = new ItemStack(Material.ARROW);

        int amount;

        while (currentAmmo > 0) {
            amount = currentAmmo > 64 ? 64 : currentAmmo;
            arrows.setAmount(amount);
            currentAmmo -= amount;
            if (drop)
                location.getWorld().dropItemNaturally(stand.getEyeLocation(), arrows);
        }

        stand.remove();
        baseStand.remove();

    }

    private void loadFile() {

        yaml = YamlConfiguration.loadConfiguration(file);

        name = file.getName().replace(".yml", "");

        showDisplay = yaml.getBoolean(ConfigDefaults.DISPLAY_SHOW.key);
        display = yaml.getString(ConfigDefaults.DISPLAY_DISPLAY_NAME.key);

        bulletsPerShot = yaml.getInt(ConfigDefaults.PROJECTILE_PER_SHOT.key);
        projectileGap = yaml.getLong(ConfigDefaults.PROJECTILE_GAP.key);
        bulletGravity = yaml.getBoolean(ConfigDefaults.PROJECTILE_GRAVITY.key);
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
        turret = yaml.getItemStack(ConfigDefaults.TOWER_TURRET.key);
        base = yaml.getItemStack(ConfigDefaults.TOWER_BASE.key);
        towerOffset = yaml.getDouble(ConfigDefaults.TOWER_OFFSET.key);

        towerRange = yaml.getDouble(ConfigDefaults.RANGE_TARGET.key);
        arrowPickupRange = yaml.getDouble(ConfigDefaults.RANGE_PICKUP_AMMUNITION.key);

        silentTower = yaml.getBoolean(ConfigDefaults.SILENT_TOWER.key);
        silentArrows = yaml.getBoolean(ConfigDefaults.SILENT_PROJECTILE.key);

        blacklist.clear();
        potionEffects.clear();

        for (String type : yaml.getStringList(ConfigDefaults.BLACKLIST.key)) {
            try {
                blacklist.add(EntityType.valueOf(type));
            } catch (IllegalArgumentException ex) {
                main.getLogger().log(Level.WARNING,
                        DefenceTowersMain.prefix + type + " is not a valid entity type while loading " + name);
            }
        }

        for (String type : yaml.getStringList(ConfigDefaults.WHITELIST.key)) {
            try {
                whitelist.add(EntityType.valueOf(type));
            } catch (IllegalArgumentException ex) {
                main.getLogger().log(Level.WARNING,
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
                    main.getLogger().log(Level.WARNING, "Unknown potion effect type: " + effectType);
                }

            }
        } catch (NullPointerException ex) {
        }

        if (towerDelay == 0)
            towerDelay = 1;

    }

    private void save() {

        yaml.set(ConfigDefaults.DISPLAY_SHOW.key, showDisplay);
        yaml.set(ConfigDefaults.DISPLAY_DISPLAY_NAME.key, display);

        yaml.set(ConfigDefaults.PROJECTILE_PER_SHOT.key, bulletsPerShot);
        yaml.set(ConfigDefaults.PROJECTILE_GAP.key, projectileGap);
        yaml.set(ConfigDefaults.PROJECTILE_GRAVITY.key, bulletGravity);
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
        yaml.set(ConfigDefaults.RANGE_PICKUP_AMMUNITION.key, arrowPickupRange);

        yaml.set(ConfigDefaults.SILENT_TOWER.key, silentTower);
        yaml.set(ConfigDefaults.SILENT_PROJECTILE.key, silentArrows);

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
        yaml.set(ConfigDefaults.TOWER_AMMUNITION_ITEM.key, ammunitionItem);
        yaml.set(ConfigDefaults.TOWER_TURRET.key, turret);
        yaml.set(ConfigDefaults.TOWER_BASE.key, base);

        List<String> blacklistNames = new ArrayList<>(), whitelistNames = new ArrayList<>();

        blacklist.forEach(type -> blacklistNames.add(type.toString()));
        whitelist.forEach(type -> whitelistNames.add(type.toString()));

        yaml.set(ConfigDefaults.BLACKLIST.key, blacklistNames);
        yaml.set(ConfigDefaults.WHITELIST.key, whitelistNames);

        try {
            yaml.save(file);
            if (!file.exists())
                file.createNewFile();
        } catch (IOException ex) {
        }

    }

    public void blacklistPlayer(OfflinePlayer player) {
        blacklistedPlayers.add(player.getUniqueId());
    }

    public List<UUID> getBlacklistedPlayers() {
        return blacklistedPlayers;
    }

    private Vector noRiderOperation() throws Exception {
        List<Entity> nearbyEntities = stand.getNearbyEntities(towerRange, towerRange, towerRange);

        if (nearbyEntities.size() == 0)
            throw new Exception("No entities nearby");

        Entity target = null;
        Vector direction = null;

        double distance = 0;

        for (Entity entity : nearbyEntities) {

            if (!stand.hasLineOfSight(entity)) continue;

            if (entity instanceof Player) {
                if (blacklistedPlayers.contains(entity.getUniqueId())) continue;
                if (((Player) entity).getGameMode() != GameMode.SURVIVAL) continue;
            }

            if (whitelist.size() != 0) {
                if (!whitelist.contains(entity.getType()))
                    continue;
            }
            if (blacklist.contains(entity.getType())) {
                continue;
            }
            if (entity.isDead())
                continue;

            if (target == null) target = entity;

            switch(targetType) { // Handles different target types
                case CLOSEST:
                    target = (location.distance(entity.getLocation()) >= location.distance(target.getLocation()) ? target : entity);
                    break;
                case FARTHEST:
                    target = (location.distance(entity.getLocation()) <= location.distance(target.getLocation()) ? target : entity);
                    break;
                case MOST_HEALTH:
                    target = ((LivingEntity) entity).getHealth() > ((LivingEntity) target).getHealth() ? entity : target;
                    break;
                case LEAST_HEALTH:
                    target = ((LivingEntity) entity).getHealth() < ((LivingEntity) target).getHealth() ? entity : target;
                    break;
            }

            Location targetLocation = target.getLocation();

            if (target instanceof Ageable) {
                if (!((Ageable) target).isAdult())
                    targetLocation.subtract(0, 1, 0);
            }

            distance = targetLocation.distance(location);

            if (distance > towerRange)
                throw new Exception("No entities nearby"); // target to far, without this, the turret will shoot out of
            // the range particles in the "corners"

            if (target == null) throw new Exception("No entities nearby");

            direction = targetLocation.clone().add(0, bulletGravity ? (distance / 8) - (projectileSpeed / 2) : 0, 0).subtract(location).toVector();

        }

        if (direction == null) throw new Exception("No entities nearby");

        return direction;

    }

    public void setPerShot(int perShot) {
        this.bulletsPerShot = perShot;
    }

    public void setProjectileMaterial(Material material) {
        projectileMaterial = material;
    }

    public void setGap(int gap) {
        this.projectileGap = gap;
    }

    public void setSpeed(float speed) {
        projectileSpeed = speed;
    }

    public void setTowerAccuracy(float accuracy) {
        this.towerAccuracy = accuracy;
    }

    public void setVisualFire(boolean visualFire) {
        this.visualFire = visualFire;
    }

    public void setFireTicks(int ticks) {
        fireTicks = ticks;
    }

    public void setTowerDelay(int delay) {
        this.towerDelay = delay;
    }

    public void setRange(double range) {
        this.towerRange = range;
    }

    public void color(boolean color) {
        tailToggle = color;
    }

    public void setColor(Color color) {
        tailRed = color.getRed();
        tailGreen = color.getGreen();
        tailBlue = color.getBlue();
    }

    public void setBounces(int bounces) {
        this.bounces = bounces;
    }

    public void setPierceLevel(int level) {
        pierceLevel = level;
    }

    public void setKnockback(int level) {
        knockback = level;
    }

    public void whitelist(EntityType type) {
        if (whitelist.contains(type)) return;
        whitelist.add(type);
    }

    public void blacklist(EntityType type) {
        if (blacklist.contains(type)) return;
        blacklist.add(type);
    }

    public void clearBlacklist() {
        blacklist.clear();
    }

    public void clearWhitelist() {
        whitelist.clear();
    }

    public void setCritChance(double chance) {
        critChance = chance;
    }

    public void setCritMultiplier(double multiplier) {
        critMultiplier = multiplier;
    }

    public void setCritAccuracy(double accuracy) {
        critAccuracy = accuracy;
    }

    public void clearPotionEffects() {
        potionEffects.clear();
    }

    public void setColorSize(float size) {
        tailSize = size;
    }

    public ItemStack getAmmunitionItem() {
        return ammunitionItem;
    }

    public void setAmmunitionItem(ItemStack item) {
        ammunitionItem = item;
        save();
        main.updateExistingTowers(name);
    }

}
