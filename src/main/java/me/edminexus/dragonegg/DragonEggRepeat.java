package me.edminexus.dragonegg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.DragonBattle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DragonEggRepeat extends JavaPlugin implements Listener {

    private int dragonKills;
    private int eggsDistributed;
    private int maxEggs;
    private boolean initialized;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dragonKills = getConfig().getInt("dragon-kills", 0);
        eggsDistributed = getConfig().getInt("eggs-distributed", 0);
        maxEggs = getConfig().getInt("max-eggs", 20);
        initialized = getConfig().getBoolean("initialized", false);

        // FIRST PLUGIN INITIALIZATION ONLY
        if (!initialized) {
            World endWorld = Bukkit.getWorlds().stream()
                    .filter(w -> w.getEnvironment() == World.Environment.THE_END)
                    .findFirst()
                    .orElse(null);

            if (endWorld != null) {
                DragonBattle battle = endWorld.getEnderDragonBattle();

                // If first dragon kill already happened
                if (battle != null && battle.hasBeenPreviouslyKilled()) {
                    eggsDistributed = 1;
                    dragonKills = 1;
                }
            }

            getConfig().set("dragon-kills", dragonKills);
            getConfig().set("eggs-distributed", eggsDistributed);
            getConfig().set("initialized", true);
            saveConfig();
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // =========================
    // DRAGON DEATH LOGIC
    // =========================
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;
        EnderDragon dragon = (EnderDragon) event.getEntity();

        World world = dragon.getWorld();
        if (world.getEnvironment() != World.Environment.THE_END) return;

        // LEGIT DRAGON CHECK
        DragonBattle battle = dragon.getDragonBattle();
        if (battle == null) return;

        // FIRST EVER DRAGON KILL
        if (!battle.hasBeenPreviouslyKilled()) {
            dragonKills = 1;

            if (eggsDistributed == 0) {
                eggsDistributed = 1;
            }

            getConfig().set("dragon-kills", dragonKills);
            getConfig().set("eggs-distributed", eggsDistributed);
            saveConfig();
            return;
        }

        // ALL SUBSEQUENT LEGIT KILLS
        dragonKills++;
        getConfig().set("dragon-kills", dragonKills);

        if (eggsDistributed == 0) {
            eggsDistributed = 1;
            getConfig().set("eggs-distributed", eggsDistributed);
            saveConfig();
            return;
        }

        if (eggsDistributed < maxEggs) {
            for (int y = 65; y <= 80; y++) {
                Block block = world.getBlockAt(0, y, 0);
                if (block.getType() == Material.AIR) {
                    block.setType(Material.DRAGON_EGG);
                    eggsDistributed++;
                    getConfig().set("eggs-distributed", eggsDistributed);
                    break;
                }
            }
        }

        saveConfig();
    }

    // =========================
    // COMMANDS
    // =========================
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("dragoneggrepeat")) return false;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /dragoneggrepeat <info|stats|secret>");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "info":
                sender.sendMessage(ChatColor.GOLD + "Name: " + ChatColor.WHITE + "Dragon Egg Repeat");
                sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.WHITE + "1.3");
                sender.sendMessage(ChatColor.GOLD + "Author: " + ChatColor.WHITE + "Edminexus");
                return true;

            case "stats":
                sender.sendMessage(ChatColor.GREEN + "Dragon kills: " + ChatColor.WHITE + dragonKills);
                sender.sendMessage(ChatColor.GREEN + "Eggs distributed: " + ChatColor.WHITE + eggsDistributed);
                return true;

            case "secret":
                sender.sendMessage(ChatColor.BLUE + "Jeet, tui ekta gandu");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
        }
    }
}
