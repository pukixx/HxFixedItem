package com.hxstar.fixeditem.manager;

import com.hxstar.fixeditem.HxFixedItem;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冷却管理器
 * 负责管理玩家使用固定物品的冷却时间
 */
public class CooldownManager {

    private final HxFixedItem plugin;

    // 冷却数据存储: UUID -> (冷却键 -> 过期时间戳)
    private final Map<UUID, Map<String, Long>> cooldowns;

    public CooldownManager(HxFixedItem plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
    }

    /**
     * 检查玩家是否在冷却中
     *
     * @param player      玩家
     * @param cooldownKey 冷却键 (通常是 itemId_clickType)
     * @return 是否在冷却中
     */
    public boolean isOnCooldown(Player player, String cooldownKey) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);

        if (playerCooldowns == null) {
            return false;
        }

        Long expireTime = playerCooldowns.get(cooldownKey);
        if (expireTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expireTime) {
            // 冷却已过期，移除记录
            playerCooldowns.remove(cooldownKey);
            return false;
        }

        return true;
    }

    /**
     * 设置玩家的冷却
     *
     * @param player        玩家
     * @param cooldownKey   冷却键
     * @param cooldownMillis 冷却时间（毫秒）
     */
    public void setCooldown(Player player, String cooldownKey, long cooldownMillis) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());

        long expireTime = System.currentTimeMillis() + cooldownMillis;
        playerCooldowns.put(cooldownKey, expireTime);
    }

    /**
     * 获取剩余冷却时间
     *
     * @param player      玩家
     * @param cooldownKey 冷却键
     * @return 剩余冷却时间（毫秒），如果没有冷却则返回0
     */
    public long getRemainingCooldown(Player player, String cooldownKey) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);

        if (playerCooldowns == null) {
            return 0;
        }

        Long expireTime = playerCooldowns.get(cooldownKey);
        if (expireTime == null) {
            return 0;
        }

        long remaining = expireTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * 移除玩家的特定冷却
     *
     * @param player      玩家
     * @param cooldownKey 冷却键
     */
    public void removeCooldown(Player player, String cooldownKey) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);

        if (playerCooldowns != null) {
            playerCooldowns.remove(cooldownKey);
        }
    }

    /**
     * 清除玩家的所有冷却
     *
     * @param player 玩家
     */
    public void clearPlayerCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * 清除所有玩家的冷却数据
     */
    public void clearAllCooldowns() {
        cooldowns.clear();
    }

    /**
     * 清理过期的冷却记录（用于定期清理）
     */
    public void cleanupExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
            Map<String, Long> playerCooldowns = entry.getValue();
            playerCooldowns.entrySet().removeIf(e -> e.getValue() <= currentTime);

            // 如果玩家没有任何冷却记录了，移除整个条目
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(entry.getKey());
            }
        }
    }
}
