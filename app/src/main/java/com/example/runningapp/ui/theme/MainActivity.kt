package com.example.runningapp.ui.theme


// Note that in the manifest file we made the launch mode to be single activity because we do not want any kind of interference
// This will prevent data loss
// We also need to include some meta data for the google maps api

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
//import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.ActivityMainBinding
import com.example.runningapp.db.RunDAO
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT

/*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.runningapp.ui.theme.RunningAppTheme

 */



@AndroidEntryPoint // This must be put here to allow dagger into an activity
class MainActivity : AppCompatActivity() {

    // @Inject // This is used to inject all of the data base object in this class with dependencies also
    // lateinit var runDAO: RunDAO

    private lateinit var binding : ActivityMainBinding


    // @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        navigateToTrackingFragmentIfNeeded(intent) // This will be needed if the main activity has probably not been opened before that is initially, this will allow us to navigate to the tracking fragment even if our app has been closed

        // To then test if the inject was successful and our runDAO is actually an object as expected
        //Log.d("runDao", "RUNDA0: ${runDAO.hashCode()}")
        //getSupportActionBar().hide()

        //setSupportActionBar(binding.toolbar) // This is to tell android that our main action bar is this tool bar

        //binding.bottomNavigationView.setupWithNavController(binding.navHostFragment.findNavController()) // This is used to set and change the fragments with changes in the bottom navigation
        //val navController = this.findNavController(R.id.navHostFragment)
        // binding.bottomNavigationView.setupWithNavController(navController)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnNavigationItemReselectedListener{/* NO-OP */} // This will not allow the fragment to reload again once we click on it3


        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) { // This is like a switch statements for the fragments
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        binding.bottomNavigationView.visibility =
                            View.VISIBLE // This means that when we are in the settings, run or
                    else -> binding.bottomNavigationView.visibility = View.GONE

                }

            }




    }

    // In case we have a new intent just like our "pending intent" the we need to do that again
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }
}

