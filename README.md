# MiaoMenu

<div align="center">

**高效 · 跨平台 · 现代化**

一个支持 Java 和 Bedrock (基岩版) 的现代化菜单插件，专为 Spigot/Paper 服务器设计。

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![BStats](https://bstats.org/api/bukkit/28979/shields.svg)](https://bstats.org/plugin/bukkit/MingLiPro/28979)

</div>

## ✨ 特性

- 🌍 **跨平台支持**: 完美适配 Java版和通过 Floodgate/Geyser 连接的基岩版玩家。
  - Java版: 支持 1.20.5+ 的新版 GUI 组件，体验丝滑。
  - 基岩版: 原生 Form 表单支持。
- ⚡ **热重载**: 监听配置文件变更，无需重启服务器即可刷新菜单。
- 🔧 **语言系统**: 完全基于 `config.yml` 的语言配置，支持自定义颜色代码和占位符。
- 🕹️ **菜单钟表**: 玩家专属物品，点击即可打开默认菜单。支持死亡保留与找回。
- 🛡️ **智能配置更新**: 内置版本检测机制，当配置文件结构更新时自动保留兼容性或提示覆盖。
- 📊 **详细统计**: 集成 BStats，后台默默统计服务器核心、版本及玩家分布，助我们优化插件。

## 📖 用法

安装后，插件会自动生成 `config.yml` 和菜单文件夹。

### 命令

| 命令 | 描述 | 权限 |
| :--- | :--- | :--- |
| `/dgm open <菜单名>` | 为自己打开指定菜单 | `dgm.use` |
| `/dgm reload` | 重载配置和所有菜单文件 | `dgm.reload` |
| `/dgm help` | 查看帮助信息 | 无 |

## 🤝 贡献

欢迎提交 Issue 或 Pull Request 来帮助完善 MiaoMenu。

## 📄 版权协议

本项目采用 MIT 协议开源。

---

<div align="center">
Made with ❤️ by MingLi
</div>