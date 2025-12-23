package com.hxstar.fixeditem.listener;

import com.hxstar.fixeditem.HxFixedItem;
import com.hxstar.fixeditem.manager.FixedItemManager;
import com.hxstar.fixeditem.model.FixedItemData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 物品保护监听器
 * 负责处理固定物品的各种保护机制
 * 包括：禁止丢弃、禁止移动、死亡不掉落、禁止存入容器等
 */
public class ItemProtectionListener implements Listener {

    private final HxFixedItem plugin;
    private final FixedItemManager fixedItemManager;

    public ItemProtectionListener(HxFixedItem plugin) {
        this.plugin = plugin;
        this.fixedItemManager = plugin.getFixedItemManager();
    }

    /**
     * 监听物品丢弃事件 - 禁止丢弃固定物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (fixedItemManager.isFixedItem(item)) {
            FixedItemData data = fixedItemManager.getFixedItemData(item);
            if (data != null && data.isPreventDrop()) {
                event.setCancelled(true);

                // 发送提示消息
                Player player = event.getPlayer();
                String message = plugin.getLanguageManager().getPrefixedMessage("protection.cannot-drop");
                player.sendMessage(message);

                // 确保物品返回正确槽位
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    fixedItemManager.checkAndRestoreItems(player);
                }, 1L);
            }
        }
    }

    /**
     * 监听背包点击事件 - 禁止移动固定物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        // 检查玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        int slot = event.getSlot();
        int rawSlot = event.getRawSlot();

        // 检查点击的物品是否为固定物品
        if (fixedItemManager.isFixedItem(currentItem)) {
            FixedItemData data = fixedItemManager.getFixedItemData(currentItem);
            if (data != null && data.isPreventMove()) {
                // 禁止任何操作
                event.setCancelled(true);
                String message = plugin.getLanguageManager().getPrefixedMessage("protection.cannot-move");
                player.sendMessage(message);
                return;
            }
        }

        // 检查光标上的物品是否为固定物品（防止Shift点击后拿着固定物品）
        if (fixedItemManager.isFixedItem(cursorItem)) {
            FixedItemData data = fixedItemManager.getFixedItemData(cursorItem);
            if (data != null && data.isPreventMove()) {
                event.setCancelled(true);
                return;
            }
        }

        // 检查是否尝试将物品放入固定槽位
        if (clickedInventory != null && clickedInventory.equals(player.getInventory())) {
            if (fixedItemManager.isFixedSlot(slot)) {
                FixedItemData slotData = plugin.getConfigManager().getFixedItemBySlot(slot);
                if (slotData != null && slotData.isPreventMove()) {
                    // 只允许固定物品在这个槽位
                    if (!fixedItemManager.isFixedItem(cursorItem)) {
                        // 如果光标上有物品且不是固定物品，取消操作
                        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        // 检查Shift点击 - 防止将固定物品移动到其他容器
        if (event.isShiftClick() && fixedItemManager.isFixedItem(currentItem)) {
            FixedItemData data = fixedItemManager.getFixedItemData(currentItem);
            if (data != null && (data.isPreventMove() || data.isPreventContainer())) {
                event.setCancelled(true);
                String message = plugin.getLanguageManager().getPrefixedMessage("protection.cannot-move");
                player.sendMessage(message);
                return;
            }
        }

        // 检查数字键切换 - 防止通过数字键移动固定物品
        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarSlot = event.getHotbarButton();
            ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);

            // 检查热键槽位的物品是否为固定物品
            if (fixedItemManager.isFixedItem(hotbarItem)) {
                FixedItemData data = fixedItemManager.getFixedItemData(hotbarItem);
                if (data != null && data.isPreventMove()) {
                    event.setCancelled(true);
                    return;
                }
            }

            // 检查是否尝试将物品放入固定槽位
            if (fixedItemManager.isFixedSlot(hotbarSlot)) {
                event.setCancelled(true);
                return;
            }
        }

        // 防止将固定物品放入容器（如箱子）
        if (clickedInventory != null && !clickedInventory.equals(player.getInventory())) {
            if (fixedItemManager.isFixedItem(cursorItem)) {
                FixedItemData data = fixedItemManager.getFixedItemData(cursorItem);
                if (data != null && data.isPreventContainer()) {
                    event.setCancelled(true);
                    String message = plugin.getLanguageManager().getPrefixedMessage("protection.cannot-container");
                    player.sendMessage(message);
                    return;
                }
            }
        }
    }

    /**
     * 监听背包拖拽事件 - 禁止拖拽固定物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // 检查被拖拽的物品是否为固定物品
        ItemStack draggedItem = event.getOldCursor();
        if (fixedItemManager.isFixedItem(draggedItem)) {
            FixedItemData data = fixedItemManager.getFixedItemData(draggedItem);
            if (data != null && data.isPreventMove()) {
                event.setCancelled(true);
                return;
            }
        }

        // 检查拖拽是否涉及固定槽位
        for (int slot : event.getRawSlots()) {
            // 转换为玩家背包槽位
            int inventorySlot = convertRawSlotToInventorySlot(event.getView(), slot);
            if (inventorySlot >= 0 && fixedItemManager.isFixedSlot(inventorySlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * 转换原始槽位到背包槽位
     */
    private int convertRawSlotToInventorySlot(org.bukkit.inventory.InventoryView view, int rawSlot) {
        if (view.getTopInventory() != null && rawSlot < view.getTopInventory().getSize()) {
            return -1; // 在上方容器中
        }

        int topSize = view.getTopInventory() != null ? view.getTopInventory().getSize() : 0;
        int adjustedSlot = rawSlot - topSize;

        // 玩家背包布局：9-35 是主背包，0-8 是快捷栏
        if (adjustedSlot >= 0 && adjustedSlot < 27) {
            return adjustedSlot + 9; // 主背包
        } else if (adjustedSlot >= 27 && adjustedSlot < 36) {
            return adjustedSlot - 27; // 快捷栏
        }

        return -1;
    }

