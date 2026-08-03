package org.shosancold.datapacksPlus;

import dev.jorel.commandapi.CommandAPI;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import net.kyori.adventure.text.Component;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
;
public class DatapacksPlus extends JavaPlugin {
    private static DatapacksPlus instance;
    private static final String[] POS_OBJECTIVES = {
            "x", "y", "z",
            "yaw", "pitch",
            "vx", "vy", "vz",
            "hx", "hz",
            "speed", "hspeed",
            "ax", "ay", "az",
            "aspeed",
            "grounded", "airborne"
    };
    private final HashMap<UUID, Scoreboard> playerBoards = new HashMap<>();
    private final HashMap<UUID, String> activeBoardType = new HashMap<>();
    private boolean positionUpdaterPaused = false;
    private final Map<UUID, Vector> lastVelocity = new HashMap<>();

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        instance = this;
        getServer().getPluginManager().registerEvents(
                new PlayerListener(this),
                this
        );
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getMainScoreboard();



        if (scoreboard.getObjective("plugin.closegui") == null) {
            scoreboard.registerNewObjective(
                    "plugin.closegui",
                    Criteria.DUMMY,
                    Component.text("InventoryCloseEvent")
            );
        }
        if (scoreboard.getObjective("plugin.preAttack") == null) {
            scoreboard.registerNewObjective(
                    "plugin.preAttack",
                    Criteria.DUMMY,
                    Component.text("preAttackEvent")
            );
        }

        if (scoreboard.getObjective("plugin.opengui") == null) {
            scoreboard.registerNewObjective(
                    "plugin.opengui",
                    Criteria.DUMMY,
                    Component.text("InventoryOpenEvent")
            );
        }

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();

        String prefix = getConfig().getString("position-scoreboard.prefix", "pos_");

