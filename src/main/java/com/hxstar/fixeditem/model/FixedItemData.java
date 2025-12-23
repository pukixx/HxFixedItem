package com.hxstar.fixeditem.model;

import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

/**
 * 固定物品数据模型
 * 存储单个固定物品的所有配置信息
 */
public class FixedItemData {

    private final String itemId;
    private int slot;
    private Material material;
    private String displayName;
    private List<String> lore;
    private int customModelData;
    private boolean glowing;

    // 左键命令配置
    private boolean leftClickEnabled;
    private List<String> leftClickCommands;
    private boolean leftClickAsConsole;
    private int leftClickCooldown;
    private Sound leftClickSound;
    private float leftClickSoundVolume;
    private float leftClickSoundPitch;

    // 右键命令配置
    private boolean rightClickEnabled;
    private List<String> rightClickCommands;
    private boolean rightClickAsConsole;
    private int rightClickCooldown;
    private Sound rightClickSound;
    private float rightClickSoundVolume;
    private float rightClickSoundPitch;

    // 保护设置
    private boolean preventDrop;
    private boolean preventMove;
    private boolean preventDeath;
    private boolean preventContainer;

    public FixedItemData(String itemId) {
        this.itemId = itemId;
        this.slot = 8;
        this.material = Material.NETHER_STAR;
        this.displayName = "固定物品";
        this.lore = new ArrayList<>();
        this.customModelData = 0;
        this.glowing = false;

        this.leftClickEnabled = false;
        this.leftClickCommands = new ArrayList<>();
        this.leftClickAsConsole = false;
        this.leftClickCooldown = 0;
        this.leftClickSound = null;
        this.leftClickSoundVolume = 1.0f;
        this.leftClickSoundPitch = 1.0f;

        this.rightClickEnabled = true;
        this.rightClickCommands = new ArrayList<>();
        this.rightClickAsConsole = false;
        this.rightClickCooldown = 3;
        this.rightClickSound = Sound.UI_BUTTON_CLICK;
        this.rightClickSoundVolume = 1.0f;
        this.rightClickSoundPitch = 1.0f;

        this.preventDrop = true;
        this.preventMove = true;
        this.preventDeath = true;
        this.preventContainer = true;
    }

    // ==================== Getter & Setter ====================

    public String getItemId() {
        return itemId;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public boolean isLeftClickEnabled() {
        return leftClickEnabled;
    }

    public void setLeftClickEnabled(boolean leftClickEnabled) {
        this.leftClickEnabled = leftClickEnabled;
    }

    public List<String> getLeftClickCommands() {
        return leftClickCommands;
    }

    public void setLeftClickCommands(List<String> leftClickCommands) {
        this.leftClickCommands = leftClickCommands;
    }

    public boolean isLeftClickAsConsole() {
        return leftClickAsConsole;
    }

    public void setLeftClickAsConsole(boolean leftClickAsConsole) {
        this.leftClickAsConsole = leftClickAsConsole;
    }

    public int getLeftClickCooldown() {
        return leftClickCooldown;
    }

    public void setLeftClickCooldown(int leftClickCooldown) {
        this.leftClickCooldown = leftClickCooldown;
    }

    public boolean isRightClickEnabled() {
        return rightClickEnabled;
    }

    public void setRightClickEnabled(boolean rightClickEnabled) {
        this.rightClickEnabled = rightClickEnabled;
    }

    public List<String> getRightClickCommands() {
        return rightClickCommands;
    }

    public void setRightClickCommands(List<String> rightClickCommands) {
        this.rightClickCommands = rightClickCommands;
    }

    public boolean isRightClickAsConsole() {
        return rightClickAsConsole;
    }

    public void setRightClickAsConsole(boolean rightClickAsConsole) {
        this.rightClickAsConsole = rightClickAsConsole;
    }

    public int getRightClickCooldown() {
        return rightClickCooldown;
    }

    public void setRightClickCooldown(int rightClickCooldown) {
        this.rightClickCooldown = rightClickCooldown;
    }

    public boolean isPreventDrop() {
        return preventDrop;
    }

    public void setPreventDrop(boolean preventDrop) {
        this.preventDrop = preventDrop;
    }

    public boolean isPreventMove() {
        return preventMove;
    }

    public void setPreventMove(boolean preventMove) {
        this.preventMove = preventMove;
    }

    public boolean isPreventDeath() {
        return preventDeath;
    }

    public void setPreventDeath(boolean preventDeath) {
        this.preventDeath = preventDeath;
    }

    public boolean isPreventContainer() {
        return preventContainer;
    }

    public void setPreventContainer(boolean preventContainer) {
        this.preventContainer = preventContainer;
    }

    public Sound getLeftClickSound() {
        return leftClickSound;
    }

    public void setLeftClickSound(Sound leftClickSound) {
        this.leftClickSound = leftClickSound;
    }

    public float getLeftClickSoundVolume() {
        return leftClickSoundVolume;
    }

    public void setLeftClickSoundVolume(float leftClickSoundVolume) {
        this.leftClickSoundVolume = leftClickSoundVolume;
    }

    public float getLeftClickSoundPitch() {
        return leftClickSoundPitch;
    }

    public void setLeftClickSoundPitch(float leftClickSoundPitch) {
        this.leftClickSoundPitch = leftClickSoundPitch;
    }

    public Sound getRightClickSound() {
        return rightClickSound;
    }

    public void setRightClickSound(Sound rightClickSound) {
        this.rightClickSound = rightClickSound;
    }

    public float getRightClickSoundVolume() {
        return rightClickSoundVolume;
    }

    public void setRightClickSoundVolume(float rightClickSoundVolume) {
        this.rightClickSoundVolume = rightClickSoundVolume;
    }

    public float getRightClickSoundPitch() {
        return rightClickSoundPitch;
    }

    public void setRightClickSoundPitch(float rightClickSoundPitch) {
        this.rightClickSoundPitch = rightClickSoundPitch;
    }
}
