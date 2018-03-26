package net.lomeli.bah.util

import android.content.res.Resources
import android.view.View
import com.topjohnwu.superuser.Shell
import net.lomeli.bah.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.text.SimpleDateFormat
import java.util.*

fun getBackupName(): String = "bootanimation_${SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Date())}.zip.bak"

fun backupAnimation(baseView: View, resources: Resources, logger: AnkoLogger) {
    if (!Shell.rootAccess()) {
        longSnackbar(baseView, resources.getString(R.string.snack_require_root))
        return
    }
    val backup = getBackupName()
    logger.info("${BACKUP_FOLDER.absolutePath}/$backup")
    val output = ArrayList<String>()
    val error = ArrayList<String>()
    Shell.Sync.su(output, error, copyCommand(BOOT_ANIMATION_LOCATION, "${BACKUP_FOLDER.absolutePath}/$backup"))
    logger.info(output)
    logger.error(error)
    if (error.isEmpty()) snackbar(baseView, resources.getString(R.string.snack_backup, backup))
    else snackbar(baseView, resources.getString(R.string.snack_failed_backup))
}

fun setBootAnimation(animationPath: String?, baseView: View, resources: Resources, logger: AnkoLogger) {
    if (animationPath == null || animationPath.isBlank()) return
    if (Shell.rootAccess()) {
        val output = ArrayList<String>()
        val error = ArrayList<String>()
        Shell.Sync.su(output, error, readWriteSystem(),
                "chmod 777 $BOOT_ANIMATION_LOCATION",
                "rm -rf $BOOT_ANIMATION_LOCATION",
                copyCommand(animationPath, BOOT_ANIMATION_LOCATION),
                "chmod 644 $BOOT_ANIMATION_LOCATION",
                readOnlySystem())
        logger.info(output)
        logger.error(error)
        if (error.isEmpty())
            longSnackbar(baseView, resources.getString(R.string.snack_replaced_animation))
        else
            longSnackbar(baseView, resources.getString(R.string.snack_failed_update_animation))
    } else
        longSnackbar(baseView, resources.getString(R.string.snack_require_root))
}