package me.isaac.defencetowers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    private final List<EntityType> blacklist = new ArrayList<>();
    private final List<EntityType> whitelist = new ArrayList<>();
    private final List<UUID> blacklistedPlayers = new ArrayList<>();
    private final List<PotionEffect> potionEffects = new ArrayList<>();
    private final Inventory inventory;
    DefenceTowersMain main;
    String display;
    File file;
    YamlConfiguration yaml;

    // Add targettypes; CLOSEST, FURTHEST, MOST HEALTH, LEAST HEALTH
    // Add turrent pitch/yaw limits
    Location location;
    Vector direction;
    Player operator = null;
    int taskID;
    double critChance = .3, critMultiplier = 2, critAccuracy = .5;
    boolean color = false;
    int red = 255, green = 0, blue = 255;
    boolean silentTower = false, silentArrows = false;
    ArmorStand stand = null, baseStand = null;
    boolean displaying = false;
    private String name;
    private boolean hasTicked = false;
    private ItemStack turret = getHeadFromValue(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2MxMWEwZDkwYzM3ZWI2OTVjOGE1MjNkODYwMWFhMWM4NWZhZDA5YTRkMjIzMmQwNGVkMjNhYzkwZTQzMjVjMiJ9fX0=");
    private ItemStack base = new ItemStack(Material.BEDROCK);
    private boolean showDisplay = true, fire = false, bulletGravity = true, canShoot = false;
    private int bulletsPerShot = 1, shotConsumption = 1, pierceLevel = 0, knockback = 0, fireTicks = 20, ammo = 0,
            maxAmmo = 0;
    private double range = 10, damage = 2;
    private float accuracy = 10f, speed = 1.5f;
    private long shotDelay = 20, bulletGap = 1, delay = 0;
    private double towerOffset = .55;

    public Tower(DefenceTowersMain main, String name, Location location) {
        this.main = main;
        this.name = name;
        display = name;
        file = new File("plugins//DefenceTowers//Towers//" + name + ".yml");
        yaml = YamlConfiguration.loadConfiguration(file);

        inventory = Bukkit.createInventory(null, InventoryType.HOPPER,
                ChatColor.translateAlternateColorCodes('&', display));

        if (file.exists()) {
            loadFile();
        }

        if (location != null) {
            this.location = location;
            startStand();
        }

        if (file.exists())
            return;

        defaultEntityTypes();

        save();

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

    @SuppressWarnings("deprecation")
    private ItemStack getHeadFromValue(String value) {
        UUID id = UUID.nameUUIDFromBytes(value.getBytes());
        int less = (int) id.getLeastSignificantBits();
        int most = (int) id.getMostSignificantBits();
        return Bukkit.getUnsafe().modifyItemStack(new ItemStack(Material.PLAYER_HEAD),
                "{SkullOwner:{Id:[I;" + (less * most) + "," + (less >> 23) + "," + (most / less) + "," + (most * 8731)
                        + "],Properties:{textures:[{Value:\"" + value + "\"}]}}}");
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

        main.towerLocations.put(stand, this);

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
            public void run() {

                if (!stand.isValid()) {
                    main.towerLocations.remove(stand);
                    displaying = false;
                    Bukkit.getScheduler().cancelTask(taskID);
                }

                if (operator == null) {
                    boolean target = true;
                    try {
                        direction = noRiderOperation();
                    } catch (Exception e) {
                        target = false;
                    }

                    if (canShoot)
                        if (target) shoot(direction);
                } else
                    direction = operator.getLocation().getDirection();

                delay++;

                if (operator != null) displayShootCooldown();

                canShoot = delay >= shotDelay;

                setHeadDirection(direction);

                if (displaying) {
                    aimingDraw(direction);

                    boolean containsPlayer = false;

                    for (Entity entity : stand.getNearbyEntities(range * 1.5, range * 1.5, range * 1.5)) {
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

        float progress = (float) delay / shotDelay;

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
                TextComponent.fromLegacyText(ChatColor.WHITE + StaticUtil.format.format(ammo) + " " + ChatColor.AQUA + cooldownBar));

    }

    public void setHeadDirection(Vector direction) {

        Location clone = stand.getLocation().clone();
        clone.setDirection(direction);
        stand.setHeadPose(new EulerAngle(Math.toRadians(clone.getPitch()), Math.toRadians(clone.getYaw()), 0));

    }

    public void aimingDraw(Vector direction) {
        Location particleLocation = location.clone().add(0, 2, 0);

        double particleOffset = accuracy / 100;

        while (true) {
            location.getWorld().spawnParticle(Particle.WAX_OFF, particleLocation, 1, particleOffset, particleOffset,
                    particleOffset, 0);
            particleLocation.add(direction.normalize().multiply(.4));
            if (particleLocation.distance(location) > range)
                return;
        }

    }

    public void shoot(Vector direction) {

        if (!canShoot)
            return;
        if (ammo < shotConsumption) {
            if (!silentTower && !hasTicked) {
                stand.getWorld().playSound(stand.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
                hasTicked = true;
            }
            return;
        }

        hasTicked = false;

        delay = 0;
        setAmmo(ammo - shotConsumption);

        for (int i = 0; i < bulletsPerShot; i++) {

            new BukkitRunnable() {
                public void run() {
                    Arrow arrow = stand.getWorld().spawnArrow(stand.getEyeLocation(), direction, speed, accuracy);

                    arrow.setBounce(false);
                    arrow.setDamage(damage);
                    arrow.setPickupStatus(PickupStatus.DISALLOWED);
                    arrow.setPierceLevel(pierceLevel);
                    arrow.setKnockbackStrength(knockback);
                    arrow.setVisualFire(fire);
                    arrow.setSilent(silentArrows);
                    arrow.setGravity(bulletGravity);
                    if (fire)
                        arrow.setFireTicks(fireTicks);

                    if (color)
                        arrow.setColor(Color.fromRGB(red, green, blue));

                    arrow.getPersistentDataContainer().set(main.getKeys().bullet, PersistentDataType.STRING, name);

                    double tempDamage = damage;

                    if (Math.random() <= getCritChance()) {
                        arrow.setCritical(true);
                        tempDamage = tempDamage * ((Math.random() >= .5 ? 1 : -1)
                                * (Math.random() * getCritAccuracy() - getCritAccuracy()) + getCritMultiplier());
                    }

                    arrow.getPersistentDataContainer().set(main.getKeys().bulletDamage, PersistentDataType.DOUBLE,
                            tempDamage);

                }
            }.runTaskLater(main, i * bulletGap);

        }

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

        ItemStack ammunitionItem = main.towerItems.getAmmunition().clone();
        ItemMeta ammunitionMeta = ammunitionItem.getItemMeta();
        List<String> lore = ammunitionMeta.getLore();
        lore.set(0, ChatColor.GRAY + "Ammunition: " + ChatColor.GOLD + StaticUtil.format.format(ammo));
        ammunitionMeta.setLore(lore);
        ammunitionItem.setItemMeta(ammunitionMeta);
        ammunitionItem.setAmount(ammo < 1 ? 1 : (ammo > 64 ? 64 : ammo));

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

        return inventory;

    }

    public boolean isDisplaying() {
        return displaying;
    }

    public void displayRange(boolean display) {

        displaying = display;

        if (!display)
            return;

        new BukkitRunnable() {
            final int points = (int) (range * 5);

            public void run() {

                Location particleLocation = location.clone().add(0, 2.7, 0);

                Color color = Color.fromRGB(ThreadLocalRandom.current().nextInt(256),
                        ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256));

                for (double i = 0; i < points; i += .1) {

                    double x = range * Math.sin(i);
                    double z = range * Math.cos(i);

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
        return ammo;
    }

    public void setAmmo(int ammo) {

        this.ammo = ammo;

        if (maxAmmo > 0 && this.ammo > maxAmmo)
            this.ammo = maxAmmo;

        ItemStack ammunitionItem = main.towerItems.getAmmunition().clone();
        ItemMeta ammunitionMeta = ammunitionItem.getItemMeta();
        List<String> lore = ammunitionMeta.getLore();
        lore.set(0, ChatColor.GRAY + "Ammunition: " + ChatColor.GOLD + StaticUtil.format.format(ammo));
        ammunitionMeta.setLore(lore);
        ammunitionItem.setItemMeta(ammunitionMeta);
        ammunitionItem.setAmount(ammo < 1 ? 1 : (ammo > 64 ? 64 : ammo));

        inventory.setItem(3, ammunitionItem);

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
        lore.add(ChatColor.GRAY + "Damage: " + damage);
        lore.add(ChatColor.DARK_GRAY + "Range: " + range);
        lore.add(ChatColor.GRAY + "Bullets Per Shot: " + bulletsPerShot);
        lore.add(ChatColor.DARK_GRAY + "Shot Consumption: " + shotConsumption);
        lore.add(ChatColor.GRAY + "Pierce: " + pierceLevel);
        lore.add(ChatColor.DARK_GRAY + "Knockback: " + knockback);
        lore.add(ChatColor.GRAY + "Accuracy: " + accuracy);
        lore.add(ChatColor.DARK_GRAY + "Bullet Speed: " + speed);
        lore.add(ChatColor.GRAY + "Rate of Fire: " + shotDelay);
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

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
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
        main.towerLocations.remove(stand, this);

        loadFile();
        startStand();

    }

    public void remove() {
        remove(true);
    }

    public void remove(boolean drop) {

        Bukkit.getScheduler().cancelTask(taskID);
        main.towerLocations.remove(stand, this);

        if (drop)
            location.getWorld().dropItemNaturally(stand.getEyeLocation(), getTurret());

        ItemStack arrows = new ItemStack(Material.ARROW);

        int amount;

        while (ammo > 0) {
            amount = ammo > 64 ? 64 : ammo;
            arrows.setAmount(amount);
            ammo -= amount;
            if (drop)
                location.getWorld().dropItemNaturally(stand.getEyeLocation(), arrows);
        }

        stand.remove();
        baseStand.remove();

    }

    private void loadFile() {

        yaml = YamlConfiguration.loadConfiguration(file);

        name = file.getName().replace(".yml", "");

        showDisplay = yaml.getBoolean("Display.Show");
        display = yaml.getString("Display.Display");

        bulletsPerShot = yaml.getInt("Bullets.Per Shot");
        bulletGap = yaml.getLong("Bullets.Gap");
        bulletGravity = yaml.getBoolean("Bullets.Gravity");
        damage = yaml.getDouble("Bullets.Damage");
        speed = yaml.getInt("Bullets.Speed");
        accuracy = yaml.getInt("Bullets.Accuracy");
        pierceLevel = yaml.getInt("Bullets.Piercing");
        knockback = yaml.getInt("Bullets.Knockback");
        fire = yaml.getBoolean("Bullets.Fire.Fire");
        fireTicks = yaml.getInt("Bullets.Fire.Ticks");
        range = yaml.getDouble("Range");
        maxAmmo = yaml.getInt("Shot.Max Ammo");

        critChance = yaml.getDouble("Critical.Chance");
        critMultiplier = yaml.getDouble("Critical.Multiplier");
        critAccuracy = yaml.getDouble("Critical.Accuracy");

        shotConsumption = yaml.getInt("Shot.Consumption");
        shotDelay = yaml.getLong("Shot.Delay");

        silentTower = yaml.getBoolean("Silent.Tower");
        silentArrows = yaml.getBoolean("Silent.Arrows");

        color = yaml.getBoolean("Color.Enable");
        red = yaml.getInt("Color.Red");
        green = yaml.getInt("Color.Green");
        blue = yaml.getInt("Color.Blue");

        turret = yaml.getItemStack("Tower.Turret");
        base = yaml.getItemStack("Tower.Base");
        towerOffset = yaml.getDouble("Tower.Offset");

        if (blacklist.size() != 0)
            blacklist.clear();
        if (potionEffects.size() != 0)
            potionEffects.clear();

        for (String type : yaml.getStringList("Blacklist")) {
            try {
                blacklist.add(EntityType.valueOf(type));
            } catch (IllegalArgumentException ex) {
                main.getLogger().log(Level.WARNING,
                        DefenceTowersMain.prefix + type + " is not a valid entity type while loading " + name);
            }
        }

        for (String type : yaml.getStringList("Whitelist")) {
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

                potionEffects.add(new PotionEffect(PotionEffectType.getByName(effectType), duration, amplifier, ambient,
                        hasParticles, hasIcon));

            }
        } catch (NullPointerException ex) {
        }

        if (shotDelay == 0)
            shotDelay = 1;

    }

    private void save() {

        yaml.set("Display.Show", showDisplay);
        yaml.set("Display.Display", display);

        yaml.set("Bullets.Per Shot", bulletsPerShot);
        yaml.set("Bullets.Gap", bulletGap);
        yaml.set("Bullets.Gravity", bulletGravity);
        yaml.set("Bullets.Damage", damage);
        yaml.set("Bullets.Speed", speed);
        yaml.set("Bullets.Accuracy", accuracy);
        yaml.set("Bullets.Piercing", pierceLevel);
        yaml.set("Bullets.Knockback", knockback);
        yaml.set("Bullets.Fire.Fire", fire);
        yaml.set("Bullets.Fire.Ticks", fireTicks);

        yaml.set("Critical.Chance", critChance);
        yaml.set("Critical.Multiplier", critMultiplier);
        yaml.set("Critical.Accuracy", critAccuracy);

        yaml.set("Shot.Consumption", shotConsumption);
        yaml.set("Shot.Delay", shotDelay);
        yaml.set("Shot.Max Ammo", maxAmmo);

        yaml.set("Range", range);

        yaml.set("Silent.Tower", silentTower);
        yaml.set("Silent.Arrows", silentArrows);

        yaml.set("Color.Enable", color);
        yaml.set("Color.Red", red);
        yaml.set("Color.Green", green);
        yaml.set("Color.Blue", blue);

        for (PotionEffect effects : potionEffects) {

            yaml.set("Potion Effects." + effects.getType().getName() + ".Amplifier", effects.getAmplifier());
            yaml.set("Potion Effects." + effects.getType().getName() + ".Duration", effects.getDuration());
            yaml.set("Potion Effects." + effects.getType().getName() + ".Is Ambient", effects.isAmbient());
            yaml.set("Potion Effects." + effects.getType().getName() + ".Has Particles", effects.hasParticles());
            yaml.set("Potion Effects." + effects.getType().getName() + ".Has Icon", effects.hasIcon());

        }

        List<String> typeNames = new ArrayList<>();
        blacklist.forEach(type -> typeNames.add(type.toString()));
        yaml.set("Blacklist", typeNames);

        if (whitelist.size() != 0) {
            typeNames.clear();
            whitelist.forEach(type -> typeNames.add(type.toString()));
            yaml.set("Whitelist", typeNames);
        }

        yaml.set("Tower.Turret", turret);
        yaml.set("Tower.Base", base);
        yaml.set("Tower.Offset", towerOffset);

        try {
            yaml.save(file);
            if (!file.exists())
                file.createNewFile();
        } catch (IOException ex) {
        }

    }

    // Default blocked entity types
    private void defaultEntityTypes() {
        blacklist.add(EntityType.AREA_EFFECT_CLOUD);
        blacklist.add(EntityType.ARMOR_STAND);
        blacklist.add(EntityType.ARROW);
        blacklist.add(EntityType.DRAGON_FIREBALL);
        blacklist.add(EntityType.DROPPED_ITEM);
        blacklist.add(EntityType.EGG);
        blacklist.add(EntityType.ENDER_CRYSTAL);
        blacklist.add(EntityType.ENDER_PEARL);
        blacklist.add(EntityType.ENDER_SIGNAL);
        blacklist.add(EntityType.EVOKER_FANGS);
        blacklist.add(EntityType.EXPERIENCE_ORB);
        blacklist.add(EntityType.FALLING_BLOCK);
        blacklist.add(EntityType.FIREBALL);
        blacklist.add(EntityType.FIREWORK);
        blacklist.add(EntityType.FISHING_HOOK);
        blacklist.add(EntityType.GLOW_ITEM_FRAME);
        blacklist.add(EntityType.ITEM_FRAME);
        blacklist.add(EntityType.LEASH_HITCH);
        blacklist.add(EntityType.LIGHTNING);
        blacklist.add(EntityType.LLAMA_SPIT);
        blacklist.add(EntityType.MARKER);
        blacklist.add(EntityType.MINECART);
        blacklist.add(EntityType.MINECART_CHEST);
        blacklist.add(EntityType.MINECART_COMMAND);
        blacklist.add(EntityType.MINECART_FURNACE);
        blacklist.add(EntityType.MINECART_HOPPER);
        blacklist.add(EntityType.MINECART_MOB_SPAWNER);
        blacklist.add(EntityType.MINECART_TNT);
        blacklist.add(EntityType.PAINTING);
//		blacklist.add(EntityType.PLAYER); Players can now be blacklisted
        blacklist.add(EntityType.PRIMED_TNT);
        blacklist.add(EntityType.SNOWBALL);
        blacklist.add(EntityType.SHULKER_BULLET);
        blacklist.add(EntityType.SPECTRAL_ARROW);
        blacklist.add(EntityType.SPLASH_POTION);
        blacklist.add(EntityType.THROWN_EXP_BOTTLE);
        blacklist.add(EntityType.TRIDENT);
        blacklist.add(EntityType.UNKNOWN);
        blacklist.add(EntityType.WITHER_SKULL);
    }

    public void blacklistPlayer(OfflinePlayer player) {
        blacklistedPlayers.add(player.getUniqueId());
    }

    public List<UUID> getBlacklistedPlayers() {
        return blacklistedPlayers;
    }

    private Vector noRiderOperation() throws Exception {
        List<Entity> nearbyEntities = stand.getNearbyEntities(range, range, range);

        if (nearbyEntities.size() == 0)
            throw new Exception("No entities nearby");

        Entity target = null;
        Vector direction = null;

        double distance = 0;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player)
                if (blacklistedPlayers.contains(entity.getUniqueId()))
                    continue;
            if (whitelist.size() != 0)
                if (!whitelist.contains(entity.getType()))
                    continue;
            if (blacklist.contains(entity.getType()))
                continue;
            if (entity.isDead())
                continue;

            target = (target == null ? entity
                    : location.distance(entity.getLocation()) >= location.distance(target.getLocation()) ? target
                    : entity);

            distance = target.getLocation().distance(location);

            if (distance > range)
                throw new Exception("No entities nearby"); // target to far, without this, the turret will shoot out of
            // the range particles in the "corners"

            Location targetLocation = target.getLocation();

            if (target instanceof Ageable) {
                if (!((Ageable) target).isAdult())
                    targetLocation.subtract(0, 1, 0);
            }

            direction = targetLocation.clone().add(0, distance / 10, 0).subtract(stand.getLocation()).toVector();

            if (!stand.hasLineOfSight(target))
                target = null;

        }

        if (target == null)
            throw new Exception("No entities nearby");

        return direction;

    }

}
