package me.isaac.defencetowers.events;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.TargetType;
import me.isaac.defencetowers.Tower;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class InteractTower implements Listener {

    DefenceTowersMain main;

    public InteractTower(DefenceTowersMain main) {
        this.main = main;
    }

    @EventHandler
    public void onClickTower(PlayerInteractAtEntityEvent e) {
        if (!e.getRightClicked().getPersistentDataContainer().has(main.getKeys().turretStand,
                PersistentDataType.STRING))
            return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        e.setCancelled(true);

        Tower tower = main.getTower(e.getRightClicked());

        if (!e.getPlayer().hasPermission("defencetowers.bypassblacklist")
                && !tower.getBlacklistedPlayers().contains(e.getPlayer().getUniqueId())) {

            if (e.getPlayer().hasPermission("defencetowers.addblockedarrows")) {
                if (e.getPlayer().getInventory().getItemInMainHand().isSimilar(tower.getAmmunitionItem())) {

                    addAmmunitionToTurret(e.getPlayer(), tower);

                    return;
                }
            }

            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(ChatColor.DARK_RED + "Tower Locked"));
            return;
        }

        if (e.getPlayer().getInventory().getItemInMainHand().isSimilar(tower.getAmmunitionItem())) {

            addAmmunitionToTurret(e.getPlayer(), tower);

            return;
        }

        if (e.getPlayer().isSneaking()) {
            tower.remove(e.getPlayer().getGameMode() == GameMode.SURVIVAL);

            for (Player players : editingTowerBlacklist.keySet()) {
                Tower editTower = editingTowerBlacklist.get(players);

                if (editTower.equals(tower)) editingTowerBlacklist.remove(players);

            }

            tower.displayRange(false);
            return;
        }

        Inventory towerInv = tower.getInventory();

        if (!e.getPlayer().hasPermission("defencetowers.ride"))
            tower.getInventory().setItem(4, null);

        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent
                .fromLegacyText(ChatColor.GOLD + "Shift + Click" + ChatColor.YELLOW + " a tower to pick it up"));
        e.getPlayer().openInventory(towerInv);

    }

    @EventHandler
    public void onHitboxDeath(EntityDeathEvent e) {
        if (!e.getEntity().getPersistentDataContainer().has(main.getKeys().turretStand, PersistentDataType.STRING)) return;
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    private void addAmmunitionToTurret(Player player, Tower tower) {
        int arrowAmount = player.getInventory().getItemInMainHand().getAmount();

        if (tower.getMaxAmmo() > 0 && tower.getAmmo() + arrowAmount > tower.getMaxAmmo()) {
            arrowAmount -= tower.getMaxAmmo() - tower.getAmmo();
            tower.setAmmo(tower.getMaxAmmo());
        } else {
            tower.setAmmo(tower.getAmmo() + arrowAmount);
            arrowAmount = 0;
        }

        if (arrowAmount == 0)
            player.getInventory().setItemInMainHand(null);
        else
            player.getInventory().getItemInMainHand().setAmount(arrowAmount);
    }

    @EventHandler
    public void onPlayerShootTurret(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND)
            return;
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        if (e.getPlayer().getVehicle() == null)
            return;
        if (!e.getPlayer().getVehicle().getPersistentDataContainer().has(main.getKeys().turretStand,
                PersistentDataType.STRING))
            return;

        Tower tower = main.getTower(e.getPlayer().getVehicle());

        tower.shoot(tower.getProjectileType(), e.getPlayer().getLocation().getDirection());

    }

    public HashMap<Player, Tower> editingTowerBlacklist = new HashMap<>();

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {

        Tower tower = getInventoriesTower(e.getView().getTopInventory());
        int amount;

        if (tower == null || e.getClickedInventory() == null)
            return;
        e.setCancelled(true);

        if (e.getView().getTopInventory().getItem(3) == null) return;

        if (e.getClickedInventory().equals(e.getView().getTopInventory())) { // Top inventory clicks

            amount = Math.min(tower.getAmmo(), 64);

            switch (e.getSlot()) {
                case 1: // Clicking radius toggle
                    tower.displayRange(!tower.isDisplaying());
                    break;
                case 2: // Clicking blacklist
                    if (editingTowerBlacklist.containsKey(e.getWhoClicked())) {
                        editingTowerBlacklist.remove(e.getWhoClicked());
                        e.getWhoClicked().sendMessage(DefenceTowersMain.prefix + "No longer setting blacklist");
                        return;
                    }
                    editingTowerBlacklist.put((Player) e.getWhoClicked(), tower);
                    e.getWhoClicked()
                            .sendMessage(DefenceTowersMain.prefix + "Type a players name to add it to the blacklist");
                    e.getWhoClicked().sendMessage(
                            DefenceTowersMain.prefix + "type \"remove <player>\" to remove them from the blacklist");
                    e.getWhoClicked().sendMessage(DefenceTowersMain.prefix + "Type \"cancel\" to stop");

                    e.getWhoClicked().closeInventory();

                    break;
                case 3: // Clicking ammunition arrow

                    if (tower.getAmmo() == 0) return;

                    switch (e.getAction()) {
                        case PICKUP_ALL:
                            tower.setAmmo(tower.getAmmo() - amount);

                            ItemStack ammunition = tower.getAmmunitionItem();
                            ammunition.setAmount(amount);

                            e.getWhoClicked().setItemOnCursor(ammunition);
                            break;
                        case MOVE_TO_OTHER_INVENTORY:

                            if (e.getView().getBottomInventory().firstEmpty() == -1)
                                return;

                            ammunition = tower.getAmmunitionItem();
                            ammunition.setAmount(amount);

                            e.getView().getBottomInventory().addItem(ammunition);

                            tower.setAmmo(tower.getAmmo() - amount);
                            break;
                        case SWAP_WITH_CURSOR:
                            if (!e.getWhoClicked().getItemOnCursor().isSimilar(tower.getAmmunitionItem()))
                                return;
                            int amountOnCursor = e.getWhoClicked().getItemOnCursor().getAmount();

                            if (tower.getMaxAmmo() > 0 && tower.getAmmo() + amountOnCursor > tower.getMaxAmmo()) {
                                amountOnCursor -= tower.getMaxAmmo() - tower.getAmmo();
                                tower.setAmmo(tower.getMaxAmmo());
                            } else {
                                tower.setAmmo(tower.getAmmo() + amountOnCursor);
                                amountOnCursor = 0;
                            }

                            if (amountOnCursor == 0)
                                e.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                            else
                                e.getWhoClicked().getItemOnCursor().setAmount(amountOnCursor);

                            break;
                        case PICKUP_HALF:

                            TargetType[] types = TargetType.values();

                            TargetType type = tower.getTargetType();

                            for (int i = 0; i < types.length; i++) {

                                if (type == types[i]) {
                                    try {
                                        type = types[i + 1];
                                    } catch (Exception ex) {
                                        type = types[0];
                                    }
                                    break;
                                }

                            }

                            tower.setTargetType(type);

                            break;
                        default:
                            return;
                    }
                    break;
                case 4: // Clicking ride saddle

                    if (!e.getWhoClicked().hasPermission("defencetowers.ride"))
                        return;

                    tower.setOperator((Player) e.getWhoClicked());
                    tower.displayShootCooldown();

                    e.getWhoClicked().closeInventory();

                    break;
                default:
                    break;
            }

        } else { // Bottom Inventory clicks

            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {

                if (!e.getCurrentItem().isSimilar(tower.getAmmunitionItem())) return;

                int clickedAmount = e.getCurrentItem().getAmount(), towerAmount = clickedAmount;

                if (tower.getMaxAmmo() > 0 && tower.getAmmo() + clickedAmount > tower.getMaxAmmo()) {
                    towerAmount = tower.getMaxAmmo() - tower.getAmmo();
                    clickedAmount -= towerAmount;
                } else clickedAmount = 0;

                e.getCurrentItem().setAmount(clickedAmount);
                tower.setAmmo(tower.getAmmo() + towerAmount);
            } else {
                e.setCancelled(false);
            }

        }

    }

    @EventHandler
    public void onDismountTurret(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        if (!e.getDismounted().getPersistentDataContainer().has(main.getKeys().turretStand, PersistentDataType.STRING))
            return;

        Tower tower = main.getTower(e.getDismounted());
        tower.kickOperator();

    }

    @EventHandler
    public void onDragInTower(InventoryDragEvent e) {

        Tower tower = getInventoriesTower(e.getView().getTopInventory());
        if (tower == null)
            return;

        for (int slot : e.getRawSlots()) {
            if (slot < e.getView().getTopInventory().getSize() - 1) {
                e.setCancelled(true);
                return;
            }
        }

    }

    public Tower getInventoriesTower(Inventory inventory) {
        for (Tower towers : main.getTowers()) {
            if (towers.getInventory().equals(inventory))
                return towers;
        }
        return null;
    }

}
