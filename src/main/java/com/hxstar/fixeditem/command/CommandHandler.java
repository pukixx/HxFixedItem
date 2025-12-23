package com.hxstar.fixeditem.command;

import com.hxstar.fixeditem.HxFixedItem;
import com.hxstar.fixeditem.manager.LanguageManager;
import com.hxstar.fixeditem.model.FixedItemData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 命令处理器
 * 处理插件的所有命令
 */
public class CommandHandler implements CommandExecutor, TabCompleter {

    private final HxFixedItem plugin;
    private final LanguageManager lang;

    public CommandHandler(HxFixedItem plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sender.sendMessage(lang.getPrefixedMessage("command.unknown"));
                sendHelp(sender);
                break;
        }

        return true;
    }

    /**
     * 处理重载命令
     */
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("hxfixeditem.admin")) {
            sender.sendMessage(lang.getPrefixedMessage("command.no-permission"));
            return;
        }

        try {
            plugin.reload();
            sender.sendMessage(lang.getPrefixedMessage("command.reload-success"));
        } catch (Exception e) {
            sender.sendMessage(lang.getPrefixedMessage("command.reload-failed", "{error}", e.getMessage()));
            plugin.getLogger().severe("重载配置时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理移除命令
     */
    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hxfixeditem.admin")) {
            sender.sendMessage(lang.getPrefixedMessage("command.no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(lang.getPrefixedMessage("command.remove-usage"));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(lang.getPrefixedMessage("command.player-not-found", "{player}", targetName));
            return;
        }

        // 如果指定了物品ID，只移除特定物品
        if (args.length >= 3) {
            String itemId = args[2];
            FixedItemData itemData = plugin.getConfigManager().getFixedItemData(itemId);

            if (itemData == null) {
                sender.sendMessage(lang.getPrefixedMessage("command.item-not-found", "{item}", itemId));
                return;
            }

            plugin.getFixedItemManager().removeFixedItem(target, itemId);
            sender.sendMessage(lang.getPrefixedMessage("command.remove-item-success",
                    "{player}", target.getName(),
                    "{item}", itemId));
        } else {
            // 移除所有固定物品
            plugin.getFixedItemManager().removeAllFixedItems(target);
            sender.sendMessage(lang.getPrefixedMessage("command.remove-all-success", "{player}", target.getName()));
        }
    }

    /**
     * 处理给予命令
     */
    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hxfixeditem.admin")) {
            sender.sendMessage(lang.getPrefixedMessage("command.no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(lang.getPrefixedMessage("command.give-usage"));
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(lang.getPrefixedMessage("command.player-not-found", "{player}", targetName));
            return;
        }

        // 检查目标玩家所在世界是否启用
        if (!plugin.getConfigManager().isWorldEnabled(target.getWorld().getName())) {
            sender.sendMessage(lang.getPrefixedMessage("command.world-not-enabled",
                    "{world}", target.getWorld().getName()));
            return;
        }

        plugin.getFixedItemManager().giveFixedItems(target);
        sender.sendMessage(lang.getPrefixedMessage("command.give-success", "{player}", target.getName()));
    }

    /**
     * 发送帮助信息
     */
    private void sendHelp(CommandSender sender) {
        List<String> helpMessages = Arrays.asList(
                "",
                lang.getMessage("help.header"),
                "",
                lang.getMessage("help.reload"),
                lang.getMessage("help.remove"),
                lang.getMessage("help.give"),
                lang.getMessage("help.help"),
                "",
                lang.getMessage("help.footer")
        );

        for (String message : helpMessages) {
            sender.sendMessage(message);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("hxfixeditem.admin")) {
            return completions;
        }

        if (args.length == 1) {
            // 子命令补全
            completions.addAll(Arrays.asList("reload", "remove", "give", "help"));
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("remove") || subCommand.equals("give")) {
                // 玩家名补全
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
                return filterCompletions(completions, args[1]);
            }
        }

        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("remove")) {
                // 物品ID补全
                Map<String, FixedItemData> fixedItems = plugin.getConfigManager().getFixedItems();
                completions.addAll(fixedItems.keySet());
                return filterCompletions(completions, args[2]);
            }
        }

        return completions;
    }

    /**
     * 过滤补全建议
     */
    private List<String> filterCompletions(List<String> completions, String input) {
        if (input == null || input.isEmpty()) {
            return completions;
        }

        String lowerInput = input.toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}
