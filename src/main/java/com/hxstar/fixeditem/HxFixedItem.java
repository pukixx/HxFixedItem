package com.hxstar.fixeditem;

import com.hxstar.fixeditem.command.CommandHandler;
import com.hxstar.fixeditem.listener.ItemProtectionListener;
import com.hxstar.fixeditem.listener.PlayerEventListener;
import com.hxstar.fixeditem.manager.ConfigManager;
import com.hxstar.fixeditem.manager.CooldownManager;
import com.hxstar.fixeditem.manager.FixedItemManager;
import com.hxstar.fixeditem.manager.LanguageManager;
import com.hxstar.fixeditem.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * HxFixedItem 主类
 * 背包槽位物品固定插件
 * 作者: @幻星
 */
public class HxFixedItem extends JavaPlugin {

    private static HxFixedItem instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private FixedItemManager fixedItemManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        instance = this;

        // 初始化管理器
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.fixedItemManager = new FixedItemManager(this);
        this.cooldownManager = new CooldownManager(this);

        // 加载配置
        configManager.loadConfig();
        languageManager.loadLanguage();

        // 初始化 PlaceholderAPI
        PlaceholderUtil.init();

        // 注册监听器
        registerListeners();

        // 注册命令
        registerCommands();

        // 启动定时检查任务
        startCheckTask();

        // 给所有在线玩家补充固定物品
        for (Player player : Bukkit.getOnlinePlayers()) {
            fixedItemManager.giveFixedItems(player);
        }

        // 输出启动信息
        printStartupMessage();
    }

    @Override
    public void onDisable() {
        // 取消所有任务
        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("HxFixedItem 插件已卸载！");
    }

    /**
     * 注册监听器
     */
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(this), this);
    }

    /**
     * 注册命令
     */
    private void registerCommands() {
        CommandHandler commandHandler = new CommandHandler(this);
        getCommand("hxfixeditem").setExecutor(commandHandler);
        getCommand("hxfixeditem").setTabCompleter(commandHandler);
    }

    /**
     * 启动定时检查任务
     */
    private void startCheckTask() {
        int interval = configManager.getCheckInterval();
        if (interval > 0) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    fixedItemManager.checkAndRestoreItems(player);
                }
            }, interval * 20L, interval * 20L);
        }
    }

    /**
     * 输出启动信息
     */
    private void printStartupMessage() {
        String[] banner = {
                "",
                "§b╔══════════════════════════════════════════╗",
                "§b║                                          ║",
                "§b║   §e✦ §fHxFixedItem §a已启动 §e✦              §b║",
                "§b║   §7作者: §d@TheMagic_Star幻星              §b║",
                "§b║   §7版本: §f" + getDescription().getVersion() + "                          §b║",
                "§b║                                          ║",
                "§b╚══════════════════════════════════════════╝",
                ""
        };
        for (String line : banner) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }

    /**
     * 重载插件
     */
    public void reload() {
        // 取消所有任务
        Bukkit.getScheduler().cancelTasks(this);

        // 重新加载配置
        configManager.loadConfig();
        languageManager.loadLanguage();

        // 重新启动定时检查任务
        startCheckTask();

        // 刷新所有在线玩家的固定物品
        for (Player player : Bukkit.getOnlinePlayers()) {
            fixedItemManager.refreshFixedItems(player);
        }
    }

    public static HxFixedItem getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public FixedItemManager getFixedItemManager() {
        return fixedItemManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}
