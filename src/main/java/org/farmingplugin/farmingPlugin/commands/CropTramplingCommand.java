package org.farmingplugin.farmingPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.farmingplugin.farmingPlugin.FarmingPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CropTramplingCommand implements CommandExecutor {

    private final FarmingPlugin plugin;
    private final HashMap<UUID, Boolean> playerTramples= new HashMap<UUID, Boolean>();

    public CropTramplingCommand(FarmingPlugin plugin) {
        this.plugin = plugin;
        // Populate the HashMap
        FileConfiguration config = plugin.getConfig();
        if (config.contains("playerTramplingStates"))
        {
            for (String id : Objects.requireNonNull(config.getConfigurationSection("playerTramplingStates")).getKeys(false))
            {
                UUID uuid = UUID.fromString(id);
                boolean state = config.getBoolean("playerTramplingStates." + id);
                playerTramples.put(uuid, state);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        // Check argument count
        if (args.length >= 2)
        {
            player.sendMessage("§e[!] Too many arguments!   §c/toggletrampling <player>");
            return false;
        }
        else if (args.length == 0)
        {
            player.sendMessage("§e[!] Too little arguments!   §c/toggletrampling <player>");
            return false;
        }

        // Get UUID of player but only if they've played on the server before
        UUID inputID;

        Player cmdPlayer = Bukkit.getPlayer(args[0]);
        if (cmdPlayer != null)
        {
            inputID = cmdPlayer.getUniqueId();
        }
        else
        {
            OfflinePlayer offlineCmdPlayer = Bukkit.getOfflinePlayer(args[0]);
            if (!offlineCmdPlayer.hasPlayedBefore())
            {
                player.sendMessage("§e[!] Player has never played the server before!");
                return false;
            }
            inputID = offlineCmdPlayer.getUniqueId();
        }


        // Read Hashmap in config file
        boolean trampleState;
        if (playerTramples.containsKey(inputID))
        {
            trampleState = !playerTramples.get(inputID);
        }
        else
        {
            trampleState = true;
        }
        playerTramples.put(inputID, trampleState);

        plugin.getConfig().set("playerTramplingStates." + inputID, trampleState);
        plugin.saveConfig();

        if (!player.getName().equalsIgnoreCase(args[0]) && !player.isOp()) {
           player.sendMessage("§e[!] That's not you!");
        }
        else if (player.getName().equalsIgnoreCase(args[0]) || player.isOp())
        {
            if (playerTramples.get(inputID))
                player.sendMessage("§e[!] Player " + args[0] + " can't trample crops anymore!");
            else
                player.sendMessage("§e[!] Player " + args[0] + " can trample crops again!");
        }



        return true;
    }
}
