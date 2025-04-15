package org.farmingplugin.farmingPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.farmingplugin.farmingPlugin.commands.CropTramplingCommand;
import org.farmingplugin.farmingPlugin.events.FarmEvents;

public final class FarmingPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig(); // creates default config file if not present
        FarmEvents fe = new FarmEvents(this);
        CropTramplingCommand cropCmds = new CropTramplingCommand(this);

        getServer().getPluginManager().registerEvents(fe, this);
        getCommand("toggletrampling").setExecutor(cropCmds);

        getLogger().info("§aFarmingPlugin Activated");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("§4FarmingPlugin Deactivated");
    }
}
