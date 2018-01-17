package net.lomeli.bah.util

import android.view.View
import net.lomeli.bah.R
import org.jetbrains.anko.design.longSnackbar
import java.io.IOException
import java.io.File

val BINARY_PATHS = arrayOf("/data/local/", "/data/local/bin/", "/data/local/xbin/", "/sbin/",
        "/su/bin/", "/system/bin/", "/system/bin/.ext/", "/system/bin/failsafe/", "/system/sd/xbin/",
        "/system/usr/we-need-root/", "/system/xbin/")
var HAS_ROOT_ACCESS = false

/**
 * @param binary - Check for the existance of this file.
 * @return true if found.
 */
fun checkForBinary(binary: String): Boolean = BINARY_PATHS.map { it + binary }.map { File(it) }
        .any { it.exists() && it.isFile }

/**
 * Check common locations for the Su binary (essentially checking if the device is rooted).
 * @return true if found.
 */
fun checkForSuBinary(): Boolean = checkForBinary("su")
/**
 * Check common locations for the busybox binary (a well known root level program).
 * @return true if found.
 */
fun checkForBusyBoxBinary(): Boolean = checkForBinary("busybox")

/**
 * Check common locations for the cp binary (usually installed on newer devices, but may be missing
 * on older ones).
 * @return true if found.
 */
fun checkForCpBinary(): Boolean = checkForBinary("cp")

/**
 * Request root access for this app.
 * @return true if root access is granted.
 */
fun getRoot(): Boolean {
    var isRooted: Boolean?
    try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls"))
        try {
            p.waitFor()
            isRooted = (p.exitValue() != 255)
        } catch (ex: InterruptedException) {
            isRooted = false
        }
    } catch (ex: IOException) {
        isRooted = false
    }
    return isRooted!!
}

fun checkForRootAccess(view: View?): Boolean {
    var result = false
    if (view != null && checkForSuBinary()) {
        if (!HAS_ROOT_ACCESS) {
            val res = view.resources
            longSnackbar(view, res.getString(R.string.snack_require_root),
                    res.getString(R.string.snack_request_root)) {
                result = getRoot()
                HAS_ROOT_ACCESS = result
            }
        } else return true
    }
    return result
}

/**
 * Run command with superuser permissions.
 * @return exit code for command.
 */
fun runCommandAsRoot(command: String, vararg args: String): Int {
    try {
        val executeArgs = arrayListOf<String>("su", "-c", command)
        if (args.isNotEmpty())
            executeArgs.addAll(args)
        val newArgs = arrayOfNulls<String>(executeArgs.size)
        executeArgs.toArray(newArgs)
        val p = Runtime.getRuntime().exec(newArgs)
        try {
            p.waitFor()
            return p.exitValue()
        } catch (ex: InterruptedException) {
            return 130
        }
    } catch (ex: IOException) {
        return 127
    }
}

/**
 * Attempts to copy a target using cp, busybox, or cat if all else fails with superuser permissions.
 */
fun copyFile(target: String, destination: String) {
    if (checkForCpBinary()) runCommandAsRoot("cp", target, destination)
    else if (checkForBusyBoxBinary()) runCommandAsRoot("busybox", "cp", target, destination)
    else runCommandAsRoot("cat", target, ">", destination)
}

/**
 * Deletss target using superuser permissions.
 */
fun deleteFile(target: String) = runCommandAsRoot("rm", "-rf", target)

/**
 * Changes the target's permissions using superuser permissions.
 */
fun chmod(target: String, permission: Int) = runCommandAsRoot("chmod", permission.toString(), target)

/**
 * Remounts the System partition using superuser permissions.
 */
fun mountSystem(permission: String) = runCommandAsRoot("mount", "-o", "$permission,remount", "/system")

/**
 * Remounts System partition as read-only.
 */
fun readOnlySystem() = mountSystem("ro")

/**
 * Remounts System partition as read-write.
 */
fun readWriteSystem() = mountSystem("rw")