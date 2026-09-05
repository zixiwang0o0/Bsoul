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

# 我觉得可以改进的地方

## 记账颜色

现在收入使用的是绿色，支出使用的是红色，我希望把这两个的颜色对换一下

## 账单生成后编辑日期

目前只有在手动记账创建的时候可以编辑日期，创建之后无法修改。我希望能让账单在被创建之后仍然可以修改日期。