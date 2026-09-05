## Fork and pull to local

## 确认项目能够正常编译

### 什么是编译？

编译就是把源码变成电脑/手机能运行的程序。

### How to assemble?

```powershell
.\gradlew.bat assembleDebug
```
a lot of problems here. to solve these problem, the one work with this problem is what?

#### Android Studio

1. What is Android Studio?

Android Studio 是谷歌官方的 Android 开发工具，可安装 SDK、编辑代码、编译 APK，并在模拟器或手机上测试应用。

2. What is SDK?

SDK 是“软件开发工具包”，包含编译器、Android 系统接口和调试工具，**用来把源码构建成 APK**。

## 检查并删除联网权限

Find the code for internet:

```powershell
Get-ChildItem app\src -Recurse -File |
Select-String "INTERNET|ACCESS_NETWORK_STATE"
```

delete the previous `apk` and generate a new one.

```powershell
bash ./gradlew clean assembleDebug
```

verify that the `apk` is offline:

```powershell
bash -c "cp /mnt/d/Android/Sdk/build-tools/34.0.0/aapt /tmp/aapt && chmod +x /tmp/aapt"
bash -c "LD_LIBRARY_PATH=/mnt/d/Android/Sdk/build-tools/34.0.0/lib64 /tmp/aapt dump permissions /mnt/d/desktop/Bsoul/app/build/outputs/apk/debug/app-debug.apk"
```

输出中没有 `android.permission.INTERNET` 即通过。

## 定位并删除自动更新/安装 APK 的能力

```powershell
Get-ChildItem app\src -Recurse -File |
Select-String "REQUEST_INSTALL_PACKAGES"
```

生成已移除安装权限的新 APK and verify:

```powershell
bash ./gradlew assembleDebug
bash -c "LD_LIBRARY_PATH=/mnt/d/Android/Sdk/build-tools/34.0.0/lib64 /tmp/aapt dump permissions /mnt/d/desktop/Bsoul/app/build/outputs/apk/debug/app-debug.apk"
```

## 什么是验证签名是否有效？

“验证签名有效”就是确认 APK 的数字签名没有损坏、证书可识别，且安装包未被篡改。

# Bug?

## 陪伴日期显示统计

在「我的」页面，智能记账已陪伴您 x 天，显示的天数不正确。今天是第一天，但是显示的是第 4 天。查看此处的计数方式并尝试修复。done

## 版本号显示更新

下载新版本后，版本号没有同步更新。done

## 账单编辑与创建界面不同

我在编辑已经创建的账单时，发现编辑页面和创建界面不同。然后主要是我发现在编辑页面，分类section 和创建的时候的分类section 不同，比如现在就缺少了日用这一类。我有两个修改方案

1. 在编辑页面加上日用这一项
2. 编辑账单的时候直接使用创建账单时候的页面