        for (String key : POS_OBJECTIVES) {
            String name = prefix + key;
            if (main.getObjective(name) == null) {
                main.registerNewObjective(
                        name,
                        Criteria.DUMMY,
                        Component.text(name)
                );
            }
        }
        getLogger().info("DatapacksPlus enabled");
        startPositionUpdater();

    }

    private void set(Scoreboard board, String objectiveName, Player player, double value, int scale) {
        Objective obj = board.getObjective(objectiveName);
        if (obj != null) {
            obj.getScore(player.getName()).setScore((int) Math.round(value * scale));
        }
    }

    private void set(Scoreboard board, String objectiveName, Player player, int value) {
        Objective obj = board.getObjective(objectiveName);
        if (obj != null) {
            obj.getScore(player.getName()).setScore(value);
        }
    }
    public boolean isPositionUpdaterPaused() {
        return positionUpdaterPaused;
    }
    public void setPositionUpdaterPaused(boolean paused) {
        this.positionUpdaterPaused = paused;
    }
    private void startPositionUpdater() {
        if (!getConfig().getBoolean("position-scoreboard.enabled", true)) return;

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        String prefix = getConfig().getString("position-scoreboard.prefix", "pos_");

        int posScale = getConfig().getInt("position-scoreboard.scale.position", 1);
        int rotScale = getConfig().getInt("position-scoreboard.scale.rotation", 1);
        int velScale = getConfig().getInt("position-scoreboard.scale.velocity", 100);
        int accScale = getConfig().getInt("position-scoreboard.scale.acceleration", 100);

        boolean velocityEnabled = getConfig().getBoolean("position-scoreboard.velocity.enabled", true);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (positionUpdaterPaused) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location l = player.getLocation();
                    Vector v = player.getVelocity();

                    // --- Position / rotation ---
                    set(main, prefix + "x", player, l.getX(), posScale);
                    set(main, prefix + "y", player, l.getY(), posScale);
                    set(main, prefix + "z", player, l.getZ(), posScale);

                    set(main, prefix + "yaw", player, l.getYaw(), rotScale);
                    set(main, prefix + "pitch", player, l.getPitch(), rotScale);

                    // --- Grounded state ---
                    boolean grounded = player.isOnGround();
                    set(main, prefix + "grounded", player, grounded ? 1 : 0);
                    set(main, prefix + "airborne", player, grounded ? 0 : 1);

                    if (!velocityEnabled) continue;

                    // --- Velocity ---
                    set(main, prefix + "vx", player, v.getX(), velScale);
                    set(main, prefix + "vy", player, v.getY(), velScale);
                    set(main, prefix + "vz", player, v.getZ(), velScale);

                    set(main, prefix + "hx", player, v.getX(), velScale);
                    set(main, prefix + "hz", player, v.getZ(), velScale);

                    double speed = v.length();
                    double hSpeed = Math.sqrt(v.getX() * v.getX() + v.getZ() * v.getZ());

                    set(main, prefix + "speed", player, speed, velScale);
                    set(main, prefix + "hspeed", player, hSpeed, velScale);

                    // --- Acceleration ---
                    Vector last = lastVelocity.get(player.getUniqueId());
                    if (last != null) {
                        Vector a = v.clone().subtract(last);

                        set(main, prefix + "ax", player, a.getX(), accScale);
                        set(main, prefix + "ay", player, a.getY(), accScale);
                        set(main, prefix + "az", player, a.getZ(), accScale);
                        set(main, prefix + "aspeed", player, a.length(), accScale);
                    }

                    lastVelocity.put(player.getUniqueId(), v.clone());
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }



    public void ConfigReload() {

        super.reloadConfig();


        for (Player player : Bukkit.getOnlinePlayers()) {
            String currentBoard = activeBoardType.getOrDefault(player.getUniqueId(),
                    getConfig().getString("default-scoreboard", "default"));

            createScoreboard(player, currentBoard);
        }

        getLogger().info("Config reloaded!");
    }

    private void applyNumberFormat(Objective obj, String boardName) {
        String path = "scoreboards." + boardName + ".number-format";
        String type = getConfig().getString(path + ".type", "default").toLowerCase();

        if (type.equals("blank")) {
            obj.numberFormat(NumberFormat.blank());
        } else if (type.equals("fixed")) {
            String value = getConfig().getString(path + ".value", "");
            // Converts your color codes (&) into a Component for the fixed format
            obj.numberFormat(NumberFormat.fixed(Component.text(color(value))));
        } else {
            // null resets it to the vanilla red numbers
            obj.numberFormat(null);
        }
    }
    public void createScoreboard(Player player, String boardName) {

        String path = "scoreboards." + boardName;
        if (getConfig().getConfigurationSection(path) == null) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("sidebar", Criteria.DUMMY,
                color(getConfig().getString(path + ".title")));
        applyNumberFormat(obj, boardName);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = getConfig().getStringList(path + ".lines");

        int score = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String entry = ChatColor.values()[i].toString();
            Team team = board.registerNewTeam("line_" + i);
            team.addEntry(entry);

            obj.getScore(entry).setScore(score--);
            setTeamLine(team, player, lines.get(i));
        }

        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
        activeBoardType.put(player.getUniqueId(), boardName); // Save the type!
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        String boardName = activeBoardType.get(player.getUniqueId());

        if (board == null || boardName == null) return;

        List<String> lines = getConfig().getStringList("scoreboards." + boardName + ".lines");

        for (int i = 0; i < lines.size(); i++) {
            Team team = board.getTeam("line_" + i);
            if (team != null) {
                setTeamLine(team, player, lines.get(i));
            }
        }
    }

    private static final Pattern OBJECTIVE_PATTERN = Pattern.compile("\\{objective\\.(.*?)\\}");
    private void setTeamLine(Team team, Player player, String template) {
        String line = template
                .replace("{player}", player.getName())
                .replace("{ping}", String.valueOf(player.getPing()))
                .replace("{health}", String.valueOf((int) player.getHealth()));

        Matcher matcher = OBJECTIVE_PATTERN.matcher(line);
        StringBuffer sb = new StringBuffer();
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        while (matcher.find()) {
            String objectiveName = matcher.group(1); // This gets "coins" from {objective.coins}
            Objective obj = board.getObjective(objectiveName);

            int score = 0;
            if (obj != null) {
                score = obj.getScore(player.getName()).getScore();
            }

            // Replace the tag with the actual score
            matcher.appendReplacement(sb, String.valueOf(score));
        }
        matcher.appendTail(sb);

        team.setPrefix(color(sb.toString()));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    public void removeScoreboard(Player player) {
        playerBoards.remove(player.getUniqueId());
        activeBoardType.remove(player.getUniqueId()); // Clear the type
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public static DatapacksPlus getInstance() {
        return instance;    }
    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

}
