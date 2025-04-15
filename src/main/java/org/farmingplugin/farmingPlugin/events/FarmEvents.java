package org.farmingplugin.farmingPlugin.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.farmingplugin.farmingPlugin.FarmingPlugin;

import java.util.Random;
import java.util.UUID;

public class FarmEvents implements Listener {

    private static FarmingPlugin plugin = null;

    public FarmEvents(FarmingPlugin plugin) {
        FarmEvents.plugin = plugin;
    }

    @EventHandler // Stop pistons from pushing crops
    public static void onPistonCropBreak(BlockPistonExtendEvent event)
    {
        for (Block block : event.getBlocks())
        {
            if (CanFarm(block.getType().toString()))
                event.setCancelled(true);
        }
    }

    @EventHandler // Stop players from getting crops normally from breaking them
    public static void onPlayerCropBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        if (CanFarm(block.getType().toString()))
            event.setDropItems(false);
    }

    @EventHandler
    public static void onPlayerCropWalk(PlayerInteractEvent event)
    {
        // Important Variables
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        Player player = event.getPlayer();
        UUID playerID = player.getUniqueId();
        boolean playerTrampleState = plugin.getConfig().getBoolean("playerTramplingStates." + playerID.toString());
        ItemStack playerItem = event.getItem();

        if (event.getAction() == Action.PHYSICAL && block.getType() == Material.FARMLAND)
        {
            event.setCancelled(playerTrampleState);
        }
    }

    @EventHandler
    public static void onPlayerFarmCrop(PlayerInteractEvent event)
    {
        // Important Variables
        Player player = event.getPlayer();
        ItemStack playerItem = event.getItem();
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        String blockName = block.getType().toString();


        // Begin functionality if a block is clicked with an item
        if (event.getAction().isRightClick() && playerItem != null && blockName != null)
        {
            String itemName = playerItem.getType().toString();
            boolean isAHoe = false;
            boolean isATool = false;
            boolean isACrop = CanFarm(blockName);

            // Check if the player's held item is a hoe or even a tool
            if (itemName.endsWith("_HOE"))
            {
                isAHoe = true;
                isATool = true;
            }
            else if (itemName.endsWith("_SHOVEL") || itemName.endsWith("_PICKAXE") || itemName.endsWith("_AXE") || itemName.endsWith("_SWORD"))
            {
                isATool = true;
            }

            // Check if the player is using a hoe to farm a crop
            if (isAHoe && isACrop) // farm the crop
            {
                UseHoe(player, playerItem, block, blockName);
            }
            else if (isAHoe && !isACrop) // if they're using a hoe but not farming a crop
            {
                if (!CanTill(block))
                    player.sendMessage("§e[!] Use a hoe to farm a crop!");
            }
            else if (isATool && isACrop) // Else return a message if they aren't using a hoe but a different tool
            {
                player.sendMessage("§e[!] You need to use a hoe to harvest this!");
            }

        }
    }

    private static void UseHoe(Player player, ItemStack hoe, Block crop, String cropName)
    {
        int seedYield = 0;
        int cropYield = 0;

        switch (hoe.getType().name()) { // Check what type of hoe they're using
            case "WOODEN_HOE", "STONE_HOE" -> {
                seedYield = 1;
                cropYield = 1;
            }
            case "IRON_HOE", "GOLDEN_HOE" -> {
                seedYield = 2;
                cropYield = 2;
            }
            case "DIAMOND_HOE", "NETHERITE_HOE" -> {
                seedYield = 3;
                cropYield = 3;
            }
        }

        DropCrop(player, cropYield, seedYield, crop, cropName, hoe);
        
        crop.setType(Material.AIR); // Destroy block
    }

    // Check if the block is a farm-able crop; takes in the block's name as a parameter
    private static boolean CanFarm(String cropName)
    {
        return cropName.equals("WHEAT") || cropName.equals("CARROTS")
                || cropName.equals("POTATOES") || cropName.equals("BEETROOTS");
    }
    // Check if block can be turned into farmland
    private static boolean CanTill(Block block)
    {
        String blockName = block.getType().name();
        if (blockName.equals("DIRT") || blockName.equals("GRASS_BLOCK")
                || blockName.equals("COARSE_DIRT") || blockName.equals("DIRT_PATH"))
            return true;
        else
            return false;
    }

    // Method that depletes a Minecraft hoe's durability by one when it is used
    private static void ChipHoe(ItemStack hoe, Player player)
    {
        Damageable durability = (Damageable)hoe.getItemMeta();

        int currDmg = durability.getDamage();
        durability.setDamage(currDmg + 1);
        player.sendMessage("§eDamage: " + currDmg);

        if (currDmg >= hoe.getType().getMaxDurability())
            player.getInventory().remove(hoe);
        else
            hoe.setItemMeta(durability);
    }

    // Drop the crop with correlating seed depending on what block it is
    private static void DropCrop(Player player, int cropCount, int seedCount, Block crop, String cropName, ItemStack hoe)
    {
        Material cropDrop = crop.getType();
        Material seedDrop = Material.AIR;
        int unbreakingLevel = 1;
        double goldCarrotRnd = 1.1;

        // Find out which crop and seed to drop
        switch (cropName)
        {
            case "WHEAT" -> {
                seedDrop = Material.WHEAT_SEEDS;
            }
            case "BEETROOTS" -> {
                cropDrop = Material.BEETROOT;
                seedDrop = Material.BEETROOT_SEEDS;
            }
            case "POTATOES" -> {
                cropDrop = Material.POTATO;
                seedDrop = Material.POTATO;
            }
            case "CARROTS" -> {
                cropDrop = Material.CARROT;
                seedDrop = Material.CARROT;

                if (hoe.getItemMeta().hasEnchant(Enchantment.FORTUNE))
                    goldCarrotRnd += hoe.getEnchantmentLevel(Enchantment.FORTUNE);

            }
        }

        // Check for enchants
        if (hoe.getItemMeta().hasEnchant(Enchantment.FORTUNE))
        {
            cropCount += hoe.getEnchantmentLevel(Enchantment.FORTUNE);
        }
        if (hoe.getItemMeta().hasEnchant(Enchantment.UNBREAKING))
        {
            unbreakingLevel += hoe.getEnchantmentLevel(Enchantment.UNBREAKING);
        }

        // Drop the crops
        for (int i = 0; i < cropCount; i++) {
            crop.getWorld().dropItemNaturally(crop.getLocation(), new ItemStack(cropDrop));
        }
        for (int i = 0; i < seedCount; i++) {
            crop.getWorld().dropItemNaturally(crop.getLocation(), new ItemStack(seedDrop));
        }

        // Check if the hoe takes damage to durability or not if enchanted with unbreaking
        Random rand = new Random();
        int qualify = rand.nextInt(100);
        int unbreakingChance = (100/unbreakingLevel);
        if (qualify <= unbreakingChance) {
            ChipHoe(hoe, player); // Damage hoe
        }

        // Spawn gold carrot logic
        double goldCarrotChance = (100/goldCarrotRnd);
        player.sendMessage(qualify + " " + goldCarrotChance + " " + goldCarrotRnd);
        if (qualify >= goldCarrotChance && cropName.equals("CARROTS"))
        {
            crop.getWorld().dropItemNaturally(crop.getLocation(), new ItemStack(Material.GOLDEN_CARROT, 1));
        }

    }

}

