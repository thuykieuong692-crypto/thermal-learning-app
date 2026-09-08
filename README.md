# 热管理学习 · 原生 Android App（独立安装包）

这是一个**真正的原生 Android 应用**，不是网页跳转壳：

- 前端 HTML/JS 直接打包进 APK，在本地 `WebView` 里运行（独立图标、全屏、独立进程）
- 打开即用，数据从腾讯云 SCF 后端实时拉取并缓存，**断网也能看上次内容**
- 改任务状态会写回你的飞书多维表格，手机/电脑/网页多端同步
- 资料外链（B站/YouTube 等）自动用系统浏览器打开

> 后端接口地址在 `app/src/main/assets/index.html` 顶部的 `API_BASE` 常量里（已写死你的 SCF 地址）。
> 换后端只需改这一行，重新构建即可。

## 工程结构
```
thermal-app/
├── app/src/main/assets/index.html   # 前端（已改为绝对接口地址 + 外链桥接）
├── app/src/main/java/.../MainActivity.java  # 原生 WebView 壳
├── app/src/main/res/...             # 图标 / 主题
├── app/build.gradle                 # 签名配置（读 CI 环境变量）
├── build.gradle / settings.gradle   # Gradle 工程
└── .github/workflows/build.yml      # GitHub Actions 自动打包签名
```

## 如何出 APK（云端，零安装）

1. 把本目录推到 GitHub 仓库（见下方命令）
2. 仓库里点 **Actions → Build Signed APK → Run workflow**
3. 跑完后在 **Artifacts** 里下载 `thermal-learning-apk`（里面的 `.apk`）
4. 手机允许「未知来源」安装即可

### 推送命令（在本机执行）
```bash
cd thermal-app
git init -b main
git add -A
git commit -m "thermal learning native app"
git remote add origin https://github.com/<你的用户名>/<仓库名>.git
git push -u origin main
```

## 说明
- 每次 CI 构建会**重新生成签名密钥**，所以不同次构建出的 APK 签名不同。
  直接覆盖安装旧版时会提示「签名冲突」，需先卸载再装。
  若想保留同一签名以便后续增量升级，可把 `release.keystore` 作为仓库 **Secret** 注入
  （在 `build.yml` 里改用 `${{ secrets.KEYSTORE }}` 而非 CI 现场生成）。
- 想本地用 Android Studio 出包：用 Android Studio 打开本目录 → Build → Generate Signed Bundle / APK。
- 飞书凭证（App Secret）只在 SCF 后端，不在 App 里，分享给别人也不会泄露。
