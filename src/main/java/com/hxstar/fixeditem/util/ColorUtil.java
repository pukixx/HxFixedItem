package com.hxstar.fixeditem.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 颜色工具类
 * 支持传统 & 颜色代码和 HEX 颜色代码
 * HEX 格式支持: &#RRGGBB 或 {#RRGGBB} 或 <#RRGGBB>
 */
public class ColorUtil {

    // HEX 颜色正则表达式模式
    private static final Pattern HEX_PATTERN_AMPERSAND = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_BRACKET = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");
    private static final Pattern HEX_PATTERN_ANGLE = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_PATTERN_PLAIN = Pattern.compile("#([A-Fa-f0-9]{6})");

    /**
     * 将文本中的颜色代码转换为 Minecraft 颜色
     * 支持:
     * - & 颜色代码 (如 &a, &b, &l 等)
     * - HEX 颜色代码 (如 &#FF5555, {#FF5555}, <#FF5555>)
     *
     * @param text 要转换的文本
     * @return 转换后的文本
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 先处理 HEX 颜色
        text = translateHexColors(text);

        // 再处理传统 & 颜色代码
        text = ChatColor.translateAlternateColorCodes('&', text);

        return text;
    }

    /**
     * 转换所有 HEX 颜色格式
     *
     * @param text 要转换的文本
     * @return 转换后的文本
     */
    private static String translateHexColors(String text) {
        // 处理 &#RRGGBB 格式
        text = translateHexPattern(text, HEX_PATTERN_AMPERSAND);

        // 处理 {#RRGGBB} 格式
        text = translateHexPattern(text, HEX_PATTERN_BRACKET);

        // 处理 <#RRGGBB> 格式
        text = translateHexPattern(text, HEX_PATTERN_ANGLE);

        return text;
    }

    /**
     * 使用指定模式转换 HEX 颜色
     *
     * @param text    要转换的文本
     * @param pattern HEX 颜色模式
     * @return 转换后的文本
     */
    private static String translateHexPattern(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = translateHexCode(hexCode);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    /**
     * 将 HEX 代码转换为 Minecraft 颜色代码
     *
     * @param hexCode 6位 HEX 代码 (不含 #)
     * @return Minecraft 颜色代码字符串
     */
    private static String translateHexCode(String hexCode) {
        try {
            // 使用 BungeeCord API 的 ChatColor.of() 方法
            return ChatColor.of("#" + hexCode).toString();
        } catch (Exception e) {
            // 如果转换失败，返回空字符串
            return "";
        }
    }

    /**
     * 移除文本中的所有颜色代码
     *
     * @param text 要处理的文本
     * @return 移除颜色代码后的文本
     */
    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 先移除 HEX 颜色
        text = HEX_PATTERN_AMPERSAND.matcher(text).replaceAll("");
        text = HEX_PATTERN_BRACKET.matcher(text).replaceAll("");
        text = HEX_PATTERN_ANGLE.matcher(text).replaceAll("");
        text = HEX_PATTERN_PLAIN.matcher(text).replaceAll("");

        // 再移除传统颜色代码
        text = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));

        return text;
    }

    /**
     * 创建渐变色文本
     *
     * @param text      要应用渐变的文本
     * @param startHex  起始颜色 (如 "FF0000")
     * @param endHex    结束颜色 (如 "0000FF")
     * @return 渐变色文本
     */
    public static String gradient(String text, String startHex, String endHex) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        int[] startRGB = hexToRGB(startHex);
        int[] endRGB = hexToRGB(endHex);

        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            // 计算当前位置的颜色
            double ratio = (double) i / (length - 1);
            int r = (int) (startRGB[0] + (endRGB[0] - startRGB[0]) * ratio);
            int g = (int) (startRGB[1] + (endRGB[1] - startRGB[1]) * ratio);
            int b = (int) (startRGB[2] + (endRGB[2] - startRGB[2]) * ratio);

            String hexColor = String.format("%02X%02X%02X", r, g, b);
            result.append(translateHexCode(hexColor)).append(c);
        }

        return result.toString();
    }

    /**
     * 将 HEX 代码转换为 RGB 数组
     *
     * @param hex HEX 代码 (不含 #)
     * @return RGB 数组 [R, G, B]
     */
    private static int[] hexToRGB(String hex) {
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    /**
     * 获取彩虹色文本
     *
     * @param text 要应用彩虹色的文本
     * @return 彩虹色文本
     */
    public static String rainbow(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] rainbowColors = {"FF0000", "FF7F00", "FFFF00", "00FF00", "0000FF", "4B0082", "9400D3"};
        StringBuilder result = new StringBuilder();
        int colorIndex = 0;

        for (char c : text.toCharArray()) {
            if (c != ' ') {
                result.append(translateHexCode(rainbowColors[colorIndex % rainbowColors.length]));
                colorIndex++;
            }
            result.append(c);
        }

        return result.toString();
    }
}
