package org.shosancold.datapacksPlus;


import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;


public class DatapacksPlusBootstrapper implements PluginBootstrap {


    private Color getColorFromString(String input) {
        if (input == null) return Color.WHITE;
        return switch (input.toLowerCase()) {
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            case "yellow" -> Color.YELLOW;
            case "aqua" -> Color.AQUA;
            case "purple" -> Color.PURPLE;
            case "orange" -> Color.ORANGE;
            case "black" -> Color.BLACK;
            default -> Color.WHITE;
        };
    }
    private static DatapacksPlus instance;

    private double resolveScore(String name, String objective) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = board.getObjective(objective);
        if (obj == null) return 0;

        return obj.getScore(name).getScore();
    }


    private static String replaceScores(String input) {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9_]+)\\?([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(input);

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        StringBuffer out = new StringBuffer();

        while (matcher.find()) {
            String name = matcher.group(1);
            String objectiveName = matcher.group(2);

            Objective obj = board.getObjective(objectiveName);
            double value = 0;

            if (obj != null) {
                value = obj.getScore(name).getScore();
            }

            matcher.appendReplacement(out, Double.toString(value));
        }

        matcher.appendTail(out);
        return out.toString();
    }


    @Override
    public void bootstrap(BootstrapContext context) {

        CommandAPI.onLoad(new CommandAPIPaperConfig(context)

        );



        new CommandAPICommand("waypointset")
                .withArguments(new LocationArgument("pos"))
                .withArguments(new EntitySelectorArgument.ManyPlayers("targets"))
                .withArguments(new StringArgument("tag"))
                .withOptionalArguments(new StringArgument("color"))
                .withOptionalArguments(new StringArgument("icon_id")) // Reference your resource pack style
                .executes((sender, args) -> {
                    Location pos = (Location) args.get("pos");
                    Collection<Player> targets = (Collection<Player>) args.get("targets");

                    String colorStr = (String) args.get("color");
                    Color color = getColorFromString(colorStr);

                    String tag = (String) args.get("tag");

                    String iconId = (String) args.get("icon_id");


                    // Create the anchor (ArmorStand is a LivingEntity, so it supports Waypoint API)
                    ArmorStand waypoint = (ArmorStand) pos.getWorld().spawnEntity(pos, EntityType.ARMOR_STAND);
                    waypoint.setMarker(true);
                    waypoint.setInvisible(true);
                    waypoint.setPersistent(false);
                    waypoint.addScoreboardTag(Objects.requireNonNull(tag));
                    waypoint.addScoreboardTag("datapacksplus_waypoint");

                    waypoint.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE).setBaseValue(50000.0);

                    waypoint.setWaypointColor(color);
                    if (iconId != null) {
                        waypoint.setWaypointStyle(NamespacedKey.minecraft(iconId));
                    }

                    pos.getWorld().getPlayers().forEach(p -> p.hideEntity(JavaPlugin.getPlugin(DatapacksPlus.class), waypoint));
                    for (Player target : targets) {
                        target.showEntity(JavaPlugin.getPlugin(DatapacksPlus.class), waypoint);
                    }
                })
                .register();

        new CommandAPICommand("waypointsclear")
                .withArguments(new EntitySelectorArgument.ManyEntities("waypoints"))
                .executes((sender, args) -> {
                    Collection<org.bukkit.entity.Entity> entities = (Collection<org.bukkit.entity.Entity>) args.get("waypoints");
                    int count = 0;
                    for (org.bukkit.entity.Entity e : entities) {
                        if (e.getScoreboardTags().contains("datapacksplus_waypoint")) {
                            e.remove();
                            count++;
                        }
                    }
                    sender.sendMessage("Removed " + count + " waypoints.");
                })
                .register();

        new CommandAPICommand("slot")
                .withPermission("datapacksplus.slot") // Require this permission to use the command
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .withArguments(new IntegerArgument("slot", 0, 8).setOptional(true))
                .executes((sender, args) -> {
                    int selectedslot;
                    if (args.get("slot") != null) {
                        Player player = (Player) args.get("target");
                        int slot = (int) args.get("slot");
                        Objects.requireNonNull(player).getInventory().setHeldItemSlot(slot);

                        return 0;
                    } else {
                        Player player = (Player) args.get("target");
                        selectedslot = Objects.requireNonNull(player).getInventory().getHeldItemSlot();
                    }

                    return selectedslot;
                })
                .register();



        new CommandAPICommand("tagremoveall")
                .withPermission("datapacksplus.tagremoveall") // Require this permission to use the command
                .withArguments(new EntitySelectorArgument.ManyEntities("targets"))
                .withOptionalArguments(new GreedyStringArgument("ex_tags"))
                .executes((sender, args) -> {
                    @SuppressWarnings("unchecked")
                    Collection<Entity> entities = (Collection<Entity>) args.get("targets");
                    String ex_tags = Objects.toString(args.get("ex_tags"), "");
                    Set<String> excludedTags = new HashSet<>(Arrays.asList(ex_tags.split(" ")));

                    assert entities != null;
                    for (Entity entity : entities) {
                        if (entity instanceof Player player) {
                            Set<String> tags = new HashSet<>(player.getScoreboardTags());

                            for (String tag : tags) {
                                if (!excludedTags.contains(tag)) {
                                    player.removeScoreboardTag(tag);
                                }
                            }
                        }
                    }
                })
                .register();

        // /heal <target> <amount>
        new CommandAPICommand("heal")
                .withPermission("datapacksplus.heal") // Require this permission to use the command
                .withArguments(new EntitySelectorArgument.ManyEntities("targets")) // Select entities
                .withArguments(new DoubleArgument("amount")) // Healing amount
                .executes((sender, args) -> {
                    Collection<Entity> entities = (Collection<Entity>) args.get("targets");
                    double amount = (double) args.get("amount");

                    if (entities.isEmpty()) {
                        sender.sendMessage("No entities selected.");
                        return;
                    }

                    int healedEntities = 0;
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity livingEntity) {
                            double newHealth = Math.min(livingEntity.getHealth() + amount, livingEntity.getAttribute(Attribute.MAX_HEALTH).getValue());
                            livingEntity.setHealth(newHealth);
                            healedEntities++;
                        }
                    }

                })
                .register();
        new CommandAPICommand("food")
                .withPermission("datapacksplus.food")
                .withArguments(new EntitySelectorArgument.ManyPlayers("targets"))
                .withArguments(new IntegerArgument("amount", 0, 20))
                .executes((sender, args) -> {
                    Collection<Player> targets = (Collection<Player>) args.get("targets");
                    int amount = (int) args.get("amount");

                    for (Player player : targets) {
                        player.setFoodLevel(amount);
                    }

                    return targets.size();
                })
                .register();
        new CommandAPICommand("saturation")
                .withPermission("datapacksplus.saturation")
                .withArguments(new EntitySelectorArgument.ManyPlayers("targets"))
                .withArguments(new IntegerArgument("amount", 0, 20))
                .executes((sender, args) -> {
                    Collection<Player> targets = (Collection<Player>) args.get("targets");
                    int amount = (int) args.get("amount");

                    for (Player player : targets) {
                        player.setSaturation(amount);
                    }

                    return targets.size();
                })
                .register();

        new CommandAPICommand("test")
                .withPermission("datapacksplus.test")
                .withArguments(new IntegerArgument("amount", 0, 20))
                .executes((sender, args) -> {
                    int amount = (int) args.get("amount");

                    sender.sendMessage("BALLS: " + amount);
                })
                .register();

        new CommandTree("serverboard")
                .then(new LiteralArgument("show")
                        .then(new StringArgument("board_name")
                                // Use the singleton instance to access the config dynamically
                                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                                    var config = DatapacksPlus.getInstance().getConfig();
                                    var section = config.getConfigurationSection("scoreboards");

                                    if (section == null) {
                                        return new String[0];
                                    }

                                    return section.getKeys(false).toArray(new String[0]);
                                }))
                                .then(new BooleanArgument("bool_show")
                                        .then(new EntitySelectorArgument.ManyPlayers("targets")
                                                .executes((sender, args) -> {
                                                    String boardName = (String) args.get("board_name");
                                                    boolean show = (boolean) args.get("bool_show");
                                                    Collection<Player> targets = (Collection<Player>) args.get("targets");

                                                    for (Player target : targets) {
                                                        if (show) {

                                                            DatapacksPlus.getInstance().createScoreboard(target, boardName);
                                                        } else {
                                                            DatapacksPlus.getInstance().removeScoreboard(target);
                                                        }
                                                    }
                                                })
                                        )
                                )
                        )
                )
                .then(new LiteralArgument("update")
                        .then(new EntitySelectorArgument.ManyPlayers("targets")
                                .executes((sender, args) -> {
                                    Collection<Player> targets = (Collection<Player>) args.get("targets");
                                    for (Player target : targets) {
                                        DatapacksPlus.getInstance().updateScoreboard(target);
                                    }
                                })
                        )
                )
                .then(new LiteralArgument("configreload")
                        .executes(((commandSender, commandArguments) -> {
                            DatapacksPlus.getInstance().ConfigReload();
                            commandSender.sendMessage("§aScoreboard configuration reloaded!");
                        })))

                .then(new LiteralArgument("list")
                        .executes(((commandSender, commandArguments) -> {
                            var section = DatapacksPlus.getInstance().getConfig().getConfigurationSection("scoreboards");
                            if (section == null){
                                commandSender.sendMessage("Available Scoreboards:");
                                for (String key : section.getKeys(false)){
                                    commandSender.sendMessage("- " + key);
                                }
                            }
                        })))

                .register();




        new CommandAPICommand("enderchest")
                .withAliases("ec", "echest")
                .withPermission("datapacksplus.enderchest")
                .withPermission(CommandPermission.OP)
                .withArguments(new EntitySelectorArgument.ManyPlayers("viewer"))
                .executes((CommandSender executor, CommandArguments args) -> {
                    @SuppressWarnings("unchecked")
                    Collection<Player> entities =
                            (Collection<Player>) args.get("viewer");

                    for (Player inventoryViewer : entities) {
                        if (inventoryViewer.isOnline()) {
                            inventoryViewer.openInventory(inventoryViewer.getEnderChest());
                        }
                    }
                    return 0;
                })
                .register();

        // ===========================================================================================
        //                                     Other enderchest
        // ===========================================================================================

        new CommandAPICommand("otherenderchest")
                .withAliases("otherec", "otherechest")
                .withPermission("datapacksplus.otherenderchest")
                .withPermission(CommandPermission.OP)
                .withArguments(new EntitySelectorArgument.OnePlayer("enderchest_player"))
                .executes((CommandSender executor, CommandArguments args) -> {
                    Player ecPlayer = (Player) args.get("enderchest_player");
                    if (ecPlayer == null) {
                        return 0;
                    }

                    if (executor instanceof Player player) {
                        player.openInventory(ecPlayer.getEnderChest());
                    }
                    return 0;
                })
                .register();

        //with different viewer
        new CommandAPICommand("otherenderchest")
                .withAliases("otherec", "otherechest")
                .withPermission("datapackplus.otherenderchest")
                .withPermission(CommandPermission.OP)
                .withArguments(new EntitySelectorArgument.OnePlayer("enderchest_player"))
                .withArguments(new EntitySelectorArgument.ManyPlayers("viewer"))
                .executes((CommandSender executor, CommandArguments args) -> {
                    Player ecPlayer = (Player) args.get("enderchest_player");

                    @SuppressWarnings("unchecked")
                    Collection<Player> entities =
                            (Collection<Player>) args.get("viewer");

                    if (ecPlayer == null) {
                        return 0;
                    }

                    for (Player inventoryViewer : entities) {
                        if (inventoryViewer.isOnline()) {
                            inventoryViewer.openInventory(ecPlayer.getEnderChest());
                        }
                    }
                    return 0;
                })
                .register();

        // ===========================================================================================
        //                                      Close Inventory
        // ===========================================================================================

        new CommandAPICommand("closeinventory")
                .withPermission("datapackplus.closeinventory")
                .withPermission(CommandPermission.OP)
                .executes((CommandSender executor, CommandArguments args) -> {
                    if (executor instanceof Player player) {
                        player.closeInventory();
                    }
                    return 0;
                })
                .register();

        new CommandAPICommand("closeinventory")
                .withPermission("datapacksplus.closeinventory")
                .withPermission(CommandPermission.OP)
                .withArguments(new EntitySelectorArgument.ManyPlayers("viewer"))
                .executes((CommandSender executor, CommandArguments args) -> {
                    @SuppressWarnings("unchecked")
                    Collection<Player> entities =
                            (Collection<Player>) args.get("viewer");

                    for (Player inventoryViewer : entities) {
                        if (inventoryViewer.isOnline()) {
                            inventoryViewer.closeInventory();
                        } else {
                        }
                    }
                    return 0;
                })
                .register();






        new CommandAPICommand("saturation")
                .withPermission("datapacksplus.saturation")
                .withArguments(new EntitySelectorArgument.ManyPlayers("targets"))
                .withArguments(new IntegerArgument("amount", 0, 20))
                .executes((sender, args) -> {
                    Collection<Player> targets = (Collection<Player>) args.get("targets");
                    int amount = (int) args.get("amount");

                    for (Player player : targets) {
                        player.setSaturation((float) amount);
                    }

                    return targets.size();
                })
                .register();

        // /motion @targets <x> <y> <z>
        new CommandAPICommand("motion")
                .withPermission("datapacksplus.motion") // Require this permission to use the command
                .withArguments(new EntitySelectorArgument.ManyEntities("targets"))
                .withArguments(new DoubleArgument("x"))
                .withArguments(new DoubleArgument("y"))
                .withArguments(new DoubleArgument("z"))
                .executes((sender, args) -> {
                    Collection<Entity> entities = (Collection<Entity>) args.get("targets");
                    double x = (double) args.get("x");
                    double y = (double) args.get("y");
                    double z = (double) args.get("z");

                    Vector velocity = new Vector(x, y, z);
                    for (Entity entity : entities) {
                        entity.setVelocity(velocity);
                    }
                })
                .register();

        // /relativemotion @targets <horizontal> <vertical> <forward> <ignore_vertical_rotation>
        new CommandAPICommand("relativemotion")
                .withPermission("datapacksplus.relativemotion") // Require this permission to use the command
                .withArguments(new EntitySelectorArgument.ManyEntities("targets"))
                .withArguments(new DoubleArgument("horizontal")) // Left-right movement
                .withArguments(new DoubleArgument("vertical"))   // Up-down movement
                .withArguments(new DoubleArgument("forward"))    // Forward-backward movement
                .withArguments(new BooleanArgument("ignore_vertical_rotation")) // If true, ignores pitch
                .executes((sender, args) -> {
                    Collection<Entity> entities = (Collection<Entity>) args.get("targets");
                    double horizontal = (double) args.get("horizontal");
                    double vertical = (double) args.get("vertical");
                    double forward = (double) args.get("forward");
                    boolean ignoreVerticalRotation = (boolean) args.get("ignore_vertical_rotation");

                    if (entities.isEmpty()) {
                        return;
                    }

                    // Use the first entity's direction if sender is not a player
                    Entity referenceEntity;
                    if (sender instanceof Player player) {
                        referenceEntity = player; // Use player's facing direction
                    } else {
                        referenceEntity = entities.iterator().next(); // Use first target's facing direction
                    }

                    Location loc = referenceEntity.getLocation();
                    Vector direction = loc.getDirection().normalize(); // Forward direction

                    if (ignoreVerticalRotation) {
                        // Flatten direction vector to ignore pitch (vertical rotation)
                        direction.setY(0);
                        direction.normalize(); // Renormalize to keep magnitude
                    }

                    Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize(); // Right direction
                    Vector up = new Vector(0, 1, 0); // Always upwards

                    // Calculate relative velocity
                    Vector velocity = direction.multiply(forward)  // Move forward/backward
                            .add(right.multiply(horizontal))       // Move left/right
                            .add(up.multiply(vertical));           // Move up/down

                    for (Entity entity : entities) {
                        entity.setVelocity(entity.getVelocity().add(velocity)); // Append to existing motion
                    }

                })
                .register();

        new CommandAPICommand("repeat")
                .withPermission("datapacksplus.repeat")
                .withArguments(new IntegerArgument("amount", 1))
                .withArguments(new IntegerArgument("delay", 0))
                .withArguments(new LiteralArgument("run"))
                .withArguments(new GreedyStringArgument("command"))
                .executes((sender, args) -> {

                    int amount = (int) args.get("amount");
                    int delay = (int) args.get("delay");
                    String command = (String) args.get("command");

                    if (amount > 10000) {
                        sender.sendMessage("§cToo many repetitions.");
                        return;
                    }

                    new BukkitRunnable() {
                        int runs = 0;

                        @Override
                        public void run() {
                            if (runs >= amount) {
                                cancel();
                                return;
                            }

                            Bukkit.dispatchCommand(sender, command);
                            runs++;
                        }
                    }.runTaskTimer(DatapacksPlus.getInstance(), delay, delay);

                })
                .register();



        new CommandAPICommand("eval")
                .withPermission("datapacksplus.eval")
                .withArguments(new DoubleArgument("scale"))
                .withArguments(new GreedyStringArgument("expression"))
                .executes((sender, args) -> {

                    double scale = (double) args.get("scale");
                    String rawExpr = (String) args.get("expression");

                    try {
                        String expr = replaceScores(rawExpr);

                        Expression e = new ExpressionBuilder(expr)
                                .function(new Function("clamp", 3) {
                                    @Override
                                    public double apply(double... a) {
                                        return Math.max(a[1], Math.min(a[2], a[0]));
                                    }
                                })
                                .build();

                        double result = e.evaluate() * scale;

                        sender.sendMessage("§7Expression: §f" + expr);
                        sender.sendMessage("§7Result: §a" + result);
                        return (int) result;

                    } catch (Exception ex) {
                        sender.sendMessage("§cInvalid expression.");
                        return -1;
                    }

                })
                .register();

        // scoreboard set players @target x 10
        // /motionscore @targets <x> <y> <z>
        new CommandAPICommand("motionscore")
                .withPermission("datapacksplus.motion") // Require this permission to use the command
                .withArguments(new EntitySelectorArgument.ManyEntities("targets"))
                .withArguments(new ObjectiveArgument("xObjective"))
                .withArguments(new ObjectiveArgument("yObjective"))
                .withArguments(new ObjectiveArgument("zObjective"))
                .executes((sender, args) -> {
                    Collection<Entity> entities = (Collection<Entity>) args.get("targets");

                    Objective xObjective = (Objective) args.get("xObjective");
                    Objective yObjective = (Objective) args.get("yObjective");
                    Objective zObjective = (Objective) args.get("zObjective");


                    for (Entity entity : entities) {
                        if (entity instanceof Player player) {
                            double x = xObjective.getScore(player).getScore();
                            double y = yObjective.getScore(player).getScore();
                            double z = zObjective.getScore(player).getScore();

                            player.setVelocity(new Vector(x, y, z));
                        }
                    }
                })
                .register();
    }

    public void registerCommands() {
        // /slot @target -> Returns current slot of player
        // /slot @target <slot> -> sets slot of player

    }
}
