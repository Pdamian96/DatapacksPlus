package org.shosancold.datapacksPlus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.*;

import javax.xml.crypto.Data;

public class PlayerListener implements Listener {

    private final DatapacksPlus plugin;

    public PlayerListener(DatapacksPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (!player.getScoreboardTags().contains("plugin.block.interact")) return;

        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        if (e.getClickedBlock() == null) return;

        Material type = e.getClickedBlock().getType();
        if (isInteractableBlock(type)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockDamageEvent e) {
        if (e.getPlayer().getScoreboardTags().contains("prevent.block.destroy")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getPlayer().getScoreboardTags().contains("prevent.block.place")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        // intentionally empty
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity target = e.getEntity();


        if (damager.getScoreboardTags().contains("prevent.attack.all")) {
            e.setCancelled(!(target instanceof Interaction));
        }

        if (damager.getScoreboardTags().contains("prevent.attack.-player")) {
            if (target instanceof Interaction) {
                e.setCancelled(false);
            } else {
                e.setCancelled(!(target instanceof Player));
            }
        }

        if (damager.getScoreboardTags().contains("prevent.attack.player")) {
            if (target instanceof Player) {
                e.setCancelled(true);
            }
        }

        if (damager.getScoreboardTags().contains("prevent.attack.armorstand")) {
            if (target instanceof ArmorStand) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerBlockSpecificAttack(HangingBreakByEntityEvent e) {
        Entity remover = e.getRemover();
        Hanging entity = e.getEntity();


        if (remover.getScoreboardTags().contains("prevent.attack.painting")) {
            if (entity instanceof Painting) {
                e.setCancelled(true);
            }
        }

        if (remover.getScoreboardTags().contains("prevent.attack.itemframe")) {
            if (entity instanceof ItemFrame || entity instanceof GlowItemFrame) {
                e.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onPlayerLeavingGui(InventoryCloseEvent e) {
        if (e.getInventory().getType() != InventoryType.ENDER_CHEST) return;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("plugin.closegui");
        if (objective == null) return;

        Score score = objective.getScore(e.getPlayer().getName());
        score.setScore(score.getScore() + 1);
    }

    @EventHandler
    public void onPlayerAccessingGui(InventoryOpenEvent e) {
        if (e.getInventory().getType() != InventoryType.ENDER_CHEST) return;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("plugin.opengui");
        if (objective == null) return;

        Score score = objective.getScore(e.getPlayer().getName());
        score.setScore(score.getScore() + 1);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getScoreboardTags().contains("prevent.drop")) {
            e.setCancelled(true);
        }
    }

    private boolean isInteractableBlock(Material type) {
        String name = type.name();

        return name.endsWith("BUTTON") ||
                name.endsWith("TRAPDOOR") ||
                name.endsWith("GATE") ||
                name.endsWith("DOOR") ||
                name.endsWith("BOX") ||
                name.endsWith("POT") ||
                name.endsWith("DETECTOR") ||
                name.endsWith("SIGN") ||
                name.equals("CHEST") ||
                name.contains("TRAPPED") ||
                name.contains("FURNACE") ||
                name.contains("SMOKER") ||
                name.contains("HOPPER") ||
                name.contains("DISPENSER") ||
                name.contains("DROPPER") ||
                name.contains("CRAFTER") ||
                name.contains("BARREL") ||
                name.contains("NOTE") ||
                name.contains("LEVER") ||
                name.contains("STONECUTTER") ||
                name.contains("LOOM") ||
                name.contains("ANVIL") ||
                name.contains("GRINDSTONE") ||
                name.contains("TABLE") ||
                name.contains("BEACON") ||
                name.contains("COMPARATOR") ||
                name.contains("REPEATER") ||
                name.contains("BREWING");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String defaultBoard = plugin.getConfig().getString("default-scoreboard", "default");
        plugin.createScoreboard(e.getPlayer(), defaultBoard);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.removeScoreboard(e.getPlayer());
    }
}