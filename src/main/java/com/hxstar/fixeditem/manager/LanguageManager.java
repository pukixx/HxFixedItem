package com.hxstar.fixeditem.manager;

import com.hxstar.fixeditem.HxFixedItem;
import com.hxstar.fixeditem.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 语言管理器
 * 负责管理插件的多语言消息
 * 支持 & 颜色代码和 HEX 颜色代码
 */
public class LanguageManager {

    private final HxFixedItem plugin;
    private FileConfiguration langConfig;
    private File langFile;
    private final Map<String, String> messages;

    public LanguageManager(HxFixedItem plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }

    /**
     * 加载语言文件
     */
    public void loadLanguage() {
        langFile = new File(plugin.getDataFolder(), "lang.yml");

        // 如果文件不存在，从资源中复制
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // 加载默认值以防止缺少配置项
        InputStream defaultStream = plugin.getResource("lang.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defaultConfig);
        }

        // 缓存所有消息
        cacheMessages();

        if (plugin.getConfigManager() != null && plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("语言文件加载完成！共加载 " + messages.size() + " 条消息。");
        }
    }

    /**
     * 缓存所有消息
     */
    private void cacheMessages() {
        messages.clear();

        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isString(key)) {
                String message = langConfig.getString(key);
                if (message != null) {
                    messages.put(key, ColorUtil.colorize(message));
                }
            }
        }
    }

    /**
     * 获取消息（已着色）
     *
     * @param key 消息键
     * @return 着色后的消息
     */
    public String getMessage(String key) {
        return messages.getOrDefault(key, "§c消息未找到: " + key);
    }

    /**
     * 获取消息并替换占位符
     *
     * @param key          消息键
     * @param placeholders 占位符映射 (占位符 -> 值)
     * @return 处理后的消息
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }

        return message;
    }

    /**
     * 获取消息并替换单个占位符
     *
     * @param key         消息键
     * @param placeholder 占位符
     * @param value       替换值
     * @return 处理后的消息
     */
    public String getMessage(String key, String placeholder, String value) {
        return getMessage(key).replace(placeholder, value);
    }

    /**
     * 获取消息并替换多个占位符
     *
     * @param key          消息键
     * @param replacements 占位符和值的交替数组 (placeholder1, value1, placeholder2, value2, ...)
     * @return 处理后的消息
     */
    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);

        if (replacements != null && replacements.length >= 2) {
            for (int i = 0; i < replacements.length - 1; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        return message;
    }

    /**
     * 获取前缀
     */
    public String getPrefix() {
        return getMessage("prefix");
    }

    /**
     * 获取带前缀的消息
     *
     * @param key 消息键
     * @return 带前缀的消息
     */
    public String getPrefixedMessage(String key) {
        return getPrefix() + getMessage(key);
    }

    /**
     * 获取带前缀的消息并替换占位符
     *
     * @param key          消息键
     * @param replacements 占位符和值的交替数组
     * @return 带前缀的处理后消息
     */
    public String getPrefixedMessage(String key, String... replacements) {
        return getPrefix() + getMessage(key, replacements);
    }

    /**
     * 重新加载语言文件
     */
    public void reload() {
        loadLanguage();
    }

    /**
     * 获取语言配置
     */
    public FileConfiguration getLangConfig() {
        return langConfig;
    }
}
