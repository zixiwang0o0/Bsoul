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

## 账单编辑与创建界面不同

我在编辑已经创建的账单时，发现编辑页面和创建界面不同。然后主要是我发现在编辑页面，分类section 和创建的时候的分类section 不同，比如现在就缺少了日用这一类。我有两个修改方案

1. 在编辑页面加上日用这一项
2. 编辑账单的时候直接使用创建账单时候的页面

I prefer option 2. 对于这次调整，你先分析一下这两个方案，告诉我优劣，然后我再决定怎么修改。

## 版本号显示

版本号在此次更新了，但是是从之前的1.0.28更新到了后一位1.0.29，而没有显示当前apk对应的1.0.31. (1.0.32)

## 统计界面

统计界面目前只有支出，我希望增加收入的统计。可以考虑在「统计」界面，通过在日，周，月，年栏目下方增加一个支出和收入的按钮来切换。

## 退款

因为退款部分现在仍然被放置在收入部分中，但是我希望把各种退款和真正的收入做一个区分，然后最后统计的时候，统计真正的收入。而对于退款，最好是可以找到这笔退款对应的支付记录，然后直接相互抵消掉（可以保留这两条记录，但是统计的时候不考虑）。以及，如果这其中产生了手续费，只把手续费计入统计就好了。不过具体实现方案我现在考虑得还不是很清楚。