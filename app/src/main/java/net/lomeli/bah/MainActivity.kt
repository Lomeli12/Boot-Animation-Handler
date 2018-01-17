package net.lomeli.bah

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import net.lomeli.bah.util.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.info

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AnkoLogger {
    private val OPEN_ZIP_ID = 130

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        HAS_ROOT_ACCESS = getRoot()
        setupFolders()
        fab.setOnClickListener { view ->
            if (checkForRootAccess(view)) {
                startActivityForResult(openFileIntent(resources.getString(R.string.dialog_select_zip),
                        "application/zip"), OPEN_ZIP_ID)
            }
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //TODO Stuff
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_backup -> {
                val backup = getBackupName()
                backupCurrentAnimation(backup)
                snackbar(window.decorView, resources.getString(R.string.snack_backup, backup))
            }
            R.id.nav_import_zip -> {
                if (checkForRootAccess(window.decorView))
                    startActivityForResult(openFileIntent(resources.getString(R.string.dialog_select_zip),
                            "application/zip"), OPEN_ZIP_ID)
            }

            R.id.nav_github -> {
                browse("https://github.com/Lomeli12/Boot-Animation-Handler")
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            OPEN_ZIP_ID -> {
                if (resultCode == Activity.RESULT_OK) {
                    val filePath = getContentPath(this, data!!.data)
                    info("Zip File: $filePath")
                    if (filePath != null && filePath.isNotBlank()) {
                        val baseView = window.decorView
                        if (checkForRootAccess(baseView)) {
                            replaceAnimation(filePath)
                            longSnackbar(baseView, resources.getString(R.string.snack_replaced_animation))
                        }
                    }
                }
            }
        }
    }
}