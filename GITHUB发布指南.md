# 热管理学习 App · GitHub 发布与出包指南

本指南帮你在 GitHub 建一个空仓库，把本地已写好的原生 Android App 工程推上去，再用 GitHub Actions 云端编译出**已签名的独立 APK**（零安装、不跳网址、原生 WebView 壳）。

---

## 第 1 步：注册 / 登录 GitHub
- 打开 https://github.com
- 没有账号点右上角 **Sign up** 免费注册（1 分钟，只需邮箱）
- 已有账号点 **Sign in** 登录

## 第 2 步：新建一个空仓库
1. 登录后，点页面右上角 **"+"** 图标 → 选 **New repository**
   （或首页中间的绿色 **New** / **Create repository** 按钮）
2. 填写：
   - **Repository name（仓库名）**：输入英文，例如 `thermal-learning-app`
     ⚠️ 只能用字母/数字/中划线，不能含中文、空格或中文标点
   - **Description（描述，可选）**：航空发动机热管理学习系统 原生 Android App
   - **Visibility（可见性）**：选 **Public**（公开，CI 免费；选 Private 也行，免费额度内同样可跑 Actions）
3. **关键：保持仓库为空**
   - 下方 "Initialize this repository with:" 区域
   - **不要**勾选 "Add a README file"
   - **不要**勾选 "Add .gitignore"
   - **不要**勾选 "Choose a license"
   - （本地已有完整工程，勾了会产生初始提交导致推送冲突）
4. 点绿色 **Create repository** 按钮

## 第 3 步：拿到仓库地址
创建后会跳到一个快速上手页，里面有一行类似：
```
https://github.com/<你的用户名>/<仓库名>.git
```
记住你的 `<用户名>` 和 `<仓库名>`，下一步要用。

## 第 4 步：推送代码（一键脚本）
在本机 `thermal-app` 目录下运行我准备的脚本，按提示输入用户名和仓库名即可：

```powershell
powershell -ExecutionPolicy Bypass -File publish.ps1
```

脚本会自动：添加远程地址 → 设置 main 分支 → 推送。

### 关于推送时的登录（认证）
- 如果本机装过 **GitHub Desktop** 或 Windows 版 Git（自带 Credential Manager），推送时会**自动弹出浏览器让你登录 GitHub**，登录即可。
- 否则会要求输入用户名和密码：
  - **用户名**：你的 GitHub 用户名
  - **密码**：不是账号密码，要去生成一个 **Personal Access Token (PAT)**
    - 路径：GitHub 右上头像 → **Settings** → 最左下 **Developer settings** → **Personal access tokens** → **Tokens (classic)** → **Generate new token (classic)**
    - 勾选 **repo**（整项） → 拉到底点 **Generate token**
    - **复制保存**那串 `ghp_xxx`（只显示一次），粘贴当作"密码"输入
- 推送成功后，仓库页面会看到 `app/`、`build.gradle`、`.github/` 等文件。

## 第 5 步：云端编译 APK
1. 进入你的仓库页 → 点顶部 **Actions** 标签
2. 左侧列表选 **Build Signed APK**
3. 右侧点 **Run workflow** → 分支选 **main** → 点绿色 **Run workflow**
4. 等待约 **3–5 分钟**，进度条变绿（✓）即成功
5. 点进该次运行 → 页面下方 **Artifacts** 区域 → 下载 `app-release-apk`
6. 解压得到 **`app-release.apk`**

## 第 6 步：手机安装
1. 把 `app-release.apk` 传到安卓手机
2. 手机允许"未知来源"安装（设置里开一下对应权限）
3. 点开安装 → 桌面出现**独立图标「热管理学习」**
4. 打开即是全屏原生 App：首次联网拉取飞书数据并缓存，断网也能看；改任务状态会写回飞书，跨设备同步

---

## 注意事项
- **签名**：CI 每次构建会重新生成签名密钥，所以不同次下载的 APK 签名不同。如需覆盖安装旧版，请**先卸载旧版**再装新版。若要保留同一签名做增量升级，可把 keystore 存为仓库 Secret（见 README）。
- **数据归属**：App 连的是你那一份飞书多维表格，凭证只在 SCF 后端，不在 App 里，分享给别人不会泄露密钥，但对方看到的是你的学习数据。
- **后端不变**：SCF 后端无需改动或重新上传，`server.py` 已带 CORS 头，原生 App 跨域请求正常。
