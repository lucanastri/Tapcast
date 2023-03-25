package com.kizune.tapcast

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.kizune.tapcast.ui.LoginFragmentDirections

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Log.e("MyTag", "Exception unhandled", e)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        auth = Firebase.auth
        storage = Firebase.storage

        val currentUser = auth.currentUser
        if (currentUser != null && savedInstanceState == null) {
            if (navController.currentDestination?.id == R.id.loginFragment) {
                val action = LoginFragmentDirections.actionLoginFragmentToDashboardFragment()
                navController.navigate(action)
            }
        }

    }
}