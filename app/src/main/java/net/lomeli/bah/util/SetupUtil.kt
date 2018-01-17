package net.lomeli.bah.util

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val BASE_FOLDER = File("${Environment.getExternalStorageDirectory().path}/boot_animation_handler")
val REPO_FOLDERS = File(BASE_FOLDER, "repos")
val BACKUP_FOLDER = File(BASE_FOLDER, "backups")

fun setupFolders() {
    if (!BASE_FOLDER.exists() || !BASE_FOLDER.isDirectory) {
        BASE_FOLDER.mkdir()
        REPO_FOLDERS.mkdir()
        BACKUP_FOLDER.mkdir()
    }

    if (!REPO_FOLDERS.exists() || !REPO_FOLDERS.isDirectory)
        REPO_FOLDERS.mkdir()

    if (!BACKUP_FOLDER.exists() || !BACKUP_FOLDER.isDirectory)
        BACKUP_FOLDER.mkdir()
}