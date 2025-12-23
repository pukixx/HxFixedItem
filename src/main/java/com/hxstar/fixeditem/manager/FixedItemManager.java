package com.hxstar.fixeditem.manager;

import com.hxstar.fixeditem.HxFixedItem;
import com.hxstar.fixeditem.model.FixedItemData;
import com.hxstar.fixeditem.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 固定物品管理器
 * 负责创建、给予、检查和移除固定物品
 */
public class FixedItemManager {

    private final HxFixedItem plugin;
    private final NamespacedKey fixedItemKey;
    private final NamespacedKey itemIdKey;

    public FixedItemManager(HxFixedItem plugin) {
        this.plugin = plugin;
        this.fixedItemKey = new NamespacedKey(plugin, "fixed_item");
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
    }

    /**
     * 创建固定物品
     *
     * @param itemData 物品数据
     * @return 创建的物品
     */
    public ItemStack createFixedItem(FixedItemData itemData) {
        return createFixedItem(itemData, null);
    }

    /**
     * 为特定玩家创建固定物品（解析PlaceholderAPI占位符）
     *
     * @param itemData 物品数据
     * @param player   玩家（用于解析占位符，可为null）
     * @return 创建的物品
     */
    public ItemStack createFixedItem(FixedItemData itemData, Player player) {
        ItemStack item = new ItemStack(itemData.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        // 设置显示名称（解析占位符）
        String displayName = itemData.getDisplayName();
        if (player != null) {
            displayName = PlaceholderUtil.parsePlaceholders(player, displayName);
        }
        meta.setDisplayName(displayName);

        // 设置描述（解析占位符）
        if (itemData.getLore() != null && !itemData.getLore().isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : itemData.getLore()) {
                if (player != null) {
                    line = PlaceholderUtil.parsePlaceholders(player, line);
                }
                lore.add(line);
            }
            meta.setLore(lore);
        }

        // 设置自定义模型数据
        if (itemData.getCustomModelData() > 0) {
            meta.setCustomModelData(itemData.getCustomModelData());
        }

        // 设置发光效果
        if (itemData.isGlowing()) {
            Enchantment glowEnchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking"));
            if (glowEnchant != null) {
                meta.addEnchant(glowEnchant, 1, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // 添加标识数据（用于识别这是固定物品）
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(fixedItemKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(itemIdKey, PersistentDataType.STRING, itemData.getItemId());

        // 添加隐藏标志
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 检查物品是否为固定物品
     *
     * @param item 要检查的物品
     * @return 是否为固定物品
     */
    public boolean isFixedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(fixedItemKey, PersistentDataType.BYTE);
    }

    /**
     * 获取固定物品的ID
     *
     * @param item 固定物品
     * @return 物品ID，如果不是固定物品则返回null
     */
    public String getFixedItemId(ItemStack item) {
        if (!isFixedItem(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(itemIdKey, PersistentDataType.STRING);
    }

    /**
     * 给予玩家所有固定物品
     *
     * @param player 玩家
     */
    public void giveFixedItems(Player player) {
        // 检查玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            // 如果世界未启用，移除所有固定物品
            removeAllFixedItems(player);
            return;
        }

        Map<String, FixedItemData> fixedItems = plugin.getConfigManager().getFixedItems();
        PlayerInventory inventory = player.getInventory();

        for (FixedItemData itemData : fixedItems.values()) {
            int slot = itemData.getSlot();

            // 检查槽位是否已经有正确的固定物品
            ItemStack currentItem = inventory.getItem(slot);

            if (isFixedItem(currentItem)) {
                String currentId = getFixedItemId(currentItem);
                if (itemData.getItemId().equals(currentId)) {
                    // 已经有正确的固定物品，跳过
                    continue;
                } else {
                    // 有其他固定物品，先移除
                    inventory.setItem(slot, null);
                }
            }

            // 如果槽位有其他物品，尝试移动到其他位置
            if (currentItem != null && currentItem.getType() != Material.AIR) {
                // 尝试找一个空位
                int emptySlot = inventory.firstEmpty();
                if (emptySlot != -1 && emptySlot != slot) {
                    inventory.setItem(emptySlot, currentItem);
                } else {
                    // 没有空位，物品会被替换（掉落到地上）
                    player.getWorld().dropItemNaturally(player.getLocation(), currentItem);
                }
            }

            // 放置固定物品（传入玩家以解析PAPI占位符）
            ItemStack fixedItem = createFixedItem(itemData, player);
            inventory.setItem(slot, fixedItem);

            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info("给予玩家 " + player.getName() + " 固定物品: " + itemData.getItemId() + " 到槽位 " + slot);
            }
        }
    }

    /**
     * 检查并恢复玩家的固定物品
     *
     * @param player 玩家
     */
    public void checkAndRestoreItems(Player player) {
        // 检查玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        Map<String, FixedItemData> fixedItems = plugin.getConfigManager().getFixedItems();
        PlayerInventory inventory = player.getInventory();

        for (FixedItemData itemData : fixedItems.values()) {
            int slot = itemData.getSlot();
            ItemStack currentItem = inventory.getItem(slot);

            // 检查槽位是否有正确的固定物品
            if (!isCorrectFixedItem(currentItem, itemData.getItemId())) {
                // 需要恢复固定物品
                restoreFixedItem(player, itemData, currentItem);
            }
        }
    }

    /**
     * 检查物品是否为指定的固定物品
     *
     * @param item   要检查的物品
     * @param itemId 期望的物品ID
     * @return 是否为正确的固定物品
     */
    private boolean isCorrectFixedItem(ItemStack item, String itemId) {
        if (!isFixedItem(item)) {
            return false;
        }
        String currentId = getFixedItemId(item);
        return itemId.equals(currentId);
    }

    /**
     * 恢复固定物品到槽位
     *
     * @param player      玩家
     * @param itemData    物品数据
     * @param currentItem 当前槽位的物品
     */
    private void restoreFixedItem(Player player, FixedItemData itemData, ItemStack currentItem) {
        PlayerInventory inventory = player.getInventory();
        int slot = itemData.getSlot();

        // 如果当前物品不是固定物品且不为空，需要处理
        if (currentItem != null && currentItem.getType() != Material.AIR && !isFixedItem(currentItem)) {
            // 尝试移动到其他槽位
            int emptySlot = -1;
            for (int i = 0; i < 36; i++) {
                if (i == slot) continue;
                ItemStack checkItem = inventory.getItem(i);
                if (checkItem == null || checkItem.getType() == Material.AIR) {
                    emptySlot = i;
                    break;
                }
            }

            if (emptySlot != -1) {
                inventory.setItem(emptySlot, currentItem);
            } else {
                // 没有空位，掉落到地上
                player.getWorld().dropItemNaturally(player.getLocation(), currentItem);
            }
        }

        // 放置固定物品（传入玩家以解析PAPI占位符）
        ItemStack fixedItem = createFixedItem(itemData, player);
        inventory.setItem(slot, fixedItem);

        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("恢复玩家 " + player.getName() + " 的固定物品: " + itemData.getItemId());
        }
    }

    /**
     * 移除玩家的所有固定物品
     *
     * @param player 玩家
     */
    public void removeAllFixedItems(Player player) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (isFixedItem(item)) {
                inventory.setItem(i, null);
            }
        }
    }

    /**
     * 移除玩家特定的固定物品
     *
     * @param player 玩家
     * @param itemId 物品ID
     */
    public void removeFixedItem(Player player, String itemId) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (isFixedItem(item)) {
                String currentId = getFixedItemId(item);
                if (itemId.equals(currentId)) {
                    inventory.setItem(i, null);
                    break;
                }
            }
        }
    }

    /**
     * 刷新玩家的固定物品（重载配置后使用）
     *
     * @param player 玩家
     */
    public void refreshFixedItems(Player player) {
        // 先移除所有旧的固定物品
        removeAllFixedItems(player);

        // 延迟1tick后给予新的固定物品
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            giveFixedItems(player);
        }, 1L);
    }

    /**
     * 获取固定物品的数据（通过物品）
     *
     * @param item 物品
     * @return 物品数据，如果不是固定物品则返回null
     */
    public FixedItemData getFixedItemData(ItemStack item) {
        String itemId = getFixedItemId(item);
        if (itemId == null) {
            return null;
        }
        return plugin.getConfigManager().getFixedItemData(itemId);
    }

    /**
     * 检查槽位是否为固定槽位
     *
     * @param slot 槽位
     * @return 是否为固定槽位
     */
    public boolean isFixedSlot(int slot) {
        return plugin.getConfigManager().getFixedSlots().contains(slot);
    }

    /**
     * 获取NamespacedKey（用于外部识别）
     */
    public NamespacedKey getFixedItemKey() {
        return fixedItemKey;
    }

    public NamespacedKey getItemIdKey() {
        return itemIdKey;
    }
}
