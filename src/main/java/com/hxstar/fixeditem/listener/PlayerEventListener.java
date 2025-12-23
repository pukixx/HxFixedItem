package com.hxstar.fixeditem.listener;

import com.hxstar.fixeditem.HxFixedItem;
import com.hxstar.fixeditem.manager.CooldownManager;
import com.hxstar.fixeditem.manager.FixedItemManager;
import com.hxstar.fixeditem.model.FixedItemData;
import com.hxstar.fixeditem.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 玩家事件监听器
 * 负责处理玩家相关事件，包括：
 * - 进入服务器
 * - 切换世界
 * - 复活
 * - 物品交互（左键/右键执行命令）
 */
public class PlayerEventListener implements Listener {

    private final HxFixedItem plugin;
    private final FixedItemManager fixedItemManager;
    private final CooldownManager cooldownManager;

    public PlayerEventListener(HxFixedItem plugin) {
        this.plugin = plugin;
        this.fixedItemManager = plugin.getFixedItemManager();
        this.cooldownManager = plugin.getCooldownManager();
    }

    /**
     * 监听玩家进入服务器事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 延迟给予固定物品，确保玩家完全加载
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                fixedItemManager.giveFixedItems(player);
            }
        }, 10L);
    }

    /**
     * 监听玩家退出服务器事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 清理玩家的冷却数据
        cooldownManager.clearPlayerCooldowns(player);
    }

    /**
     * 监听玩家切换世界事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // 延迟处理，确保世界切换完成
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                // 检查新世界是否启用
                if (plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
                    // 新世界启用，给予固定物品
                    fixedItemManager.giveFixedItems(player);
                } else {
                    // 新世界未启用，移除固定物品
                    fixedItemManager.removeAllFixedItems(player);
                }
            }
        }, 5L);
    }

    /**
     * 监听玩家复活事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 延迟给予固定物品，确保复活完成
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                fixedItemManager.giveFixedItems(player);
            }
        }, 5L);
    }

    /**
     * 监听玩家传送事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // 如果传送到不同世界，由 PlayerChangedWorldEvent 处理
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return;
        }

        // 同世界传送后检查固定物品
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                fixedItemManager.checkAndRestoreItems(player);
            }
        }, 3L);
    }

    /**
     * 监听玩家交互事件 - 处理左键/右键执行命令
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        // 检查玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        // 只处理左键和右键
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK &&
                action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        // 检查是否为固定物品
        if (!fixedItemManager.isFixedItem(item)) {
            return;
        }

        FixedItemData itemData = fixedItemManager.getFixedItemData(item);
        if (itemData == null) {
            return;
        }

        boolean isLeftClick = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK);
        boolean isRightClick = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);

        // 处理左键
        if (isLeftClick && itemData.isLeftClickEnabled()) {
            handleClick(player, itemData, true);
            event.setCancelled(true);
            return;
        }

        // 处理右键
        if (isRightClick && itemData.isRightClickEnabled()) {
            handleClick(player, itemData, false);
            event.setCancelled(true);
        }
    }

    /**
     * 监听玩家交互实体事件 - 防止用固定物品交互实体
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查是否为固定物品
        if (fixedItemManager.isFixedItem(item)) {
            FixedItemData itemData = fixedItemManager.getFixedItemData(item);
            if (itemData != null && itemData.isRightClickEnabled()) {
                handleClick(player, itemData, false);
                event.setCancelled(true);
            }
        }
    }

    /**
     * 处理点击事件
     *
     * @param player      玩家
     * @param itemData    物品数据
     * @param isLeftClick 是否为左键点击
     */
    private void handleClick(Player player, FixedItemData itemData, boolean isLeftClick) {
        String clickType = isLeftClick ? "left" : "right";
        int cooldown = isLeftClick ? itemData.getLeftClickCooldown() : itemData.getRightClickCooldown();
        List<String> commands = isLeftClick ? itemData.getLeftClickCommands() : itemData.getRightClickCommands();
        boolean asConsole = isLeftClick ? itemData.isLeftClickAsConsole() : itemData.isRightClickAsConsole();
        Sound sound = isLeftClick ? itemData.getLeftClickSound() : itemData.getRightClickSound();
        float volume = isLeftClick ? itemData.getLeftClickSoundVolume() : itemData.getRightClickSoundVolume();
        float pitch = isLeftClick ? itemData.getLeftClickSoundPitch() : itemData.getRightClickSoundPitch();

        // 检查命令列表是否为空
        if (commands == null || commands.isEmpty()) {
            return;
        }

        // 检查冷却
        if (cooldown > 0) {
            String cooldownKey = itemData.getItemId() + "_" + clickType;
            if (cooldownManager.isOnCooldown(player, cooldownKey)) {
                long remaining = cooldownManager.getRemainingCooldown(player, cooldownKey);
                String message = plugin.getLanguageManager().getPrefixedMessage(
                        "cooldown.wait",
                        "{time}", String.format("%.1f", remaining / 1000.0)
                );
                player.sendMessage(message);
                return;
            }

            // 设置冷却
            cooldownManager.setCooldown(player, cooldownKey, cooldown * 1000L);
        }

        // 播放音效
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }

        // 执行命令
        executeCommands(player, commands, asConsole);

        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("玩家 " + player.getName() + " " + (isLeftClick ? "左键" : "右键") +
                    " 使用了固定物品: " + itemData.getItemId());
        }
    }

    /**
     * 执行命令列表
     *
     * @param player    玩家
     * @param commands  命令列表
     * @param asConsole 是否以控制台身份执行
     */
    private void executeCommands(Player player, List<String> commands, boolean asConsole) {
        for (String command : commands) {
            // 替换内置占位符
            String processedCommand = command
                    .replace("{player}", player.getName())
                    .replace("{uuid}", player.getUniqueId().toString())
                    .replace("{world}", player.getWorld().getName())
                    .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                    .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                    .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));

            // 解析 PlaceholderAPI 占位符
            processedCommand = PlaceholderUtil.parsePlaceholders(player, processedCommand);

            // 执行命令
            if (asConsole) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            } else {
                player.performCommand(processedCommand);
            }
        }
    }

    /**
     * 监听玩家游戏模式切换事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();

        // 延迟检查固定物品
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                fixedItemManager.checkAndRestoreItems(player);
            }
        }, 3L);
    }

    /**
     * 监听玩家切换手持物品事件 - 用于额外检查
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // 延迟检查，确保切换完成
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                fixedItemManager.checkAndRestoreItems(player);
            }
        }, 1L);
    }
}
