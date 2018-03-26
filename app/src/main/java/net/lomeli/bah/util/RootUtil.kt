package net.lomeli.bah.util

import java.io.File

val BINARY_PATHS = arrayOf("/data/local/", "/data/local/bin/", "/data/local/xbin/", "/sbin/",
        "/su/bin/", "/system/bin/", "/system/bin/.ext/", "/system/bin/failsafe/", "/system/sd/xbin/",
        "/system/usr/we-need-root/", "/system/xbin/")

/**
 * @param binary - Check for the existance of this file.
 * @return true if found.
 */
fun checkForBinary(binary: String): Boolean = BINARY_PATHS.map { it + binary }.map { File(it) }
        .any { it.exists() && it.isFile }

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
 * Check for a cp or busybox command to copy files. Not really a problem on newer phones, but
 * older phones usually don't have the cp command.
 * @return The best possible command to copy files. Defaults to cat if nothing else is available.
 */
fun copyCommand(source: String, target: String): String {
    if (checkForCpBinary()) return "cp $source $target"
    if (checkForBusyBoxBinary()) return "busybox cp $source $target"
    return "cat $source > $target"
}

/**
 * Remounts the System partition using superuser permissions.
 */
fun mountSystem(permission: String): String = "mount -o remount,$permission /system"

/**
 * Remounts System partition as read-only.
 */
fun readOnlySystem() = mountSystem("ro")

/**
 * Remounts System partition as read-write.
 */
fun readWriteSystem() = mountSystem("rw")