package com.hxstar.fixeditem.manager;

import com.hxstar.fixeditem.HxFixedItem;
import com.hxstar.fixeditem.model.FixedItemData;
import com.hxstar.fixeditem.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 配置管理器
 * 负责管理插件配置文件的读取和保存
 */
public class ConfigManager {

    private final HxFixedItem plugin;
    private FileConfiguration config;
    private File configFile;

    // 配置缓存
    private Set<String> enabledWorlds;
    private Map<String, FixedItemData> fixedItems;
    private int checkInterval;
    private boolean debug;

    public ConfigManager(HxFixedItem plugin) {
        this.plugin = plugin;
        this.enabledWorlds = new HashSet<>();
        this.fixedItems = new HashMap<>();
    }

    /**
     * 加载配置文件
     */
    public void loadConfig() {
        // 保存默认配置
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // 加载启用的世界列表
        loadEnabledWorlds();

        // 加载固定物品配置
        loadFixedItems();

        // 加载其他设置
        checkInterval = config.getInt("settings.check-interval", 5);
        debug = config.getBoolean("settings.debug", false);

        if (debug) {
            plugin.getLogger().info("配置加载完成！");
            plugin.getLogger().info("启用的世界: " + enabledWorlds);
            plugin.getLogger().info("固定物品数量: " + fixedItems.size());
        }
    }

    /**
     * 加载启用的世界列表
     */
    private void loadEnabledWorlds() {
        enabledWorlds.clear();
        List<String> worlds = config.getStringList("enabled-worlds");
        enabledWorlds.addAll(worlds);
    }

    /**
     * 加载固定物品配置
     */
    private void loadFixedItems() {
        fixedItems.clear();
        ConfigurationSection itemsSection = config.getConfigurationSection("fixed-items");

        if (itemsSection == null) {
            plugin.getLogger().warning("配置文件中未找到 fixed-items 节点！");
            return;
        }

        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
            if (itemSection == null) continue;

            try {
                FixedItemData itemData = parseFixedItemData(itemId, itemSection);
                fixedItems.put(itemId, itemData);

                if (debug) {
                    plugin.getLogger().info("加载固定物品: " + itemId + " -> 槽位 " + itemData.getSlot());
                }
            } catch (Exception e) {
                plugin.getLogger().severe("加载固定物品 " + itemId + " 时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 解析固定物品数据
     */
    private FixedItemData parseFixedItemData(String itemId, ConfigurationSection section) {
        FixedItemData data = new FixedItemData(itemId);

        // 基础属性
        data.setSlot(section.getInt("slot", 8));

        // 物品材质
        String materialName = section.getString("material", "NETHER_STAR");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("无效的材质: " + materialName + "，使用默认材质 NETHER_STAR");
            material = Material.NETHER_STAR;
        }
        data.setMaterial(material);

        // 物品名称
        data.setDisplayName(ColorUtil.colorize(section.getString("display-name", "&e固定物品")));

        // 物品描述
        List<String> lore = section.getStringList("lore");
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ColorUtil.colorize(line));
        }
        data.setLore(coloredLore);

        // 自定义模型数据
        data.setCustomModelData(section.getInt("custom-model-data", 0));

        // 是否发光
        data.setGlowing(section.getBoolean("glowing", false));

        // 左键命令配置
        ConfigurationSection leftClickSection = section.getConfigurationSection("left-click");
        if (leftClickSection != null) {
            data.setLeftClickEnabled(leftClickSection.getBoolean("enabled", false));
            data.setLeftClickCommands(leftClickSection.getStringList("commands"));
            data.setLeftClickAsConsole(leftClickSection.getBoolean("as-console", false));
            data.setLeftClickCooldown(leftClickSection.getInt("cooldown", 0));

            // 左键音效配置
            String leftSoundName = leftClickSection.getString("sound", "");
            if (!leftSoundName.isEmpty()) {
                try {
                    data.setLeftClickSound(Sound.valueOf(leftSoundName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的音效: " + leftSoundName);
                }
            }
            data.setLeftClickSoundVolume((float) leftClickSection.getDouble("sound-volume", 1.0));
            data.setLeftClickSoundPitch((float) leftClickSection.getDouble("sound-pitch", 1.0));
        }

        // 右键命令配置
        ConfigurationSection rightClickSection = section.getConfigurationSection("right-click");
        if (rightClickSection != null) {
            data.setRightClickEnabled(rightClickSection.getBoolean("enabled", true));
            data.setRightClickCommands(rightClickSection.getStringList("commands"));
            data.setRightClickAsConsole(rightClickSection.getBoolean("as-console", false));
            data.setRightClickCooldown(rightClickSection.getInt("cooldown", 3));

            // 右键音效配置
            String rightSoundName = rightClickSection.getString("sound", "UI_BUTTON_CLICK");
            if (!rightSoundName.isEmpty()) {
                try {
                    data.setRightClickSound(Sound.valueOf(rightSoundName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的音效: " + rightSoundName);
                    data.setRightClickSound(Sound.UI_BUTTON_CLICK);
                }
            }
            data.setRightClickSoundVolume((float) rightClickSection.getDouble("sound-volume", 1.0));
            data.setRightClickSoundPitch((float) rightClickSection.getDouble("sound-pitch", 1.0));
        }

        // 保护设置
        ConfigurationSection protectionSection = section.getConfigurationSection("protection");
        if (protectionSection != null) {
            data.setPreventDrop(protectionSection.getBoolean("prevent-drop", true));
            data.setPreventMove(protectionSection.getBoolean("prevent-move", true));
            data.setPreventDeath(protectionSection.getBoolean("prevent-death", true));
            data.setPreventContainer(protectionSection.getBoolean("prevent-container", true));
        } else {
            // 默认全部保护
            data.setPreventDrop(true);
            data.setPreventMove(true);
            data.setPreventDeath(true);
            data.setPreventContainer(true);
        }

        return data;
    }

    /**
     * 检查世界是否启用
     */
    public boolean isWorldEnabled(String worldName) {
        // 如果列表为空，则所有世界都启用
        if (enabledWorlds.isEmpty()) {
            return true;
        }
        return enabledWorlds.contains(worldName);
    }

    /**
     * 获取所有固定物品数据
     */
    public Map<String, FixedItemData> getFixedItems() {
        return fixedItems;
    }

    /**
     * 根据ID获取固定物品数据
     */
    public FixedItemData getFixedItemData(String itemId) {
        return fixedItems.get(itemId);
    }

    /**
     * 根据槽位获取固定物品数据
     */
    public FixedItemData getFixedItemBySlot(int slot) {
        for (FixedItemData data : fixedItems.values()) {
            if (data.getSlot() == slot) {
                return data;
            }
        }
        return null;
    }

    /**
     * 获取所有固定槽位
     */
    public Set<Integer> getFixedSlots() {
        Set<Integer> slots = new HashSet<>();
        for (FixedItemData data : fixedItems.values()) {
            slots.add(data.getSlot());
        }
        return slots;
    }

    /**
     * 获取检查间隔（秒）
     */
    public int getCheckInterval() {
        return checkInterval;
    }

    /**
     * 是否开启调试模式
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 获取启用的世界列表
     */
    public Set<String> getEnabledWorlds() {
        return enabledWorlds;
    }
}
