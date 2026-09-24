package com.example.sakuku.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object RootChecker {

    private val ROOT_BINARY_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/system/xbin/daemonsu"
    )

    val ROOT_PACKAGES = arrayOf(
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.topjohnwu.magisk",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.smedialink.oneclickroot",
        "com.zhiqupk.root.global",
        "com.alephzain.framaroot",
        "me.weishu.kernelsu"
    )

    fun isDeviceRooted(context: Context): Boolean =
        hasTestKeysBuildTag() || hasRootBinary() || hasRootPackage(context) || canExecuteSu()

    private fun hasTestKeysBuildTag(): Boolean {
        val tags = Build.TAGS
        return tags != null && tags.contains("test-keys")
    }

    private fun hasRootBinary(): Boolean =
        ROOT_BINARY_PATHS.any { path ->
            try {
                File(path).exists()
            } catch (e: SecurityException) {
                false
            }
        }

    private fun hasRootPackage(context: Context): Boolean {
        val pm = context.packageManager
        return ROOT_PACKAGES.any { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun canExecuteSu(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val output = process.inputStream.bufferedReader().readLine()
            !output.isNullOrBlank()
        } catch (e: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }
}
