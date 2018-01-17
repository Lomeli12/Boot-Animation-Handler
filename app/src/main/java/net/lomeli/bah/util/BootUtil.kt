package net.lomeli.bah.util

import java.text.SimpleDateFormat
import java.util.*

val BOOT_ANIMATION_LOCATION = "/system/media/bootanimation.zip"

fun replaceAnimation(replacement: String) {
    readWriteSystem()
    deleteFile(BOOT_ANIMATION_LOCATION)
    copyFile(replacement, BOOT_ANIMATION_LOCATION)
    chmod(BOOT_ANIMATION_LOCATION, 644)
    readOnlySystem()
}

fun getBackupName(): String = "bootanimation_${SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Date())}.zip.bak"

fun backupCurrentAnimation(backup: String) {
    setupFolders()
    copyFile(BOOT_ANIMATION_LOCATION, "${BACKUP_FOLDER.absolutePath}/$backup")
}