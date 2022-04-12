package me.isaac.defencetowers;

import me.isaac.defencetowers.events.BulletHitEntity;
import me.isaac.defencetowers.events.InteractTower;
import me.isaac.defencetowers.events.PlaceTurret;
import me.isaac.defencetowers.events.PlayerLeave;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DefenceTowersMain extends JavaPlugin {

    /*
                Changes:
                Towers now target smaller mobs correctly.
                Fixed item projectile types from flashing into snowballs.
                Towers hitbox's replaced with slimes.
                Fixed towers dropping incorrect ammunition item.
                Bug fixes.

                Ideas:
                Run commands on bullet hit, that also run commands after a delay.
                Add particle projectiles.

     */

    private NamespacedKeys keys;

    public static DefenceTowersMain instance;

    public static final File towerFolder = new File("plugins//DefenceTowers//Towers");
    public static final String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&dDefence Towers&7]&r ");

//    private final HashMap<Entity, Tower> towerLocations = new HashMap<>();

    private final List<Tower> allTowers = new ArrayList<>();

    final File towerLocationsFile = new File("plugins//DefenceTowers//TowerLocations.yml");
    YamlConfiguration towerLocationYaml = YamlConfiguration.loadConfiguration(towerLocationsFile);

    public final TowerItems towerItems = new TowerItems();

    public void onEnable() {

        instance = this;

        keys = new NamespacedKeys(this);

        registerCommands();
        registerEvents();

        createExampleTower();

        loadExistingTowers();

        removeTurretBulletsLoop();

    }

    public void onDisable() {

        removeBullets();
        saveTowers();

    }

    private void registerCommands() {

        Commands commands = new Commands(this);

        getCommand("defencetowers").setExecutor(commands);
        getCommand("defencetowers").setTabCompleter(commands);

    }

    private InteractTower interactTower;

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        interactTower = new InteractTower(this);

        pm.registerEvents(new PlaceTurret(this), this);
        pm.registerEvents(new BulletHitEntity(this), this);
        pm.registerEvents(interactTower, this);
        pm.registerEvents(new PlayerLeave(this), this);

    }

    public InteractTower getInteractTowerInstance() {
        return interactTower;
    }

    public NamespacedKeys getKeys() {
        return keys;
    }

    private void removeBullets() {
        for (World worlds : getServer().getWorlds()) {
            for (Entity entity : worlds.getEntities()) {
                if (!entity.getPersistentDataContainer().has(getKeys().bullet, PersistentDataType.STRING)) continue;
                if (entity.getPersistentDataContainer().get(getKeys().bounces, PersistentDataType.INTEGER) > 0) continue;

                if (entity.getVelocity().length() < .2 || entity.isOnGround()) entity.remove();

            }
        }
    }

    private void removeTurretBulletsLoop() {

        new BukkitRunnable() {
            public void run() {
                removeBullets();
            }
        }.runTaskTimer(this, 0, 5);

    }

    private void createExampleTower() {
        if (!Tower.exists("Example Tower")) {

            StaticUtil.checkConfig("Example Tower");

            Tower tower = new Tower(this, "Example Tower", null, false);

            tower.setDisplay("&dExample Tower");
            tower.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));

            getLogger().log(Level.INFO, prefix + "Example Tower created!");
        }
    }

    private void saveTowers() {
        if (allTowers.size() != 0) {

            for (Tower tower : allTowers) {

                String towerLocation = StaticUtil.locationString(tower.getBaseStand().getLocation().add(0, .4, 0));

                towerLocationYaml.set(towerLocation + ".Tower", tower.getName());
                towerLocationYaml.set(towerLocation + ".Ammo", tower.getAmmo());
                towerLocationYaml.set(towerLocation + ".TargetingMode", tower.getTargetType().name());
                List<String> playerList = new ArrayList<>();
                tower.getBlacklistedPlayers().forEach(id -> playerList.add(id.toString()));
                towerLocationYaml.set(towerLocation + ".Blacklist", playerList);
                tower.getStand().remove();
                tower.getBaseStand().remove();
                tower.getHitBoxes().forEach(Entity::remove);
            }

            try {
                towerLocationYaml.save(towerLocationsFile);
                towerLocationsFile.createNewFile();
            } catch (IOException ignored) {}
        }
    }

    private void loadExistingTowers() {
        if (!towerLocationsFile.exists()) return;

        for (String keys : towerLocationYaml.getKeys(false)) {

            if (!Tower.exists(towerLocationYaml.getString(keys + ".Tower"))) {

                getLogger().log(Level.WARNING, prefix + towerLocationYaml.getString(keys + ".Tower") + " file does not exist, and could not be loaded!");

                continue;
            }

            Tower tower = new Tower(this, towerLocationYaml.getString(keys + ".Tower"), StaticUtil.locationString(keys).add(.5, -.4, .5), false);

            tower.setAmmo(towerLocationYaml.getInt(keys + ".Ammo"));
            tower.setTargetType(TargetType.valueOf(towerLocationYaml.getString(keys + ".TargetingMode")));

            for (String idStr : towerLocationYaml.getStringList(keys + ".Blacklist")) {
                tower.blacklistPlayer(getServer().getOfflinePlayer(UUID.fromString(idStr)));
            }

        }

        for (String key : towerLocationYaml.getKeys(false)) {
            towerLocationYaml.set(key, null);
        }

        try {
            towerLocationYaml.save(towerLocationsFile);
        } catch (IOException ignored) {}

        towerLocationsFile.delete();
    }

    public Tower getTower(Entity entity) {

        for (Tower tower : allTowers) {
            if (tower.getEntities().contains(entity)) {
                return tower;
            }
        }
        throw new IllegalArgumentException("Entity is not a tower");
    }

    public void addTower(Tower tower) {
        allTowers.add(tower);
    }

    public void removeTower(Tower tower) {
        allTowers.remove(tower);
    }

    public void createExampleTowers() {

        StaticUtil.checkConfig("Flamethrower Tower"); // Sets up default config values
        Tower flameTower = new Tower(this, "Flamethrower Tower", null, false); // Gets newly created tower
        flameTower.setDisplay("&4Flamethrower Tower");
        flameTower.setPerShot(5);
        flameTower.setGap(1);
        flameTower.setProjectileDamage(.5d);
        flameTower.setSpeed(1);
        flameTower.setTowerAccuracy(12);
        flameTower.setVisualFire(true);
        flameTower.setFireTicks(200);
        flameTower.setAmmunitionItem(new ItemStack(Material.FIRE_CHARGE));
        flameTower.setColorSize(2);
        flameTower.setTowerDelay(6);
        flameTower.setCritChance(.3);
        flameTower.setCritMultiplier(2);
        flameTower.setCritAccuracy(.5);
        flameTower.setBase(new ItemStack(Material.GILDED_BLACKSTONE));
        flameTower.setRange(10);
        flameTower.color(true);
        flameTower.setProjectileType(ProjectileType.SMALL_FIREBALL);
        flameTower.setColor(Color.ORANGE);
        flameTower.setTurret(StaticUtil.getHeadFromValue("ewogICJ0aW1lc3RhbXAiIDogMTY0ODk2MzMwMjM4NCwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgzZWYxZmI3ZDk5NzBkNzUwNTk1ZDNiNWRjMTRhNTQxZjI0NDIxNTQ5YzdlYWE4M2U3ZGNiMjYzZjRmODRmNWIiCiAgICB9CiAgfQp9"));

        StaticUtil.checkConfig("Healing Machinegun Tower");
        Tower healingTower = new Tower(this, "Healing Machinegun Tower", null, false);
        healingTower.setDisplay("&4Healing Machinegun Tower");
        healingTower.setPerShot(3);
        healingTower.setGap(1);
        healingTower.setProjectileDamage(0);
        healingTower.setSpeed(.5f);
        healingTower.setTowerAccuracy(15f);
        healingTower.setProjectileType(ProjectileType.ITEM);
        healingTower.setProjectileMaterial(Material.POPPY);
        healingTower.setAmmunitionItem(new ItemStack(Material.POPPY));
        healingTower.setBounces(4);
        healingTower.setTowerDelay(4);
        healingTower.setRange(16);
        healingTower.setColor(Color.RED);
        healingTower.color(true);
        healingTower.whitelist(EntityType.PLAYER);
        healingTower.setTurret(StaticUtil.getHeadFromValue("ewogICJ0aW1lc3RhbXAiIDogMTY0ODk2MzMyOTgzNCwKICAicHJvZmlsZUlkIiA6ICIwZDYyOGNhZTBlOTM0MTZkYjQ1OWM3Y2FhOGNiZDU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJEVmFfRmFuQm95IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY0OWU5NjFkZDY1MWMxZTllNWFhMzExNDg5NjcwMGU3MmVkZGM2ZTkxZGRlNTBjMzg0MzhlMDVjYTdmNmZlMSIKICAgIH0KICB9Cn0="));
        healingTower.setBase(new ItemStack(Material.RED_GLAZED_TERRACOTTA));
        healingTower.clearPotionEffects();
        healingTower.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 10, 1));

        StaticUtil.checkConfig("Shotgun Tower");
        Tower shotgunTower = new Tower(this, "Shotgun Tower", null, false);
        shotgunTower.setDisplay("&bShotgun Tower");
        shotgunTower.setPerShot(9);
        shotgunTower.setProjectileDamage(3);
        shotgunTower.setSpeed(1);
        shotgunTower.setAmmunitionItem(new ItemStack(Material.STONE_BRICKS));
        shotgunTower.setTowerAccuracy(20);
        shotgunTower.setTowerDelay(35);
        shotgunTower.setColor(Color.AQUA);
        shotgunTower.setProjectileType(ProjectileType.ITEM);
        shotgunTower.setProjectileMaterial(Material.DIAMOND_BLOCK);
        shotgunTower.setRange(14);
        shotgunTower.setTurret(StaticUtil.getHeadFromValue("ewogICJ0aW1lc3RhbXAiIDogMTY0ODk2Mzg0ODUxNywKICAicHJvZmlsZUlkIiA6ICJjMDNlZTUxNjIzZTU0ZThhODc1NGM1NmVhZmJjZDA4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJsYXltYW51ZWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjlkYjI2N2ExMDdmYzA2ZmYxNTVmMzliZGY4ZjIzMzFmNTAzOTc5ZmRmYThlZWM2MDlhOTIyZWQyZWExNGQ3YSIKICAgIH0KICB9Cn0="));
        shotgunTower.setBase(new ItemStack(Material.BEACON));
        shotgunTower.setCritChance(.1);
        shotgunTower.setCritMultiplier(2);
        shotgunTower.setCritAccuracy(.8);
        shotgunTower.clearPotionEffects();

        StaticUtil.checkConfig("Sniper Tower");
        Tower sniperTower = new Tower(this, "Sniper Tower", null, false);
        sniperTower.setDisplay("&cSniper Tower");
        sniperTower.setProjectileDamage(10);
        sniperTower.setSpeed(2);
        sniperTower.setTowerAccuracy(1);
        sniperTower.setPierceLevel(2);
        sniperTower.setKnockback(1);
        sniperTower.setProjectileType(ProjectileType.TRIDENT);
        sniperTower.setCritChance(.4);
        sniperTower.setCritMultiplier(2);
        sniperTower.setCritAccuracy(.8);
        sniperTower.color(true);
        sniperTower.setColor(Color.GREEN);
        sniperTower.setTurret(StaticUtil.getHeadFromValue("ewogICJ0aW1lc3RhbXAiIDogMTY0ODk2MzIyMjQzOSwKICAicHJvZmlsZUlkIiA6ICIwZDYyOGNhZTBlOTM0MTZkYjQ1OWM3Y2FhOGNiZDU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJEVmFfRmFuQm95IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc1NWZiY2FkZDRjMDVlM2ZiMzJiOWJlYjBkYTNjNTUyNzc0ZDA5MmI0NDQyM2NlYTQ1NGM0ZTY1YmMxYzgyOCIKICAgIH0KICB9Cn0="));
        sniperTower.setBase(new ItemStack(Material.FLOWERING_AZALEA_LEAVES));
        sniperTower.setRange(25);
        sniperTower.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1));

    }

    public List<Tower> getTowers() {
        return allTowers;
    }

    public void updateExistingTowers(String name) {
        if (allTowers.size() != 0) {

            for (Tower towers : allTowers) {
                if (!towers.getName().equals(name)) continue;

                towers.restart();

            }

        }
    }

}
