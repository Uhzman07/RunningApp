package com.example.runningapp.ui.theme.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.runningapp.R
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldsFromSharedPref()

        val btnApplyChanges = view.findViewById<Button>(R.id.btnApplyChanges)
        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if(success){
                Snackbar.make(view,"Saved changes",Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(view,"Please fill out all fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }


    // To inject our shared Preferences using the
    @Inject
    lateinit var sharedPreferences: SharedPreferences


    private fun loadFieldsFromSharedPref(){
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT,80f)

        val etName = view?.findViewById<EditText>(R.id.etName)
        val etWeight = view?.findViewById<EditText>(R.id.etWeight)

        etName?.setText(name)
        etWeight?.setText(weight.toString())

    }

    private fun applyChangesToSharedPref() : Boolean{
        val etName = view?.findViewById<EditText>(R.id.etName)
        val etWeight = view?.findViewById<EditText>(R.id.etWeight)
        val nameText = etName?.text.toString()
        val weightText = etWeight?.text.toString()

        if(nameText.isEmpty() || weightText.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_NAME,nameText)
            .putFloat(KEY_WEIGHT,weightText.toFloat())
            .apply()

        val toolbarText = "Let's go $nameText"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText
        return true

    }

}