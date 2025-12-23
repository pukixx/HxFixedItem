package com.hxstar.fixeditem.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI 工具类
 * 处理占位符解析
 */
public class PlaceholderUtil {

    private static boolean papiEnabled = false;

    /**
     * 初始化 - 检查 PlaceholderAPI 是否可用
     */
    public static void init() {
        papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (papiEnabled) {
            Bukkit.getLogger().info("[HxFixedItem] 已检测到 PlaceholderAPI，占位符功能已启用");
        }
    }

    /**
     * 检查 PlaceholderAPI 是否启用
     */
    public static boolean isPapiEnabled() {
        return papiEnabled;
    }

    /**
     * 解析占位符
     *
     * @param player 玩家
     * @param text   要解析的文本
     * @return 解析后的文本
     */
    public static String parsePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (papiEnabled && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }
}
