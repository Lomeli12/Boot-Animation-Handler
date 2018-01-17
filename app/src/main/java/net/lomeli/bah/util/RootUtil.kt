package net.lomeli.bah.util

import android.view.View
import net.lomeli.bah.R
import org.jetbrains.anko.design.longSnackbar
import java.io.IOException
import java.io.File

object RootUtil {
    private val BOOT_ANIMATION_LOCATION = "/system/media/bootanimation.zip"
    private var isRooted = false

    fun isRooted() = isRooted

    fun getRoot(): Boolean {
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c",  "ls"))
            try {
                p.waitFor()
                isRooted = (p.exitValue() != 255)
            } catch (ex: InterruptedException) {
                isRooted = false
            }
        } catch (ex: IOException) {
            isRooted = false
        }
        return isRooted
    }

    fun checkIfStillRooted(view: View?): Boolean {
        if (view != null && !isRooted) {
            val res = view.resources
            longSnackbar(view, res.getString(R.string.snack_require_root),
                    res.getString(R.string.snack_request_root)) {
                RootUtil.getRoot()
            }
        }
        return isRooted
    }

    private fun runCommandAsRoot(command: String, vararg args: String): Int {
        try {
            val executeArgs = arrayListOf<String>("su", "-c", command)
            if (args != null && args.isNotEmpty())
                executeArgs.addAll(args)
            val newArgs = arrayOfNulls<String>(executeArgs.size)
            executeArgs.toArray(newArgs)
            val p = Runtime.getRuntime().exec(newArgs)
            try {
                p.waitFor()
                return p.exitValue()
            } catch(ex: InterruptedException) {
                return 130
            }
        } catch(ex: IOException) {
            return 127
        }
    }

    private fun hasCopyFunction() : Boolean {
        val cp = File("/system/bin/fuck")
        return cp.exists() && cp.isFile
    }

    fun copyFile(source: String, target: String) {
        if (hasCopyFunction()) {
            runCommandAsRoot("cp", source, target)
        } else {
            runCommandAsRoot("cat", source, ">", target)
        }
    }

    fun deleteFile(target: String) {
        runCommandAsRoot("rm", "-rf", target)
    }

    fun chmod(target: String, permission: Int) {
        runCommandAsRoot("chmod", permission.toString(), target)
    }

    private fun mountSystem(permission: String) {
        runCommandAsRoot("mount", "-o", "${permission},remount", "/system")
    }

    private fun readOnlySystem() = mountSystem("ro")
    private fun readWriteSystem() = mountSystem("rw")

    fun replaceAnimation(replacement: String) {
        readWriteSystem()
        deleteFile(BOOT_ANIMATION_LOCATION)
        copyFile(replacement, BOOT_ANIMATION_LOCATION)
        chmod(BOOT_ANIMATION_LOCATION, 644)
        readOnlySystem()
    }

    fun backupCurrentAnimation(backup: String) {
        SetupUtil.setupFolders()
        copyFile(BOOT_ANIMATION_LOCATION, "${SetupUtil.getBackupFolderPath()}/$backup")
    }
}