package me.isaac.defencetowers.tower;

import me.isaac.defencetowers.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Tower {

    private final Inventory inventory;
    private final DefenceTowersMain main;
    private final List<UUID> blacklistedPlayers = new ArrayList<>();
    public List<Projectile> towersActiveProjectileList = new ArrayList<>();
    private Tower towerInstance;
    private String name;
    private Location location, turretBarrelLocation;
    private TowerOptions towerOptions;
    private Vector direction;
    private Player operator = null;
    private ArmorStand turretStand = null, baseStand = null, nameStand = null;
    private List<Slime> hitBoxes = new ArrayList<>();
    private List<Entity> entities = new ArrayList<>();
    private int soundTick = 0;
    private boolean hitBoxValid, displaying = false, canShoot = false;
    private int currentAmmo = 0;
    private long delay = 0, regenDelay = 0;
    private double health;

    //TODO Make targeting system when players are controlling towers, get nearby entity player is looking at.
    //TODO Make turrets have a rotation speed.
    private TargetType targetType = TargetType.CLOSEST;
    private double criticalMultiplier = 1;
    private List<Entity> nearbyEntities = new ArrayList<>(), nearbyEntitiesExtended;

    public Tower(DefenceTowersMain main, String name, Location location, boolean create) {
        towerInstance = this;
        this.main = main;
        this.name = name;
        towerOptions = new TowerOptions(main, name, create); // TowerOptions handles saving, loading, and editing options for towers.
        health = towerOptions.getMaxHealth();

        inventory = Bukkit.createInventory(null, InventoryType.HOPPER,
                ChatColor.translateAlternateColorCodes('&', towerOptions.getDisplay()));

        if (location != null) {
            this.location = location;
            startStand();
        }

    }

    public static boolean exists(String name) {
        return new File("plugins//DefenceTowers//Towers//" + name + ".yml").exists();
    }

    public ArmorStand getBaseStand() {
        return baseStand;
    }

    public ArmorStand getTurretStand() {
        return turretStand;
    }

    public void kickOperator() {
        if (operator == null)
            return;
        operator.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
        operator = null;
        if (turretStand.getPassengers().contains(operator))
            turretStand.removePassenger(operator);
    }

    public Player getOperator() {
        return operator;
    }

    public void setOperator(Player player) {
        operator = player;
        hitBoxes.get(1).addPassenger(player);
    }

    public boolean getCanShoot() {
        return canShoot;
    }

    public void setCanShoot(boolean canShoot) {
        this.canShoot = canShoot;
    }

    private void setupHitbox(Location location) {

        Slime bottom = ((RegionAccessor) location.getWorld()).spawn(location.add(0, 1.4, 0), Slime.class, slime -> {
            slime.setSize(1);
            slime.setAI(false);
            slime.setSilent(true);
            slime.setGravity(false);
            slime.setCollidable(true);
            slime.getPersistentDataContainer().set(main.getKeys().turretStand, PersistentDataType.STRING, name);
            slime.setInvisible(true);
        });

        Slime top = ((RegionAccessor) location.getWorld()).spawn(location.add(0, towerOptions.getTowerOffset(), 0), Slime.class, slime -> {
            slime.setSize(1);
            slime.setAI(false);
            slime.setSilent(true);
            slime.setGravity(false);
            slime.setCollidable(true);
            slime.getPersistentDataContainer().set(main.getKeys().turretStand, PersistentDataType.STRING, name);
            slime.setInvisible(true);
        });

        hitBoxes.add(top);
        hitBoxes.add(bottom);

        entities.addAll(hitBoxes);

        hitBoxValid = true;

    }

    public void startStand() {

        baseStand = ((RegionAccessor) location.getWorld()).spawn(location, ArmorStand.class, baseStand -> {
            baseStand.setGravity(false);
            baseStand.getEquipment().setHelmet(towerOptions.getBaseItem());
            baseStand.setMarker(true);
            baseStand.setInvisible(true);
        });

        turretStand = ((RegionAccessor) location.getWorld()).spawn(location.clone().add(0, towerOptions.getTowerOffset(), 0), ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.getEquipment().setHelmet(towerOptions.getTurretItem());
            stand.setInvulnerable(true);
            stand.setMarker(true);
            stand.setVisible(false);
        });

        if (towerOptions.shouldShowDisplay()) {
            nameStand = ((RegionAccessor) location.getWorld()).spawn(location.clone().add(0, towerOptions.getTowerOffset() + towerOptions.getNameOffset(), 0), ArmorStand.class, stand -> {
                stand.setGravity(false);
                stand.setMarker(true);
                stand.setInvisible(true);
                stand.setInvulnerable(true);
                stand.setVisible(false);
                stand.setCustomName(ChatColor.translateAlternateColorCodes('&', towerOptions.getDisplay()));
                stand.setCustomNameVisible(true);
            });

            entities.add(nameStand);
        }

        entities.add(turretStand);
        entities.add(baseStand);

        setupHitbox(baseStand.getEyeLocation());
        turretBarrelLocation = hitBoxes.get(1).getEyeLocation();
        direction = turretStand.getLocation().getDirection();

        main.addTower(this);

        new BukkitRunnable() {
            public void run() {

                for (Slime hitBox : hitBoxes) {
                    if (!hitBox.isValid()) {
                        hitBoxValid = false;
                        break;
                    }
                }

                if (!turretStand.isValid() || !baseStand.isValid() || !hitBoxValid || (towerOptions.isUsingHealth() && health <= 0)) {
                    displaying = false;
                    remove(false || (towerOptions.isUsingHealth() && health <= 0));
                    cancel();
                    return;
                }

                if (regenDelay >= towerOptions.getRegenDelay() && health < getMaxHealth()) {
                    regenDelay = 0;
                    addHealth(towerOptions.getRegenAmount());
                }

                if (currentAmmo < towerOptions.getMaxAmmo()) {
                    for (Entity entity : turretStand.getNearbyEntities(towerOptions.getAmmunitionPickupRange(), towerOptions.getAmmunitionPickupRange(), towerOptions.getAmmunitionPickupRange())) {
                        if (entity instanceof Item) {
                            Item item = (Item) entity;
                            if (item.getItemStack().getType() != Material.ARROW) continue;

                            int amount = item.getItemStack().getAmount();

                            if (towerOptions.getMaxAmmo() > 0 && currentAmmo + amount > towerOptions.getMaxAmmo()) {
                                amount -= towerOptions.getMaxAmmo() - currentAmmo;
                                setAmmo(towerOptions.getMaxAmmo());
                            } else {
                                setAmmo(currentAmmo + amount);
                                amount = 0;
                            }

                            item.getItemStack().setAmount(amount);

                        }
                    }
                }

                canShoot = delay >= towerOptions.getTowerDelay();

                if (operator == null) {
                    boolean target = true;
                    try {
                        direction = noRiderOperation();
                    } catch (Exception e) {
                        target = false;
                    }

                    if (canShoot) {
                        if (target) {
                            shoot(turretBarrelLocation, towerOptions.getProjectileType(), direction);
                        }
                    }

                } else
                    direction = operator.getLocation().getDirection();

                for (int i = 0; i < towersActiveProjectileList.size(); i++) {

                    Projectile projectile = towersActiveProjectileList.get(i);

                    try {
                        if (!projectile.isValid()) {
                            towersActiveProjectileList.remove(i);
                            continue;
                        }
                    } catch (Exception ex) {
                        return;
                    }

                    if (towerOptions.isTail())
                        projectile.getWorld().spawnParticle(Particle.REDSTONE, projectile.getLocation(), 1, new DustOptions(Color.fromRGB(towerOptions.getTailRed(), towerOptions.getTailGreen(), towerOptions.getTailBlue()), towerOptions.getTailSize()));

                }

                delay++;
                regenDelay++;
                soundTick++;

                if (operator != null) displayShootCooldown();

                setHeadDirection(direction);

                if (displaying) {
                    aimingDraw(direction);

                    boolean containsPlayer = false;

                    for (Entity entity : nearbyEntitiesExtended) {
                        if (entity instanceof Player) {
                            containsPlayer = true;
                            break;
                        }
                    }

                    if (!containsPlayer)
                        displaying = false;
                }

            }
        }.runTaskTimerAsynchronously(main, 0, 1);

    }

    public void displayShootCooldown() {
        if (operator == null) return;
        String cooldownBar = "";

        float progress = (float) delay / towerOptions.getTowerDelay();

        int i = 0;

        while (i < progress * 10 && i < 10) {
            cooldownBar += ChatColor.AQUA + "█";
            i++;
        }

        while (i < 10) {
            cooldownBar += ChatColor.DARK_GRAY + "█";
            i++;
        }

        BaseComponent[] barMessage = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', main.messagesYaml.getString(MessageDefault.TOWER_COOLDOWN_BAR.path).replace("%AMMO%", StaticUtil.format.format(currentAmmo)).replace("%BAR%", cooldownBar)));

        operator.spigot().sendMessage(ChatMessageType.ACTION_BAR, barMessage);

    }

    public void setHeadDirection(Vector direction) {
        Location clone = turretStand.getLocation().clone();
        try {
            clone.setDirection(direction);
        } catch (NullPointerException ignored) {
        }
        turretStand.setHeadPose(new EulerAngle(Math.toRadians(clone.getPitch()), Math.toRadians(clone.getYaw()), 0));

    }

    public void aimingDraw(Vector direction) {
        Location particleLocation = location.clone().add(0, 2, 0);

        double particleOffset = towerOptions.getTowerAccuracy() / 100;

        while (true) {
            location.getWorld().spawnParticle(Particle.WAX_OFF, particleLocation, 1, particleOffset, particleOffset,
                    particleOffset, 0);
            particleLocation.add(direction.normalize().multiply(.4));
            if (particleLocation.distance(location) > towerOptions.getTowerRange())
                return;
        }

    }

    public void addActiveProjectile(Projectile projectile) {
        towersActiveProjectileList.add(projectile);
    }

    public void shoot(Location location, ProjectileType type, Vector direction) {

        if (direction == null) throw new IllegalArgumentException("Direction cannot be null");

        if (!canShoot)
            return;
        if (currentAmmo < towerOptions.getShotConsumption()) {
            if (!towerOptions.isSilentTower() && soundTick >= 20) {
                turretStand.getWorld().playSound(turretStand.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, .7f, 1);
                soundTick = 0;
            }
            return;
        }

        delay = 0;
        setAmmo(currentAmmo - towerOptions.getShotConsumption());

        for (int i = 0; i < towerOptions.getProjectilesPerShot(); i++) {

            new BukkitRunnable() {

                Projectile projectile;

                public void run() {

                    projectile = freeProjectile(towerOptions.getProjectileType(), location, direction);

                    towersActiveProjectileList.add(projectile);

                }
            }.runTaskLater(main, i * towerOptions.getProjectileGap());

        }

    }

    public Projectile freeProjectile(ProjectileType type, Location location, Vector direction) {
        Projectile projectile = null;
        switch (type) {
            case ARROW:
                projectile = shootArrow(location, direction);
                break;
            case ITEM:
                projectile = shootItem(location, direction);
                break;
            case TRIDENT:
                projectile = shootTrident(location, direction);
                break;
            case LARGE_FIREBALL:
                projectile = shootFireball(location, direction, false);
                break;
            case SMALL_FIREBALL:
                projectile = shootFireball(location, direction, true);
                break;
            case WITHER_SKULL:
                projectile = shootWitherSkull(location, direction);
                break;
        }
        updateProjectile(projectile);
        return projectile;
    }

    private Projectile shootArrow(Location location, Vector direction) {
        Arrow arrow = location.getWorld().spawnArrow(location, direction, (float) towerOptions.getProjectileSpeed(), towerOptions.getTowerAccuracy());
        arrow.setRotation(location.getYaw(), location.getPitch());
        return arrow;
    }

    private Projectile shootWitherSkull(Location location, Vector direction) {
        Arrow arrow = location.getWorld().spawnArrow(location, direction, (float) towerOptions.getProjectileSpeed(), towerOptions.getTowerAccuracy());
        Vector velocity = arrow.getVelocity();
        arrow.remove();

        WitherSkull skull = ((RegionAccessor) location.getWorld()).spawn(location, WitherSkull.class, t -> {
            t.setCharged(false);
            t.setDirection(velocity);
            t.setGravity(towerOptions.projectileHasGravity());
            t.setRotation(turretStand.getLocation().getYaw(), turretStand.getLocation().getPitch());
        });

        return skull;

    }

    private Projectile shootTrident(Location location, Vector direction) {
        Arrow arrow = location.getWorld().spawnArrow(location, direction, (float) towerOptions.getProjectileSpeed(), towerOptions.getTowerAccuracy());
        Vector velocity = arrow.getVelocity();
        arrow.remove();

        Trident trident = ((RegionAccessor) location.getWorld()).spawn(location, Trident.class, t -> {
            t.setVelocity(velocity);
            t.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            t.setPierceLevel(towerOptions.getProjectilesPerShot());
            t.setKnockbackStrength(towerOptions.getKnockback());
            t.setRotation(turretStand.getLocation().getYaw(), turretStand.getLocation().getPitch());
        });


        return trident;
    }

    private Projectile shootItem(Location location, Vector direction) {
        Arrow arrow = location.getWorld().spawnArrow(location, direction, (float) towerOptions.getProjectileSpeed(), towerOptions.getTowerAccuracy());
        Vector velocity = arrow.getVelocity();
        arrow.remove();

        Snowball snowball = ((RegionAccessor) location.getWorld()).spawn(location, Snowball.class, (t) -> {
            t.setVelocity(velocity);
            t.setItem(new ItemStack(towerOptions.getProjectileMaterial()));
        });

        return snowball;
    }

    private Projectile shootFireball(Location location, Vector direction, boolean small) {
        Arrow arrow = turretStand.getWorld().spawnArrow(location, direction, (float) towerOptions.getProjectileSpeed(), towerOptions.getTowerAccuracy());
        Vector velocity = arrow.getVelocity();
        arrow.remove();

        Fireball fireball;

        if (small) {
            fireball = ((RegionAccessor) location.getWorld()).spawn(location, SmallFireball.class, t -> {
                t.setDirection(velocity);
                t.setVelocity(velocity);
            });
        } else {
            fireball = ((RegionAccessor) location.getWorld()).spawn(location, Fireball.class, t -> {
                t.setDirection(velocity);
                t.setVelocity(velocity);
            });
        }

        return fireball;
    }

    private void updateProjectile(Projectile projectile) {

        PersistentDataContainer pdc = projectile.getPersistentDataContainer();

        double tempDamage = towerOptions.getProjectileDamage();

        if (Math.random() <= towerOptions.getCritChance()) ;
        criticalMultiplier = (Math.random() >= .5 ? 1 : -1) * (Math.random() * towerOptions.getCritAccuracy() - towerOptions.getCritAccuracy()) + towerOptions.getCritMultiplier();

        pdc.set(main.getKeys().critical, PersistentDataType.DOUBLE, criticalMultiplier);

        pdc.set(main.getKeys().bullet, PersistentDataType.STRING, name);
        pdc.set(main.getKeys().bulletDamage, PersistentDataType.DOUBLE, towerOptions.getProjectileDamage());
        pdc.set(main.getKeys().bounces, PersistentDataType.INTEGER, towerOptions.getBounces());
        pdc.set(main.getKeys().splits, PersistentDataType.INTEGER, towerOptions.getSplits());
        pdc.set(main.getKeys().fire, PersistentDataType.INTEGER, towerOptions.getFireTicks());
        pdc.set(main.getKeys().tail, PersistentDataType.STRING, towerOptions.isTail() + " " + towerOptions.getTailRed() + " " + towerOptions.getTailGreen() + " " + towerOptions.getTailBlue());

        if (projectile instanceof Arrow) {
            ((Arrow) projectile).setPierceLevel(towerOptions.getPierceLevel());
            ((Arrow) projectile).setKnockbackStrength(towerOptions.getKnockback());
        } else {
            pdc.set(main.getKeys().pierce, PersistentDataType.INTEGER, towerOptions.getPierceLevel());
            pdc.set(main.getKeys().knockback, PersistentDataType.INTEGER, towerOptions.getKnockback());
        }

        projectile.setVisualFire(towerOptions.isVisualFire());
        projectile.setSilent(towerOptions.isSilentProjectiles());

    }

    public Inventory getInventory() {

        updateItems();

        return inventory;

    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType type) {
        targetType = type;
        updateItems();
    }

    private void updateItems() {
        ItemStack ammunitionItem = towerOptions.getAmmunitionItem().clone();
        ItemMeta ammunitionMeta = main.towerItems.getAmmunition().getItemMeta();
        ammunitionMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.messagesYaml.getString(MessageDefault.TOWER_AMMUNITION_NAME.path).replace("%AMMO%", StaticUtil.format.format(currentAmmo))));

        List<String> lore = ammunitionMeta.getLore();

        lore.set(1, ChatColor.translateAlternateColorCodes('&', main.messagesYaml.getStringList(MessageDefault.TOWER_AMMUNITION_DESCRIPTION.path).get(1).replace("%MODE%", targetType.toString())));

        ammunitionMeta.setLore(lore);
        ammunitionItem.setItemMeta(ammunitionMeta);
        ammunitionItem.setAmount(currentAmmo < 1 ? 1 : (Math.min(currentAmmo, 64)));

        ItemStack blacklistItem = main.towerItems.getBlacklist().clone();
        ItemMeta blacklistMeta = blacklistItem.getItemMeta();
        List<String> blacklistLore = blacklistMeta.getLore();
        boolean color = true;

        for (UUID ids : blacklistedPlayers) {
            blacklistLore.add((color ? ChatColor.DARK_GRAY : ChatColor.GRAY) + Bukkit.getOfflinePlayer(ids).getName());
            color = !color;
        }

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
            final int points = (int) (towerOptions.getTowerRange() * 5);

            public void run() {

                Location particleLocation = location.clone().add(0, 2.7, 0);

                Color color = Color.fromRGB(ThreadLocalRandom.current().nextInt(256),
                        ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256));

                for (double i = 0; i < points; i += .1) {

                    double x = towerOptions.getTowerRange() * Math.sin(i);
                    double z = towerOptions.getTowerRange() * Math.cos(i);

                    location.getWorld().spawnParticle(Particle.REDSTONE, particleLocation.clone().add(x, 0, z), 1,
                            new DustOptions(color, 3));
                }

                if (!displaying)
                    cancel();
            }
        }.runTaskTimer(main, 0, 25);

    }

    public int getAmmo() {
        return currentAmmo;
    }

    public void setAmmo(int ammo) {

        this.currentAmmo = ammo;

        if (towerOptions.getMaxAmmo() > 0 && this.currentAmmo > towerOptions.getMaxAmmo())
            this.currentAmmo = towerOptions.getMaxAmmo();

        updateItems();

    }

    public String getName() {
        return name;
    }

    public void restart() {

        remove(false);

        towerOptions = new TowerOptions(main, name, false);

        new BukkitRunnable() {
            public void run() {
                startStand();
            }
        }.runTaskLater(main, 20);

    }

    public void remove() {
        remove(true);
    }

    public ItemStack getTurret() {

        ItemStack turretItem = towerOptions.getTurretItem().clone();
        ItemMeta itemm = turretItem.getItemMeta();

        itemm.setDisplayName(ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', towerOptions.getDisplay()));
        itemm.getPersistentDataContainer().set(main.getKeys().turretItem, PersistentDataType.STRING, name);
        List<String> lore = new ArrayList<>();

        for (String str : main.messagesYaml.getStringList(MessageDefault.TOWER_ITEM_DESCRIPTION.path)) {
            for (ConfigDefaults defaultConfig : ConfigDefaults.values()) {
                if (str.contains("%" + defaultConfig.name() + "%")) {

                    String newText = "";

                    switch (defaultConfig) {
                        case BLACKLIST:
                            newText += towerOptions.getBlacklist().toString();
                            break;
                        case WHITELIST:
                            newText += towerOptions.getWhitelist().toString();
                            break;
                        case TOWER_BASE:
                            newText += towerOptions.getBaseItem().toString();
                            break;
                        case TOWER_ARMOR:
                            newText += towerOptions.getTowerArmor();
                            break;
                        case TOWER_DELAY:
                            newText += towerOptions.getTowerDelay();
                            break;
                        case DISPLAY_SHOW:
                            newText += towerOptions.shouldShowDisplay();
                            break;
                        case SILENT_TOWER:
                            newText += towerOptions.isSilentTower();
                            break;
                        case TOWER_OFFSET:
                            newText += towerOptions.getTowerOffset();
                            break;
                        case TOWER_TURRET:
                            newText += towerOptions.getTurretItem().toString();
                            break;
                        case RANGE_TARGET:
                            newText += towerOptions.getTowerRange();
                            break;
                        case POTION_EFFECTS:
                            newText += towerOptions.getPotionEffects().toString();
                            break;
                        case PROJECTILE_GAP:
                            newText += towerOptions.getProjectileGap();
                            break;
                        case TOWER_MAX_AMMO:
                            newText += towerOptions.getMaxAmmo();
                            break;
                        case CRITICAL_CHANCE:
                            newText += towerOptions.getCritChance();
                            break;
                        case PROJECTILE_FIRE:
                            newText += towerOptions.getFireTicks();
                            break;
                        case PROJECTILE_TAIL:
                            newText += towerOptions.isTail();
                            break;
                        case TOWER_TOUGHNESS:
                            newText += towerOptions.getTowerToughness();
                            break;
                        case PROJECTILE_TYPE:
                            newText += towerOptions.getProjectileType().toString();
                            break;
                        case PROJECTILE_SPEED:
                            newText += towerOptions.getProjectileSpeed();
                            break;
                        case TOWER_MAX_HEALTH:
                            newText += towerOptions.getMaxHealth();
                            break;
                        case TOWER_USE_HEALTH:
                            newText += towerOptions.isUsingHealth();
                            break;
                        case CRITICAL_ACCURACY:
                            newText += towerOptions.getCritAccuracy();
                            break;
                        case PROJECTILE_DAMAGE:
                            newText += towerOptions.getProjectileDamage();
                            break;
                        case PROJECTILE_SPLITS:
                            newText += towerOptions.getSplits();
                            break;
                        case SILENT_PROJECTILE:
                            newText += towerOptions.isSilentProjectiles();
                            break;
                        case TOWER_CONSUMPTION:
                            newText += towerOptions.getShotConsumption();
                            break;
                        case TOWER_NAME_OFFSET:
                            newText += towerOptions.getNameOffset();
                            break;
                        case TOWER_REGEN_DELAY:
                            newText += towerOptions.getRegenDelay();
                            break;
                        case PROJECTILE_BOUNCES:
                            newText += towerOptions.getBounces();
                            break;
                        case PROJECTILE_GRAVITY:
                            newText += towerOptions.projectileHasGravity();
                            break;
                        case TOWER_REGEN_AMOUNT:
                            newText += towerOptions.getRegenAmount();
                            break;
                        case CRITICAL_MULTIPLIER:
                            newText += towerOptions.getCritMultiplier();
                            break;
                        case PROJECTILE_ACCURACY:
                            newText += towerOptions.getTowerAccuracy();
                            break;
                        case PROJECTILE_MATERIAL:
                            newText += towerOptions.getProjectileMaterial();
                            break;
                        case PROJECTILE_PER_SHOT:
                            newText += towerOptions.getProjectilesPerShot();
                            break;
                        case PROJECTILE_PIERCING:
                            newText += towerOptions.getPierceLevel();
                            break;
                        case PROJECTILE_TAIL_RED:
                            newText += towerOptions.getTailRed();
                            break;
                        case DISPLAY_DISPLAY_NAME:
                            newText += towerOptions.getDisplay();
                            break;
                        case PROJECTILE_HIT_TYPES:
                            newText += towerOptions.getHitTypes().toString();
                            break;
                        case PROJECTILE_KNOCKBACK:
                            newText += towerOptions.getKnockback();
                            break;
                        case PROJECTILE_TAIL_BLUE:
                            newText += towerOptions.getTailBlue();
                            break;
                        case PROJECTILE_TAIL_SIZE:
                            newText += towerOptions.getTailSize();
                            break;
                        case PROJECTILE_TAIL_GREEN:
                            newText += towerOptions.getTailGreen();
                            break;
                        case TOWER_AMMUNITION_ITEM:
                            newText += towerOptions.getAmmunitionItem().toString();
                            break;
                        case PROJECTILE_VISUAL_FIRE:
                            newText += towerOptions.isVisualFire();
                            break;
                        case PROJECTILE_BOUNCE_BOOST:
                            newText += towerOptions.getBounceBoost();
                            break;
                        case PROJECTILE_SPLIT_AMOUNT:
                            newText += towerOptions.getSplitAmount();
                            break;
                        case RANGE_PICKUP_AMMUNITION:
                            newText += towerOptions.getAmmunitionPickupRange();
                            break;
                        case PROJECTILE_SPLIT_ACCURACY:
                            newText += towerOptions.getSplitAccuracy();
                            break;
                    }


                    str = str.replace("%" + defaultConfig.name() + "%", newText);

                }


            }

            lore.add(ChatColor.translateAlternateColorCodes('&', str));

        }

        itemm.setLore(lore);

        turretItem.setItemMeta(itemm);

        return turretItem;

    }

    public void remove(boolean drop) {
        new BukkitRunnable() {
            public void run() {
                main.removeTower(towerInstance);
                if (drop)
                    location.getWorld().dropItemNaturally(turretStand.getEyeLocation(), getTurret());

                ItemStack ammunition = towerOptions.getAmmunitionItem().clone();

                int amount;

                while (currentAmmo > 0 && drop) {
                    amount = Math.min(currentAmmo, 64);
                    ammunition.setAmount(amount);
                    currentAmmo -= amount;
                    location.getWorld().dropItemNaturally(turretStand.getEyeLocation(), ammunition);
                }

                turretStand.remove();
                baseStand.remove();
                nameStand.remove();
                main.removeTower(towerInstance);
                hitBoxes.forEach(hitbox -> {
                    hitbox.remove();
                });
                hitBoxes.clear();
            }
        }.runTaskLater(main, 0);
    }

    public void blacklistPlayer(OfflinePlayer player) {
        blacklistedPlayers.add(player.getUniqueId());
    }

    public List<UUID> getBlacklistedPlayers() {
        return blacklistedPlayers;
    }

    private Vector noRiderOperation() throws Exception {

        new BukkitRunnable() {
            public void run() {
                nearbyEntities = turretStand.getNearbyEntities(towerOptions.getTowerRange(), towerOptions.getTowerRange(), towerOptions.getTowerRange());
                nearbyEntitiesExtended = turretStand.getNearbyEntities(towerOptions.getTowerRange() * 1.5, towerOptions.getTowerRange() * 1.5, towerOptions.getTowerRange() * 1.5);
            }
        }.runTaskLater(main, 0);

        if (nearbyEntities.size() == 0)
            throw new Exception("No entities nearby");

        Entity target = null;
        Vector direction = null;

        double distance;

        for (Entity entity : nearbyEntities) {

            if (!hitBoxes.get(1).hasLineOfSight(entity) || entity.equals(baseStand)) continue;
            if (entity.getPersistentDataContainer().has(main.getKeys().turretStand, PersistentDataType.STRING))
                continue;

            if (entity instanceof Player) {
                if (blacklistedPlayers.contains(entity.getUniqueId())) continue;
                if (((Player) entity).getGameMode() != GameMode.SURVIVAL) continue;
            }
            if (towerOptions.getWhitelist().size() != 0) {
                if (!towerOptions.getWhitelist().contains(entity.getType()))
                    continue;
            }
            if (towerOptions.getBlacklist().contains(entity.getType())) {
                continue;
            }
            if (entity.isDead())
                continue;
            if (target == null) target = entity;
            switch (targetType) { // Handles different target types
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
                case WEAKEST:
                    target = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() < ((LivingEntity) target).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() ? entity : target;
                    break;
                case STRONGEST:
                    target = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() > ((LivingEntity) target).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() ? entity : target;
                    break;
            }

            Location targetLocation = target.getLocation().add(0, target.getHeight() / 2, 0);

            distance = targetLocation.distance(location);

            if (distance > towerOptions.getTowerRange()) continue;

            direction = targetLocation.clone().add(0, towerOptions.projectileHasGravity() ? (distance / 8) - (towerOptions.getProjectileSpeed() / 5) : 0, 0).subtract(hitBoxes.get(1).getLocation()).toVector();

        }

        if (direction == null) throw new Exception("No entities nearby");

        return direction;

    }

    public List<Slime> getHitBoxes() {
        return hitBoxes;
    }

    public Location getLocation() {
        return location;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public ArmorStand getNameStand() {
        return nameStand;
    }

    public TowerOptions getTowerOptions() {
        return towerOptions;
    }

    public Location getTurretBarrelLocation() {
        return turretBarrelLocation;
    }

    public double setHealth(double health) {
        this.health = health;
        return health;
    }

    public double addHealth(double health) {
        this.health = Math.min(getMaxHealth(), this.health + health);
        return this.health;
    }

    public double getHealth() {
        return health;
    }

    public double damage(double damage) {
        setHealth(health - damage);
        return health;
    }

    public double getMaxHealth() {
        return towerOptions.getMaxHealth();
    }

}
