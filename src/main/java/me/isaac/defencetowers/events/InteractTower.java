package me.isaac.defencetowers.events;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.Tower;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;

public class InteractTower implements Listener {

    DefenceTowersMain main;

    public InteractTower(DefenceTowersMain main) {
        this.main = main;
    }

    // Add clicking on towers with arrows will add ammo
    @EventHandler
    public void onClickTower(PlayerInteractAtEntityEvent e) {

        if (!(e.getRightClicked() instanceof ArmorStand))
            return;
        if (!e.getRightClicked().getPersistentDataContainer().has(main.getKeys().turretStand,
                PersistentDataType.STRING))
            return;
        e.setCancelled(true);

        Tower tower = main.towerLocations.get(e.getRightClicked());

        if (!e.getPlayer().hasPermission("defencetowers.bypassblacklist")
                && !tower.getBlacklistedPlayers().contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(ChatColor.DARK_RED + "Tower Locked"));
            return;
        }

        if (e.getPlayer().isSneaking()) {
            tower.remove(e.getPlayer().getGameMode() == GameMode.SURVIVAL);
            if (main.getInteractTowerInstance().editingTower.containsKey(e.getPlayer()))
                main.getInteractTowerInstance().editingTower.remove(e.getPlayer());
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
    public void onPlayerShootTurret(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND)
            return;
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        if (e.getPlayer().getVehicle() == null)
            return;
        if (!(e.getPlayer().getVehicle() instanceof ArmorStand))
            return;
        if (!e.getPlayer().getVehicle().getPersistentDataContainer().has(main.getKeys().turretStand,
                PersistentDataType.STRING))
            return;

        Tower tower = main.towerLocations.get(e.getPlayer().getVehicle());

        tower.shoot(e.getPlayer().getLocation().getDirection());

    }

    public HashMap<Player, Tower> editingTower = new HashMap<>();

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {

        Tower tower = getInventoriesTower(e.getView().getTopInventory());
        int amount;

        if (tower == null)
            return;
        e.setCancelled(true);

        if (e.getClickedInventory().equals(e.getView().getTopInventory())) { // Top inventory clicks

            amount = tower.getAmmo() > 64 ? 64 : tower.getAmmo();

            switch (e.getSlot()) {
                case 1: // Clicking radius toggle
                    tower.displayRange(!tower.isDisplaying());
                    break;
                case 2: // Clicking blacklist
                    if (editingTower.containsKey(e.getWhoClicked())) {
                        editingTower.remove(e.getWhoClicked());
                        e.getWhoClicked().sendMessage(DefenceTowersMain.prefix + "No longer setting blacklist");
                        return;
                    }
                    editingTower.put((Player) e.getWhoClicked(), tower);
                    e.getWhoClicked()
                            .sendMessage(DefenceTowersMain.prefix + "Type a players name to add it to the blacklist");
                    e.getWhoClicked().sendMessage(
                            DefenceTowersMain.prefix + "type \"remove <player>\" to remove them from the blacklist");
                    e.getWhoClicked().sendMessage(DefenceTowersMain.prefix + "Type \"cancel\" to stop");

                    e.getWhoClicked().closeInventory();

                    break;
                case 3: // Clicking ammunition arrow
                    switch (e.getAction()) {
                        case PICKUP_ALL:
                            tower.setAmmo(tower.getAmmo() - amount);

                            ItemStack arrows = new ItemStack(Material.ARROW);
                            arrows.setAmount(amount);

                            e.getWhoClicked().setItemOnCursor(arrows);
                            break;
                        case MOVE_TO_OTHER_INVENTORY:

                            if (e.getView().getBottomInventory().firstEmpty() == -1)
                                return;

                            arrows = new ItemStack(Material.ARROW);
                            arrows.setAmount(amount);

                            e.getView().getBottomInventory().addItem(arrows);

                            tower.setAmmo(tower.getAmmo() - amount);
                            break;
                        case SWAP_WITH_CURSOR:
                            if (e.getWhoClicked().getItemOnCursor().getType() != Material.ARROW)
                                return;
                            int amountOnCursor = e.getWhoClicked().getItemOnCursor().getAmount();

                            if (tower.getAmmo() + amountOnCursor > tower.getMaxAmmo() && tower.getMaxAmmo() > 0) {
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

            switch (e.getAction()) {
                case MOVE_TO_OTHER_INVENTORY:
                    int clickedAmount = e.getCurrentItem().getAmount(), towerAmount = clickedAmount;

                    if (tower.getMaxAmmo() > 0 && tower.getAmmo() + clickedAmount > tower.getMaxAmmo()) {
                        towerAmount = tower.getMaxAmmo() - tower.getAmmo();
                        clickedAmount -= towerAmount;
                    } else clickedAmount = 0;

                    e.getCurrentItem().setAmount(clickedAmount);
                    tower.setAmmo(tower.getAmmo() + towerAmount);
                    break;
//                case PICKUP_ALL:
//                case PICKUP_HALF:
//                case PLACE_ONE:
//                case PLACE_ALL:
//                case DROP_ONE_SLOT:
//                case DROP_ALL_SLOT:
//                case DROP_ALL_CURSOR:
//                case DROP_ONE_CURSOR:
//                case CLONE_STACK:
//                case PLACE_SOME:
//                    e.setCancelled(false);
//                    break;
                default:
                    e.setCancelled(false);
                    break;
            }

        }

    }

    @EventHandler
    public void onDismountTurret(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        if (!(e.getDismounted() instanceof ArmorStand))
            return;
        if (!e.getDismounted().getPersistentDataContainer().has(main.getKeys().turretStand, PersistentDataType.STRING))
            return;

        Tower tower = main.towerLocations.get(e.getDismounted());

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
        for (Tower towers : main.towerLocations.values()) {
            if (towers.getInventory().equals(inventory))
                return towers;
        }
        return null;
    }

}
