use this one instead package com.ovalaardvark433.anti32k;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OvalsAnti32k extends JavaPlugin implements Listener {

    private final Map<Material, String> illegalItems = new HashMap<>();
    private final Map<Material, String> itemLoreMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Add illegal items with their disguised names and lores
        illegalItems.put(Material.BEDROCK, "Nemo");
        illegalItems.put(Material.ENDER_PORTAL_FRAME, "Nemo");
        illegalItems.put(Material.MOB_SPAWNER, "Nemo");

        itemLoreMap.put(Material.BEDROCK, "B");
        itemLoreMap.put(Material.ENDER_PORTAL_FRAME, "P");
        itemLoreMap.put(Material.MOB_SPAWNER, "S");

        getLogger().info("OvalsAnti32k has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("OvalsAnti32k has been disabled!");
    }

    private boolean isDisguised(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.hasLore() && meta.getLore().size() == 1 && itemLoreMap.containsValue(meta.getLore().get(0));
    }

    private void disguiseItem(ItemStack item) {
        if (item == null || !illegalItems.containsKey(item.getType())) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(illegalItems.get(item.getType()));
            String lore = itemLoreMap.get(item.getType());
            meta.setLore(Collections.singletonList(lore));
            item.setItemMeta(meta);
            item.setType(Material.RAW_FISH);
            item.setDurability((short) 2); // Set durability to 2 for Clownfish
        }
    }

    private void revertItem(ItemStack item) {
        if (!isDisguised(item)) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }

        String lore = meta.getLore().get(0);
        if (lore.equals("B")) {
            item.setType(Material.BEDROCK);
        } else if (lore.equals("P")) {
            item.setType(Material.ENDER_PORTAL_FRAME);
        } else if (lore.equals("S")) {
            item.setType(Material.MOB_SPAWNER);
        }

        meta.setDisplayName(null);
        meta.setLore(null);
        item.setItemMeta(meta);
    }

    private void checkAndFixItem(ItemStack item) {
        if (item == null) return;

        // Revert specific items to clownfish
        Material type = item.getType();
        if (type == Material.BEDROCK || type == Material.ENDER_PORTAL_FRAME || type == Material.MOB_SPAWNER) {
            item.setType(Material.RAW_FISH);
            item.setDurability((short) 2); // Clownfish data value

            // Set name to "Nemo" and set lore
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RESET + "Nemo");
                String lore = itemLoreMap.get(type);
                meta.setLore(Collections.singletonList(lore));
                item.setItemMeta(meta);
            }
            return;
        }

        // Check and fix enchantments
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int maxLevel = enchantment.getMaxLevel();
            int currentLevel = entry.getValue();
            if (currentLevel > maxLevel) {
                item.removeEnchantment(enchantment);
                item.addUnsafeEnchantment(enchantment, maxLevel);
            }
        }

        // Ensure unstackable items are not stacked
        if (item.getMaxStackSize() == 1 && item.getAmount() > 1) {
            item.setAmount(1);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        // Check if the item being placed is a Shulker Box
        if (itemInHand != null && isShulkerBox(itemInHand.getType())) {
            Block block = event.getBlockPlaced();
            BlockState state = block.getState();
            if (state instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) state;
                Inventory inventory = shulkerBox.getInventory();
                for (ItemStack item : inventory.getContents()) {
                    checkAndFixItem(item);
                }
            }
        }
        // Check and fix the item in hand (could be a different item than the Shulker Box)
        checkAndFixItem(itemInHand);
    }

    private boolean isShulkerBox(Material material) {
        switch (material) {
            case WHITE_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case SILVER_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType().toString().contains("SHULKER_BOX")) {
            BlockState state = block.getState();
            if (state instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) state;
                for (ItemStack item : shulkerBox.getInventory().getContents()) {
                    revertItem(item);
                }
            }
        } else if (illegalItems.containsKey(block.getType())) {
            ItemStack droppedItem = new ItemStack(block.getType(), 1);
            disguiseItem(droppedItem);
            block.getWorld().dropItemNaturally(block.getLocation(), droppedItem);
            block.setType(Material.AIR);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        checkAndFixItem(event.getItem().getItemStack());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        checkAndFixItem(event.getCurrentItem());
        checkAndFixItem(event.getCursor());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        for (ItemStack item : event.getInventory().getContents()) {
            checkAndFixItem(item);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        for (ItemStack item : event.getInventory().getContents()) {
            checkAndFixItem(item);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        checkAndFixItem(event.getMainHandItem());
        checkAndFixItem(event.getOffHandItem());
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        for (ItemStack item : player.getInventory().getContents()) {
            checkAndFixItem(item);
        }
    }
}