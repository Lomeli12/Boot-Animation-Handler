package net.lomeli.bah.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.provider.DocumentFile
import java.io.File
import android.annotation.TargetApi
import android.preference.PreferenceManager
import java.io.IOException
import java.util.*

const val BOOT_ANIMATION_LOCATION = "/system/media/bootanimation.zip"

fun openFileIntent(title: String, type: String): Intent {
    val intent = Intent().setType(type).setAction(Intent.ACTION_GET_CONTENT)
    return Intent.createChooser(intent, title)
}

fun openFileIntent(title: String): Intent = openFileIntent(title, "*/*")

fun getContentPath(context: Context, uri: Uri): String? {
    if (DocumentsContract.isDocumentUri(context, uri)) {
        if (isExternalStorageDocument(uri)) {
            val docID = DocumentsContract.getDocumentId(uri)
            val split = docID.split(":")
            val type = split[0]

            if ("primary".equals(type, true)) return "${Environment.getExternalStorageDirectory()}/${split[1]}"
        } else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val docID = DocumentsContract.getDocumentId(uri)
            val split = docID.split(":")
            val type = split[0]

            var contentUri: Uri? = null
            when (type) {
                "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])

            if (contentUri != null)
                return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } else if ("content".equals(uri.scheme, true))
        return getDataColumn(context, uri, null, null)
    else if ("file".equals(uri.scheme, true))
        return uri.path
    return null
}

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context The context.
 * @param uri The Uri to query.
 * @param selection (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(column_index)
        }
    } finally {
        if (cursor != null)
            cursor.close()
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun isExternalStorageDocument(uri: Uri): Boolean = "com.android.externalstorage.documents" == uri.authority

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun isDownloadsDocument(uri: Uri): Boolean = "com.android.providers.downloads.documents" == uri.authority

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
fun isMediaDocument(uri: Uri): Boolean = "com.android.providers.media.documents" == uri.authority

fun getDocumentFile(file: File, isDirectory: Boolean, context: Context): DocumentFile? {
    val baseFolder = getExtSdCardFolder(file, context)
    var originalDirectory = false
    if (baseFolder == null) return null

    var relativePath: String? = null
    try {
        val fullPath = file.canonicalPath
        if (!baseFolder.equals(fullPath)) relativePath = fullPath.substring(baseFolder.length + 1)
        else originalDirectory = true
    } catch (e: IOException) {
        return null
    } catch (ex: Exception) {
        originalDirectory = true
    }
    val as_ = PreferenceManager.getDefaultSharedPreferences(context).getString("URI", null)
    var treeUri: Uri? = null
    if (as_ != null) treeUri = Uri.parse(as_)
    if (treeUri == null) return null

    var document = DocumentFile.fromTreeUri(context, treeUri)
    if (originalDirectory) return document
    val parts = relativePath!!.split("\\/")
    for (i in 0 until parts.size) {
        var nextDocument = document.findFile(parts[i])

        if (nextDocument == null) {
            if (i < parts.size - 1 || isDirectory) nextDocument = document.createDirectory(parts[i])
            else nextDocument = document.createFile("image", parts[i])
        }
        document = nextDocument
    }
    return null
}

fun mkdir(file: File?, context: Context): Boolean {
    if (file == null) return false
    if (file.exists()) return file.isDirectory
    if (file.mkdirs()) return true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOnExtSdCard(file, context)) {
        var document = getDocumentFile(file, true, context)
        return document!!.exists()
    }
    return false
}

/**
 * Get a list of external SD card paths. (Kitkat or higher.)
 *
 * @return A list of external SD card paths.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
private fun getExtSdCardPaths(context: Context): Array<String> {
    val paths = ArrayList<String>()
    for (file in context.getExternalFilesDirs("external")) {
        if (file != null && file != context.getExternalFilesDir("external")) {
            val index = file.absolutePath.lastIndexOf("/Android/data")
            if (index < 0) {

                //Log.w(LOG, "Unexpected external file dir: " + file.absolutePath)
            } else {
                var path = file.absolutePath.substring(0, index)
                try {
                    path = File(path).canonicalPath
                } catch (e: IOException) {
                    // Keep non-canonical path.
                }

                paths.add(path)
            }
        }
    }
    if (paths.isEmpty()) paths.add("/storage/sdcard1")
    return paths.toTypedArray()
}

/**
 * Determine the main folder of the external SD card containing the given file.
 *
 * @param file the file.
 * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
 * null is returned.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
fun getExtSdCardFolder(file: File, context: Context): String? {
    val extSdPaths = getExtSdCardPaths(context)
    try {
        for (i in extSdPaths.indices) {
            if (file.canonicalPath.startsWith(extSdPaths[i])) {
                return extSdPaths[i]
            }
        }
    } catch (e: IOException) {
        return null
    }

    return null
}

/**
 * Determine if a file is on external sd card. (Kitkat or higher.)
 *
 * @param file The file.
 * @return true if on external sd card.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
fun isOnExtSdCard(file: File, c: Context): Boolean {
    return getExtSdCardFolder(file, c) != null
}