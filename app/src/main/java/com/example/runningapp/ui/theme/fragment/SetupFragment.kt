package com.example.runningapp.ui.theme.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {


    @Inject // This is to inject the sharedPreference from the app module class
    lateinit var sharedPref : SharedPreferences


    // Since a boolean is a primitive data type, we cannot use the late init var

    @set:Inject // We need to use this since we are setting directly instead of injecting all
    var isFirstAppOpen = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Then for us to remove the setUp Fragment from the back stack once the app has been opened before
        if(!isFirstAppOpen){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()

            // Then to then perform the action of navigating
            // When we open an app and that is not the first time that we are opening it then we navigate from setup fragment to the run fragment and then we remove the setup fragment from the backstack

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
                )
        }

        val tvContinue = view.findViewById<View>(R.id.tvContinue)
        tvContinue.setOnClickListener{
            val success = writePersonalDataToSharedPref()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment) // This is then used to move from one fragment to the other fragment
                // Also note that we have added "R.id.action_setupFragment_to_runFragment" which is an action that can be got from the nav_graph used to transition from one fragment to the other
                // So when we click the continue textview then it can perform the action of changing from one fragment to another
            }else{
                Snackbar.make(requireView(),"Please enter all the fields", Snackbar.LENGTH_SHORT).show()
            }


        }


    }


    private fun writePersonalDataToSharedPref() : Boolean{
        val etName = view?.findViewById<EditText>(R.id.etName)
        val etWeight = view?.findViewById<EditText>(R.id.etWeight)
        val name = etName?.text.toString()
        val weight = etWeight?.text.toString()

        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false) // Note that since we know that since that is not the first time since the user is using the shared Preferences
            .apply() // This is asynchronous
            //.commit() // This is synchronous
        val toolbarText = "Let's go, $name!"

        // Note that requireActivity() is the one that is used represent the fragment activity that is the one that we can use to access the main activity
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText
        return true
    }

}