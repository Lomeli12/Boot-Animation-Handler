package net.lomeli.bah

import com.topjohnwu.superuser.BuildConfig
import com.topjohnwu.superuser.BusyBox
import com.topjohnwu.superuser.Shell

class BAHApp: Shell.ContainerApp() {

    override fun onCreate() {
        super.onCreate()
        Shell.setFlags(Shell.FLAG_MOUNT_MASTER)
        Shell.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.verboseLogging(BuildConfig.DEBUG)
        BusyBox.setup(this)
    }
}