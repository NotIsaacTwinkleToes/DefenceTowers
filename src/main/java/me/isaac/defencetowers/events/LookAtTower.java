package me.isaac.defencetowers.events;

import me.isaac.defencetowers.DefenceTowersMain;
import me.isaac.defencetowers.tower.Tower;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;

public class LookAtTower implements Listener {

    public Map<Player, Tower> lookingAtTower = new HashMap<>();
    DefenceTowersMain main;

    public LookAtTower(DefenceTowersMain main) {
        this.main = main;
    }

    @EventHandler
    public void onLookAtTower(PlayerMoveEvent e) {

        boolean atTower = true;

        RayTraceResult rayTraceResult = e.getPlayer().getWorld().rayTraceEntities(e.getPlayer().getEyeLocation().add(e.getPlayer().getLocation().getDirection().multiply(2)), e.getPlayer().getLocation().getDirection(), 5, 1);

        if (rayTraceResult == null) {
            if (lookingAtTower.containsKey(e.getPlayer())) {
                lookingAtTower.remove(e.getPlayer());
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
            }
            return;
        }
        if (rayTraceResult.getHitEntity() == null) atTower = false;

        Entity entity = rayTraceResult.getHitEntity();

        if (!e.getPlayer().hasLineOfSight(entity)) atTower = false;

        Tower tower;

        try {
            tower = main.getTower(entity);
        } catch (IllegalArgumentException ignored) {
            return;
        }

        if (!tower.getTowerOptions().isUsingHealth()) return;

        if (atTower) {
            lookingAtTower.put(e.getPlayer(), tower);
        } else if (lookingAtTower.containsKey(e.getPlayer())) {
            lookingAtTower.remove(e.getPlayer());
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
        }

    }

}
