package me.isaac.defencetowers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.isaac.defencetowers.events.BulletHitEntity;
import me.isaac.defencetowers.events.InteractTower;
import me.isaac.defencetowers.events.PlaceTurret;
import me.isaac.defencetowers.events.PlayerLeave;

public class DefenceTowersMain extends JavaPlugin {

    /*
                Changes

                1.17 supported & tested.

                Fixed distance issues.

     */

    private NamespacedKeys keys;

    public static final File towerFolder = new File("plugins//DefenceTowers//Towers");
    public static final String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&dDefence Towers&7]&r ");

    private final HashMap<ArmorStand, Tower> towerLocations = new HashMap<>();

    final File towerLocationsFile = new File("plugins//DefenceTowers//TowerLocations.yml");
    YamlConfiguration towerLocationYaml = YamlConfiguration.loadConfiguration(towerLocationsFile);

    public final TowerItems towerItems = new TowerItems();

    public void onEnable() {

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

                if (entity.getVelocity().length() < .2 || ((Arrow) entity).isOnGround()) entity.remove();

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

            Tower tower = new Tower(this, "Example Tower", null, true);

            tower.setDisplay("&dExample Tower");
            tower.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));

            getLogger().log(Level.INFO, prefix + "Example Tower created!");
        }
    }

    private void saveTowers() {
        if (towerLocations.size() != 0) {
            for (ArmorStand stand : towerLocations.keySet()) {
                towerLocationYaml.set(Tower.locationString(stand.getLocation()) + ".Tower", towerLocations.get(stand).getName());
                towerLocationYaml.set(Tower.locationString(stand.getLocation()) + ".Ammo", towerLocations.get(stand).getAmmo());
                List<String> playerList = new ArrayList<>();
                towerLocations.get(stand).getBlacklistedPlayers().forEach(id -> playerList.add(id.toString()));
                towerLocationYaml.set(Tower.locationString(stand.getLocation()) + ".Blacklist", playerList);
                stand.remove();
                towerLocations.get(stand).baseStand.remove();
            }

            try {
                towerLocationYaml.save(towerLocationsFile);
                if (!towerLocationsFile.createNewFile()) getLogger().log(Level.WARNING, "Tower locations could not be saved!");
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

            Tower tower = new Tower(this, towerLocationYaml.getString(keys + ".Tower"), Tower.locationString(keys).add(.5, -.4, .5), false);

            tower.setAmmo(towerLocationYaml.getInt(keys + ".Ammo"));

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

    public Tower getTower(ArmorStand armorStand) {
        if (towerLocations.containsKey(armorStand)) return towerLocations.get(armorStand);
        throw new IllegalArgumentException("Entity is not a tower");
    }

    public void addTowerStand(ArmorStand stand, Tower tower) {
        towerLocations.put(stand, tower);
    }

    public void removeTower(ArmorStand stand) {
        towerLocations.remove(stand);
    }

    public HashMap<ArmorStand, Tower> getTowers() {
        return towerLocations;
    }

    public void updateExistingTowers(String name) {
        if (towerLocations.size() != 0) {

            List<Tower> existingTowers = new ArrayList<>(towerLocations.values());

            for (Tower towers : existingTowers) {
                if (!towers.getName().equals(name)) continue;

                towers.restart();

            }

        }
    }

}
