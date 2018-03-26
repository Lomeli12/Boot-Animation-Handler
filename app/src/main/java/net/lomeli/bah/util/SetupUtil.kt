package net.lomeli.bah.util

import android.content.Context
import android.os.Environment
import java.io.File

val BASE_FOLDER = File("${Environment.getExternalStorageDirectory().path}/boot_animation_handler")
val REPO_FOLDER = File(BASE_FOLDER, "repos")
val LOCAL_REPO = File(REPO_FOLDER, "local")
val BACKUP_FOLDER = File(BASE_FOLDER, "backups")

fun setupFolders(context: Context) {
    if (!BASE_FOLDER.exists() || !BASE_FOLDER.isDirectory) {
        mkdir(BASE_FOLDER, context)
        mkdir(REPO_FOLDER, context)
        mkdir(LOCAL_REPO, context)
        mkdir(BACKUP_FOLDER, context)
    }

    if (!REPO_FOLDER.exists() || !REPO_FOLDER.isDirectory) {
        mkdir(REPO_FOLDER, context)
        mkdir(LOCAL_REPO, context)
    }

    if (!LOCAL_REPO.exists() || !LOCAL_REPO.isDirectory)
        mkdir(LOCAL_REPO, context)

    if (!BACKUP_FOLDER.exists() || !BACKUP_FOLDER.isDirectory)
        mkdir(BACKUP_FOLDER, context)
}