package org.shosancold.datapacksPlus;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.Vector;

import java.util.*;

public class DatapacksPlus extends JavaPlugin {

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));


        // /slot @target -> Returns current slot of player
        // /slot @target <slot> -> sets slot of player
        new CommandAPICommand("slot")
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .withArguments(new IntegerArgument("slot", 0, 8).setOptional(true))
                .executes((sender, args) -> {
                    int selectedslot;
                    if (args.get("slot") != null) {
                        Player player = (Player) args.get("target");
                        int slot = (int) args.get("slot");
                        Objects.requireNonNull(player).getInventory().setHeldItemSlot(slot);

                        sender.sendMessage("Set slot " + slot + " for player " + player.getName());
                        return 0;
                    } else {
                        Player player = (Player) args.get("target");
                        selectedslot = Objects.requireNonNull(player).getInventory().getHeldItemSlot();
                        ;


                    }

                    return selectedslot;
                })
                .register();


        new CommandAPICommand("slot")
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .withArguments(new IntegerArgument("slot", 0, 8))
                .executes((sender, args) -> {
                    Player player = (Player) args.get("target");
                    int slot = (int) args.get("slot");
                    Objects.requireNonNull(player).getInventory().setHeldItemSlot(slot);
                    sender.sendMessage("Set slot " + slot + " for player " + player.getName());
                })
                .register();
        new CommandAPICommand("setnbt")
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .withArguments(new StringArgument("nbt"))
                .executes((sender, args) -> {

                    Player player = (Player) args.get("target");
                    String nbt = (String) args.get("nbt");

                    player.set


                })
                .register();



        new CommandAPICommand("tagremoveall")
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



        // /motion @targets <x> <y> <z>
        new CommandAPICommand("motion")
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
                    sender.sendMessage("Applied velocity to entities");
                })
                .register();

        // scoreboard set players @target x 10
        // /motionscore @targets <x> <y> <z>
        new CommandAPICommand("motionscore")
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
                    sender.sendMessage("Applied velocity to entities");
                })
                .register();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

}