    /**
     * 监听玩家死亡事件 - 防止固定物品掉落
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // 检查玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        List<ItemStack> fixedItemsToKeep = new ArrayList<>();

        // 遍历掉落物，移除固定物品
        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (fixedItemManager.isFixedItem(item)) {
                FixedItemData data = fixedItemManager.getFixedItemData(item);
                if (data != null && data.isPreventDeath()) {
                    iterator.remove();
                    fixedItemsToKeep.add(item);
                }
            }
        }

        // 在玩家复活后恢复固定物品
        if (!fixedItemsToKeep.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                fixedItemManager.giveFixedItems(player);
            }, 1L);
        }
    }

    /**
     * 监听副手切换事件 - 防止通过F键移动固定物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        // 检查玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        ItemStack mainHandItem = event.getMainHandItem();
        ItemStack offHandItem = event.getOffHandItem();

        // 检查主手物品
        if (fixedItemManager.isFixedItem(mainHandItem)) {
            FixedItemData data = fixedItemManager.getFixedItemData(mainHandItem);
            if (data != null && data.isPreventMove()) {
                event.setCancelled(true);
                String message = plugin.getLanguageManager().getPrefixedMessage("protection.cannot-move");
                player.sendMessage(message);
                return;
            }
        }

        // 检查副手物品
        if (fixedItemManager.isFixedItem(offHandItem)) {
            FixedItemData data = fixedItemManager.getFixedItemData(offHandItem);
            if (data != null && data.isPreventMove()) {
                event.setCancelled(true);
                return;
            }
        }

        // 检查主手是否为固定槽位（槽位0通常是快捷栏第一格）
        int heldSlot = player.getInventory().getHeldItemSlot();
        if (fixedItemManager.isFixedSlot(heldSlot)) {
            event.setCancelled(true);
        }
    }

    /**
     * 监听物品移动事件 - 防止漏斗等移动固定物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();

        if (fixedItemManager.isFixedItem(item)) {
            event.setCancelled(true);
        }
    }

    /**
     * 监听物品拾取事件 - 防止拾取掉落的固定物品（理论上不应该有）
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();

        if (fixedItemManager.isFixedItem(item)) {
            // 固定物品不应该在地上，直接销毁
            event.getItem().remove();
            event.setCancelled(true);
        }
    }

    /**
     * 监听创造模式中键复制事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // 检查玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        int slot = event.getSlot();

        // 防止在固定槽位放置其他物品
        if (fixedItemManager.isFixedSlot(slot)) {
            ItemStack newItem = event.getCursor();
            if (newItem != null && newItem.getType() != Material.AIR) {
                if (!fixedItemManager.isFixedItem(newItem)) {
                    event.setCancelled(true);

                    // 恢复固定物品
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        fixedItemManager.checkAndRestoreItems(player);
                    }, 1L);
                }
            }
        }
    }

    /**
     * 监听背包关闭事件 - 确保固定物品存在
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // 检查玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        // 延迟检查，确保背包操作完成
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                fixedItemManager.checkAndRestoreItems(player);
            }
        }, 1L);
    }
}
