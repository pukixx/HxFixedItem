# HxFixedItem
为玩家背包的指定槽位放置固定物品，点击可执行命令。 物品无法丢弃、移动、死亡不掉落，适用于服务器菜单、返回大厅、商店入口等场景。
固定物品插件文档
📖 简介
为玩家背包的指定槽位放置固定物品，点击可执行命令。
物品无法丢弃、移动、死亡不掉落，适用于服务器菜单、返回大厅、商店入口等场景。

✨ 特色功能
🎨 自由定制物品外观
任意材质、自定义名称与描述
支持 HEX 颜色（&#FFD700）和传统颜色代码（&a）混用
支持 CustomModelData，搭配资源包打造专属图标
可选附魔光效
📦 灵活的槽位配置
快捷栏（0-8）和背包格（9-35）均可配置
多物品互不冲突，各占其位
被挤占时自动归位，确保位置固定
🌍 世界隔离机制
可指定生效世界，其他世界自动隐藏
适合大厅/游戏分区服，不同区域显示不同道具
留空则全服通用
🛡️ 完善的物品保护
Q 键丢不掉、拖不动、死亡不掉落
无法放入箱子、漏斗等容器
定时检测 + 事件监听双重保障
🎮 RPG 服应用场景
场景	说明
技能快捷栏	固定技能书到 1-4 号槽位，左键/右键触发不同技能命令，配合 MythicMobs 或 SkillAPI 实现技能释放
回城石/传送道具	8 号位放置“回城石”，右键执行 /spawn 或 /warp home，可设置冷却防止滥用
背包扩展入口	0 号位放置箱子图标，点击打开 ChestCommands/TrMenu 菜单，模拟额外仓库页面
任务追踪器	中间位置放一本书，点击打开任务菜单，描述中使用 PAPI 显示当前任务进度
货币/商店快捷入口	绿宝石图标，lore 显示 %vault_eco_balance% 实时余额，点击打开商店 GUI
⚙️ 配置示例
yaml:
# config.yml
settings:
  check-interval: 5    # 检查间隔（秒），0 为禁用
  debug: false

# 留空 = 全服启用
enabled-worlds: []

fixed-items:
  menu:                              # 物品ID（自定义）
    slot: 4                          # 槽位 0-8快捷栏，9-35背包
    material: NETHER_STAR
    display-name: "&#FFD700✦ 服务器菜单"
    lore:
      - ""
      - "&7右键打开菜单"
      - "&7余额: &a%vault_eco_balance%"   # 支持PAPI
    custom-model-data: 0
    glowing: true                    # 附魔光效
    
    left-click:
      enabled: true
      commands:
        - "say {player} 左键了"
      as-console: true               # true=后台执行
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
