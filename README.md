# 屏幕答题助手 (ScreenAssistant)

一个能实时识别手机屏幕上的题目并给出答案的Android应用。

## 功能特点

- ✨ **悬浮窗截图** - 可拖动的悬浮按钮，随时截取屏幕
- 🔍 **OCR文字识别** - 使用Google ML Kit识别屏幕上的文字
- 🤖 **AI智能答题** - 集成OpenAI API，自动分析题目并给出答案
- 📱 **结果显示** - 在悬浮窗中直接显示识别的题目和答案

## 编译说明

### 方法一：使用Android Studio（推荐）

1. 下载并安装 [Android Studio](https://developer.android.com/studio)
2. 克隆或下载本项目
3. 用Android Studio打开项目目录
4. 等待Gradle同步完成
5. 点击 `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`
6. 编译完成后，APK位于：`app/build/outputs/apk/release/app-release-unsigned.apk`

### 方法二：使用命令行

```bash
# 确保已安装JDK 17
# Windows
set JAVA_HOME=C:\path\to\jdk-17
gradlew.bat assembleRelease

# Linux/Mac
export JAVA_HOME=/path/to/jdk-17
./gradlew assembleRelease
```

编译完成后，APK位于：`app/build/outputs/apk/release/app-release-unsigned.apk`

## 配置说明

在使用前，需要配置AI API密钥：

1. 打开 `app/src/main/java/com/screenassistant/AIHelper.java`
2. 找到第13行的 `API_KEY` 常量
3. 将 `YOUR_API_KEY_HERE` 替换为您的OpenAI API密钥

```java
private static final String API_KEY = "sk-your-actual-api-key-here";
```

## 使用方法

1. 安装APK到Android设备（需要Android 7.0及以上）
2. 打开应用，授予悬浮窗权限
3. 点击"启动服务"按钮
4. 屏幕上会出现蓝色圆形悬浮按钮
5. 点击悬浮按钮即可截取当前屏幕并识别题目
6. 识别结果会显示在新的悬浮窗中

## 技术栈

- **开发语言**: Java
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 13 (API 33)
- **主要依赖**:
  - Google ML Kit Text Recognition
  - OkHttp (网络请求)
  - Gson (JSON解析)
  - Material Design Components

## 项目结构

```
ScreenAssistant/
├── app/
│   ├── src/main/
│   │   ├── java/com/screenassistant/
│   │   │   ├── MainActivity.java          # 主界面
│   │   │   ├── FloatingWindowService.java # 悬浮窗服务
│   │   │   ├── ScreenCaptureHelper.java   # 截图辅助类
│   │   │   ├── OCRHelper.java             # OCR识别辅助类
│   │   │   └── AIHelper.java              # AI答题辅助类
│   │   ├── res/                           # 资源文件
│   │   └── AndroidManifest.xml            # 应用配置
│   └── build.gradle                       # 应用构建配置
├── build.gradle                           # 项目构建配置
├── settings.gradle                        # Gradle设置
└── README.md                              # 本文件
```

## 权限说明

应用需要以下权限：
- `INTERNET` - 用于调用AI API
- `SYSTEM_ALERT_WINDOW` - 用于显示悬浮窗
- `FOREGROUND_SERVICE` - 用于前台服务
- `FOREGROUND_SERVICE_MEDIA_PROJECTION` - 用于屏幕截图
- `POST_NOTIFICATIONS` - 用于显示通知

## 注意事项

1. 首次编译需要下载依赖，请确保网络连接正常
2. 需要有效的OpenAI API密钥才能使用AI答题功能
3. 截图功能在某些设备上可能需要额外权限
4. 建议在Android 7.0及以上版本使用

## 开发计划

- [ ] 支持更多AI服务提供商
- [ ] 添加历史记录功能
- [ ] 支持多语言识别
- [ ] 优化截图性能
- [ ] 添加题目分类功能

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，欢迎提Issue。
