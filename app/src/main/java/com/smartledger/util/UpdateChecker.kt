package com.smartledger.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** 仅从用户自有 GitHub 仓库的 latest release 检查和下载更新。 */
object UpdateChecker {
    private const val API_URL =
        "https://api.github.com/repos/zixiwang0o0/Bsoul/releases/latest"

    data class UpdateInfo(
        val versionName: String,
        val releaseNotes: String,
        val apkUrl: String
    )

    sealed class CheckResult {
        data class HasUpdate(val info: UpdateInfo) : CheckResult()
        data class UpToDate(val version: String) : CheckResult()
        data class Failed(val message: String) : CheckResult()
    }

    fun currentVersion(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0)
            .versionName.orEmpty().removePrefix("v")

    suspend fun check(context: Context): CheckResult = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Bsoul-Android")
            }
            if (connection.responseCode != 200) {
                val code = connection.responseCode
                connection.disconnect()
                return@withContext CheckResult.Failed("更新服务返回 $code")
            }
            val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            connection.disconnect()
            val remote = json.getString("tag_name").removePrefix("v")
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (index in 0 until assets.length()) {
                    val asset = assets.getJSONObject(index)
                    if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }
            }
            if (isNewer(currentVersion(context), remote)) {
                if (apkUrl == null) CheckResult.Failed("Release 中没有 APK 附件")
                else CheckResult.HasUpdate(
                    UpdateInfo(remote, json.optString("body").take(600), apkUrl)
                )
            } else CheckResult.UpToDate(remote)
        } catch (error: Exception) {
            CheckResult.Failed(error.message ?: "网络错误")
        }
    }

    suspend fun download(
        context: Context,
        info: UpdateInfo,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.cacheDir, "apk").apply { mkdirs() }
            val output = File(directory, "Bsoul-${info.versionName}.apk")
            val connection = (URL(info.apkUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 15_000
                readTimeout = 60_000
                setRequestProperty("User-Agent", "Bsoul-Android")
            }
            val total = connection.contentLengthLong
            connection.inputStream.use { input ->
                output.outputStream().use { target ->
                    val buffer = ByteArray(16 * 1024)
                    var downloaded = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        target.write(buffer, 0, count)
                        downloaded += count
                        if (total > 0) onProgress(((downloaded * 100) / total).toInt())
                    }
                }
            }
            connection.disconnect()
            require(output.length() > 0) { "下载文件为空" }
            output
        }
    }

    fun install(context: Context, apk: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
            })
            return false
        }
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apk
        )
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
        return true
    }

    private fun isNewer(local: String, remote: String): Boolean {
        val left = local.split('.').map { it.toIntOrNull() ?: 0 }
        val right = remote.split('.').map { it.toIntOrNull() ?: 0 }
        repeat(maxOf(left.size, right.size)) { index ->
            val comparison = right.getOrElse(index) { 0 }
                .compareTo(left.getOrElse(index) { 0 })
            if (comparison != 0) return comparison > 0
        }
        return false
    }
}
