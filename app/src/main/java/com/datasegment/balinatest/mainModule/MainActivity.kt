package com.datasegment.balinatest.mainModule

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.datasegment.balinatest.R
import com.datasegment.balinatest.mainModule.imageController.view.MapFragment
import com.datasegment.balinatest.mainModule.imageController.view.PhotoFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var  actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var photoFragment: PhotoFragment
    private lateinit var mapFragment: MapFragment
    private var userName: String? = null
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userToken = intent.getStringExtra("USER_TOKEN")
        userName = intent.getStringExtra("USER_NAME")
        photoFragment = PhotoFragment()
        mapFragment = MapFragment()
        val bundle = Bundle()
        bundle.putString("USER_TOKEN", userToken)
        photoFragment.arguments = bundle
        mapFragment.arguments = bundle
        drawerLayout = findViewById(R.id.myDrawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, photoFragment).commit()
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val headerView: View = navigationView.getHeaderView(0)
        val headerText: TextView = headerView.findViewById(R.id.textView_header)
        headerText.text = userName
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.photos_nav_menu -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, photoFragment).commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.map_nav_menu -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, mapFragment).commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                supportActionBar?.hide()
            }

            override fun onDrawerClosed(drawerView: View) {
                supportActionBar?.show()
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    override fun onBackPressed() {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            true
        }else{
            super.onOptionsItemSelected(item)
        }

    }
}