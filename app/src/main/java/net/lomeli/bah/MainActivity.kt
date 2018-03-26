package net.lomeli.bah

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import net.lomeli.bah.util.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AnkoLogger {
    private val OPEN_ZIP_ID = 130
    private val APP_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        askForPermissions()

        setupFolders(applicationContext)

        if (!Shell.rootAccess())
            longSnackbar(window.decorView, resources.getString(R.string.snack_require_root))
    }

    private fun askForPermissions() {
        var id = 255
        for (permission in APP_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), id)
                id += 1
            }
        }

    }

    private fun setupUI() {
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)

        fab.setOnClickListener { backupAndImport(this) }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_backup -> backupAnimation(window.decorView, resources, this)
            R.id.nav_import_zip -> backupAndImport( this)
            R.id.nav_github -> browse("https://github.com/Lomeli12/Boot-Animation-Handler")
            R.id.nav_donate -> browse("https://ko-fi.com/lomeli")
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPEN_ZIP_ID -> {
                if (resultCode == Activity.RESULT_OK) {
                    val filePath = getContentPath(this, data!!.data)
                    info("Zip File: $filePath")
                    setBootAnimation(filePath, window.decorView, resources, this)
                }
            }
        }
    }


    private fun backupAndImport(logger: AnkoLogger) {
        alert(resources.getString(R.string.alert_backup_current)) {
            yesButton {
                backupAnimation(window.decorView, resources, logger)
                importZipFile()
            }
            noButton {
                importZipFile()
            }
        }.show()
    }

    private fun importZipFile() {
        if (Shell.rootAccess())
            startActivityForResult(openFileIntent(resources.getString(R.string.dialog_select_zip),
                    "application/zip"), OPEN_ZIP_ID)
        else
            longSnackbar(window.decorView, resources.getString(R.string.snack_require_root))
    }
}