# HxFixedItem

为玩家背包的指定槽位放置固定物品，点击可执行命令。物品无法丢弃、移动，死亡不掉落，适用于服务器菜单、返回大厅、商店入口等场景。

---

## 📖 简介
HxFixedItem 让你可以在玩家背包或快捷栏的指定槽位放置不可移动的固定物品，并在左右键点击时触发命令（可后台执行）。物品受保护（无法丢弃、拖动、放入容器、死亡不掉落），支持自定义显示效果与多世界隔离。

---

## ✨ 特色功能
- 自由定制物品外观：材质、显示名、描述（lore）
- 支持 HEX 颜色（例如 `&#FFD700`）与传统颜色代码（例如 `&a`）混用
- 支持 `CustomModelData` 搭配资源包显示自定义图标
- 可选附魔光效（glowing）
- 灵活的槽位配置：快捷栏（0–8）与背包格（9–35）
- 多物品互不冲突，各占其位；被挤占时自动归位
- 世界隔离（可指定生效世界；留空则全服生效）
- 完善的物品保护：禁止丢弃、禁止移动、禁止放入容器、死亡不掉落
- 事件监听 + 定时检测双重保障

---

## 🎮 典型应用场景

| 场景 | 说明 |
|---|---|
| 技能快捷栏 | 固定技能书到 1–4 号槽位，左键/右键触发不同技能命令，配合 MythicMobs 或 SkillAPI 使用。 |
| 回城石 / 传送道具 | 在 8 号位放置“回城石”，右键执行 `/spawn` 或 `/warp home`，可设置冷却避免滥用。 |
| 背包扩展入口 | 在 0 号位放置箱子图标，点击打开 ChestCommands/TrMenu 菜单，模拟额外仓库页面。 |
| 任务追踪器 | 中间放一本书，点击打开任务菜单，lore 使用 PAPI 显示当前任务进度。 |
| 货币/商店快捷 | 使用绿宝石图标，lore 显示 `%vault_eco_balance%` 实时余额，点击打开商店 GUI。 |

---

## ⚙️ 配置示例（config.yml）

```yaml
# config.yml
settings:
  check-interval: 5    # 检查间隔（秒），0 为禁用
  debug: false

# 留空 = 全服启用
enabled-worlds: []

fixed-items:
  menu:                              # 物品ID（自定义）
    slot: 4                          # 槽位：0-8 快捷栏，9-35 背包
    material: NETHER_STAR
    # display-name 可使用 HEX（&#FFD700）或传统颜色代码（&a）
    # 例如: "&#FFD700✦ 服务器菜单" 或 "§6✦ 服务器菜单"
    display-name: "✦ 服务器菜单"
    lore:
      - ""
      - "&7右键打开菜单"
      - "&7余额: &a%vault_eco_balance%"   # 支持 PlaceholderAPI
    custom-model-data: 0
    glowing: true                    # 附魔光效

    left-click:
      enabled: true
      commands:
        - "say {player} 左键了"
      as-console: true               # true = 后台执行（控制台）
      cooldown: 2
      sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
      sound-volume: 1.0
      sound-pitch: 1.2

    right-click:
      enabled: true
      commands:
        - "menu open main {player}"
      as-console: true
      cooldown: 1
      sound: "UI_BUTTON_CLICK"
      sound-volume: 1.0
      sound-pitch: 1.0

    protection:
      prevent-drop: true
      prevent-move: true
      prevent-death: true
      prevent-container: true
```

---

## 安装与使用
1. 将插件的 jar 放入服务器的 `plugins/` 目录。
2. 启动服务器生成默认配置文件。
3. 编辑 `config.yml` 按需添加 `fixed-items` 项，重载或重启插件使配置生效。

---

## 说明与建议
- display-name 中使用 HEX 颜色（如 `&#FFD700`）需确保你的文本解析器/占位符插件支持该语法；否则可用经典颜色代码（如 `&6` 或 `§6`）。
- 如需用自定义模型数据显示特殊图标，请配合资源包并设置 `custom-model-data`。
- 对于需要权限或占用特殊命令的场景，建议将命令以控制台执行（`as-console: true`）并在命令内做权限校验。

---

如果你希望我直接把这个文件提交到仓库（替换/更新 README.md），我可以帮你创建一次提交或 PR。请告诉我你要直接替换（直接推送到 main）还是创建一个分支并发起 PR.