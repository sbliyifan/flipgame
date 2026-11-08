# 实验五 RecyclerView 翻牌游戏 APP

姓名：李易凡　学号：2024110517　包名：`cn.edu.sicnu.flipgame`

## 功能

- **24 张牌**的翻牌配对游戏，用 `RecyclerView` + `GridLayoutManager` 网格排布；
- **两种玩法**：2 张牌玩法（12 对）、3 张牌玩法（8 组）；
- **菜单 + 对话框**：主界面 ActionBar 菜单「关于」弹出关于对话框；游戏界面菜单「重新开始」；
- **横竖屏自适应**：竖屏 4 列 6 行，横屏 6 列 4 行，旋转后牌面状态与分数不丢失；
- **MVC 结构**：Model / View / Adapter / Controller 职责分离。

## 目录

```
FlipGame/
├── app/src/main/
│   ├── java/cn/edu/sicnu/flipgame/
│   │   ├── MainActivity.kt           Controller：入口 + 菜单 + 关于对话框
│   │   ├── GameActivity.kt           Controller：游戏界面控制、状态保存
│   │   ├── model/Card.kt             Model：单张牌 + match 匹配规则
│   │   ├── model/CardMatchingGame.kt Model：发牌、计分、chooseCardAtIndex、reset
│   │   └── view/CardAdapter.kt       Adapter：RecyclerView.Adapter 派生类
│   ├── res/layout/
│   │   ├── activity_main.xml
│   │   ├── activity_game.xml
│   │   └── item_card.xml
│   ├── res/menu/{menu_main.xml, menu_game.xml}
│   └── res/values/{strings.xml, colors.xml, themes.xml}
└── README.md
```

## 与实验要求的对应

| 要求 | 实现位置 |
| --- | --- |
| (3) MainActivity 菜单 + 关于对话框 | `MainActivity.onCreateOptionsMenu()` / `showAboutDialog()` |
| (4a) RecyclerView 实现 24 张牌 | `activity_game.xml` 的 `rv_cards` + `GridLayoutManager` |
| (4b) 派生 Adapter 并重载方法 | `CardAdapter` 重写 `onCreateViewHolder` / `onBindViewHolder` / `getItemCount`，构造函数接收点击回调 |
| (4c) Model 新增 `reset()` | `CardMatchingGame.reset()` |
| (4d) Controller 支持横竖屏不同牌数 | `GameActivity.onCreate()` 按 `Configuration.orientation` 选 4 列或 6 列；`onSaveInstanceState` 保存牌面状态 |
| (5) 3 张牌玩法 | `CardMatchingGame(cardsPerGroup = 3)`；匹配规则集中在 `Card.match()` 与 `chooseCardAtIndex()` |

## 主题说明

`themes.xml` 的 parent 使用 `Theme.MaterialComponents.DayNight.DarkActionBar.Bridge`。
新版 Android Studio 默认主题为 Material3，直接给 Button 设置 `android:background` 会被主题样式覆盖而不生效，换成 Bridge 主题即可正常显示。

## 运行

用 Android Studio 打开本目录 → 等待 Gradle 同步 → Run。
未包含 `gradle-wrapper.jar`（二进制），若提示 Gradle 版本缺失，在 `File > Project Structure > Project` 里选择本机已有版本即可。

## 分支

- `main`：初始提交
- `dev`：本次作业开发分支（演示"新建 Dev 分支开发 → PR 合并回 main"的流程）
