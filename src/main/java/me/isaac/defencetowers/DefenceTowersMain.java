package me.isaac.defencetowers;

import me.isaac.defencetowers.events.ProjectileHitEvents;
import me.isaac.defencetowers.events.InteractTower;
import me.isaac.defencetowers.events.PlaceTower;
import me.isaac.defencetowers.events.PlayerLeave;
import me.isaac.defencetowers.tower.*;
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
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DefenceTowersMain extends JavaPlugin {

    /*
    Added 2 more target types, STRONGEST, WEAKEST
        STRONGEST will attack the entity that does the most damage,
        WEAKEST will attack the entity that does the least damage.

    Added new tower config options, Splits, Split amount, Bounce Boost, Hit Types.

    Added HitTypes - These will affect what happens when a projectile hits something.
        BREAK - projectiles will be removed.
        SPLIT - projectiles will split into multiple projectiles before being removed.
       BOUNCE - projectiles will bounce multiple times before being removed.

    Added Potato Tower example

    Fixed placing towers inside other towers
     */

    private NamespacedKeys keys;

    public static DefenceTowersMain instance;

    public static final File towerFolder = new File("plugins//DefenceTowers//Towers");
    public static final String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&dDefence Towers&7]&r ");

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

        removeProjectiles();
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

        pm.registerEvents(new PlaceTower(this), this);
        pm.registerEvents(new ProjectileHitEvents(this), this);
        pm.registerEvents(interactTower, this);
        pm.registerEvents(new PlayerLeave(this), this);

    }

    public InteractTower getInteractTowerInstance() {
        return interactTower;
    }

    public NamespacedKeys getKeys() {
        return keys;
    }

    private void removeProjectiles() {
        for (World worlds : getServer().getWorlds()) {
            for (Entity entity : worlds.getEntities()) {
                if (!entity.getPersistentDataContainer().has(getKeys().bullet, PersistentDataType.STRING)) continue;
                if ((entity.getVelocity().length() < .2 && !entity.hasGravity()) || entity.isOnGround()) entity.remove();
            }
        }
    }

    private void removeTurretBulletsLoop() {

        new BukkitRunnable() {
            public void run() {
                removeProjectiles();
            }
        }.runTaskTimer(this, 0, 5);

    }

    private void createExampleTower() {
        if (!Tower.exists("Example Tower")) {

            StaticUtil.checkConfig("Example Tower");

            Tower tower = new Tower(this, "Example Tower", null, false);

            tower.getTowerOptions().setDisplay("&dExample Tower");
            tower.getTowerOptions().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));

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
                tower.getTurretStand().remove();
                tower.getBaseStand().remove();
                tower.getNameStand().remove();
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
        TowerOptions flameTowerOptions = flameTower.getTowerOptions();
        flameTowerOptions.setDisplay("&4Flamethrower Tower");
        flameTowerOptions.setProjectilesPerShot(5);
        flameTowerOptions.setGap(1);
        flameTowerOptions.setProjectileDamage(.5d);
        flameTowerOptions.setSpeed(1);
        flameTowerOptions.setTowerAccuracy(12);
        flameTowerOptions.setVisualFire(true);
        flameTowerOptions.setFireTicks(200);
        flameTowerOptions.setAmmunitionItem(new ItemStack(Material.FIRE_CHARGE));
        flameTowerOptions.setColorSize(2);
        flameTowerOptions.setTowerDelay(6);
        flameTowerOptions.setCritChance(.3);
        flameTowerOptions.setCritMultiplier(2);
        flameTowerOptions.setCritAccuracy(.5);
        flameTowerOptions.setBase(new ItemStack(Material.GILDED_BLACKSTONE));
        flameTowerOptions.setRange(10);
        flameTowerOptions.color(true);
        flameTowerOptions.setProjectileType(ProjectileType.SMALL_FIREBALL);
        flameTowerOptions.setColor(Color.ORANGE);
        flameTowerOptions.setTurret(StaticUtil.getHeadFromValue("ewogICJ0aW1lc3RhbXAiIDogMTY0ODk2MzMwMjM4NCwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgzZWYxZmI3ZDk5NzBkNzUwNTk1ZDNiNWRjMTRhNTQxZjI0NDIxNTQ5YzdlYWE4M2U3ZGNiMjYzZjRmODRmNWIiCiAgICB9CiAgfQp9"));

        StaticUtil.checkConfig("Healing Machinegun Tower");
        Tower healingTower = new Tower(this, "Healing Machinegun Tower", null, false);
        TowerOptions healingTowerOptions = healingTower.getTowerOptions();
        healingTowerOptions.setDisplay("&4Healing Machinegun Tower");
        healingTowerOptions.setProjectilesPerShot(2);
        healingTowerOptions.setGap(1);
        healingTowerOptions.setTowerDelay(10);
        healingTowerOptions.setProjectileDamage(0d);
        healingTowerOptions.setSpeed(.5f);
        healingTowerOptions.setTowerAccuracy(15f);
        healingTowerOptions.setProjectileType(ProjectileType.ITEM);
        healingTowerOptions.setProjectileMaterial(Material.POPPY);
        healingTowerOptions.setAmmunitionItem(new ItemStack(Material.POPPY));
        healingTowerOptions.setBounces(4);
        healingTowerOptions.setTowerDelay(4);
        healingTowerOptions.setRange(16);
        healingTowerOptions.setColor(Color.RED);
        healingTowerOptions.addHitType(HitType.SPLIT, 1);
        healingTowerOptions.addHitType(HitType.BOUNCE, 2);
        healingTowerOptions.addHitType(HitType.BREAK, .2);
        healingTowerOptions.setSplits(2);
        healingTowerOptions.setSplitAmount(1);
        healingTowerOptions.color(true);
        healingTowerOptions.whitelist(EntityType.PLAYER);
        healingTowerOptions.setTurret(StaticUtil.getHeadFromValue("ewogICJ0aW1lc3RhbXAiIDogMTY0ODk2MzMyOTgzNCwKICAicHJvZmlsZUlkIiA6ICIwZDYyOGNhZTBlOTM0MTZkYjQ1OWM3Y2FhOGNiZDU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJEVmFfRmFuQm95IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY0OWU5NjFkZDY1MWMxZTllNWFhMzExNDg5NjcwMGU3MmVkZGM2ZTkxZGRlNTBjMzg0MzhlMDVjYTdmNmZlMSIKICAgIH0KICB9Cn0="));
        healingTowerOptions.setBase(new ItemStack(Material.RED_GLAZED_TERRACOTTA));
        healingTowerOptions.clearPotionEffects();
        healingTowerOptions.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 10, 1));

        StaticUtil.checkConfig("Shotgun Tower");
        Tower shotgunTower = new Tower(this, "Shotgun Tower", null, false);
        TowerOptions shotgunTowerOptions = shotgunTower.getTowerOptions();
        shotgunTowerOptions.setDisplay("&bShotgun Tower");
        shotgunTowerOptions.setProjectilesPerShot(9);
        shotgunTowerOptions.setProjectileDamage(3d);
        shotgunTowerOptions.setSpeed(1);
        shotgunTowerOptions.setAmmunitionItem(new ItemStack(Material.STONE_BRICKS));
        shotgunTowerOptions.setTowerAccuracy(20);
        shotgunTowerOptions.setTowerDelay(35);
        shotgunTowerOptions.setColor(Color.AQUA);
        shotgunTowerOptions.setProjectileType(ProjectileType.ITEM);
        shotgunTowerOptions.setProjectileMaterial(Material.DIAMOND_BLOCK);
        shotgunTowerOptions.setRange(14);
        shotgunTowerOptions.setTurret(StaticUtil.getHeadFromValue("ewogICJ0aW1lc3RhbXAiIDogMTY0ODk2Mzg0ODUxNywKICAicHJvZmlsZUlkIiA6ICJjMDNlZTUxNjIzZTU0ZThhODc1NGM1NmVhZmJjZDA4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJsYXltYW51ZWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjlkYjI2N2ExMDdmYzA2ZmYxNTVmMzliZGY4ZjIzMzFmNTAzOTc5ZmRmYThlZWM2MDlhOTIyZWQyZWExNGQ3YSIKICAgIH0KICB9Cn0="));
        shotgunTowerOptions.setBase(new ItemStack(Material.BEACON));
        shotgunTowerOptions.setCritChance(.1);
        shotgunTowerOptions.setCritMultiplier(2);
        shotgunTowerOptions.setCritAccuracy(.8);
        shotgunTowerOptions.clearPotionEffects();

        StaticUtil.checkConfig("Sniper Tower");
        Tower sniperTower = new Tower(this, "Sniper Tower", null, false);
        TowerOptions sniperTowerOptions = sniperTower.getTowerOptions();
        sniperTowerOptions.setDisplay("&cSniper Tower");
        sniperTowerOptions.setProjectileDamage(10d);
        sniperTowerOptions.setSpeed(2);
        sniperTowerOptions.setTowerAccuracy(1);
        sniperTowerOptions.setPierceLevel(2);
        sniperTowerOptions.setKnockback(1);
        sniperTowerOptions.setProjectileType(ProjectileType.TRIDENT);
        sniperTowerOptions.setCritChance(.4);
        sniperTowerOptions.setCritMultiplier(2);
        sniperTowerOptions.setCritAccuracy(.8);
        sniperTowerOptions.color(true);
        sniperTowerOptions.setColor(Color.GREEN);
        sniperTowerOptions.setTurret(StaticUtil.getHeadFromValue("ewogICJ0aW1lc3RhbXAiIDogMTY0ODk2MzIyMjQzOSwKICAicHJvZmlsZUlkIiA6ICIwZDYyOGNhZTBlOTM0MTZkYjQ1OWM3Y2FhOGNiZDU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJEVmFfRmFuQm95IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc1NWZiY2FkZDRjMDVlM2ZiMzJiOWJlYjBkYTNjNTUyNzc0ZDA5MmI0NDQyM2NlYTQ1NGM0ZTY1YmMxYzgyOCIKICAgIH0KICB9Cn0="));
        sniperTowerOptions.setBase(new ItemStack(Material.FLOWERING_AZALEA_LEAVES));
        sniperTowerOptions.setRange(25);
        sniperTowerOptions.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1));

        StaticUtil.checkConfig("Potato Tower");
        Tower potatoTower = new Tower(this, "Potato Tower", null, false);
        TowerOptions potatoTowerOptions = potatoTower.getTowerOptions();
        potatoTowerOptions.setDisplay("&6Potato Tower");
        potatoTowerOptions.setProjectileDamage(10d);
        potatoTowerOptions.setSpeed(1.5f);
        potatoTowerOptions.setTowerAccuracy(15f);
        potatoTowerOptions.setProjectileType(ProjectileType.ITEM);
        potatoTowerOptions.setProjectileMaterial(Material.POTATO);
        potatoTowerOptions.setColor(Color.fromRGB(183, 146, 104));
        potatoTowerOptions.color(true);
        potatoTowerOptions.setColorSize(2f);
        potatoTowerOptions.addHitType(HitType.SPLIT, 1d);
        potatoTowerOptions.addHitType(HitType.BREAK, 0.2d);
        potatoTowerOptions.setSplits(3);
        potatoTowerOptions.setSplitAmount(5);
        potatoTowerOptions.setBounceBoost(.3);
        potatoTowerOptions.setRange(30);
        potatoTowerOptions.setTowerDelay(40);
        potatoTowerOptions.setAmmunitionItem(new ItemStack(Material.POTATO));
        potatoTowerOptions.setBase(new ItemStack((Material.PODZOL)));
        potatoTowerOptions.setTurret(StaticUtil.getHeadFromValue("eyJ0aW1lc3RhbXAiOjE1ODY4MDM4MzQzMjcsInByb2ZpbGVJZCI6IjNmYzdmZGY5Mzk2MzRjNDE5MTE5OWJhM2Y3Y2MzZmVkIiwicHJvZmlsZU5hbWUiOiJZZWxlaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZmNTVlYjQ0ODJhNWM4MGEyOTRmNDU2MDQ1ZDIzNDgzYWMzMDVjZDdhZjFkZWExNWI2MmYyNjlhMGMzNzA5MGEifX19"));

    }

    public List<Tower> getTowers() {
        return allTowers;
    }

    public void updateExistingTowers(String name) {
        if (allTowers.size() == 0) return;

        for (Tower towers : allTowers) {
            if (!towers.getName().equals(name)) continue;
            towers.kickOperator();
            towers.restart();
        }
    }

}
