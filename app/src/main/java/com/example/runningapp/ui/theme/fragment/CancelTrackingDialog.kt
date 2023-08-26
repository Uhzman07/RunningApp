package com.example.runningapp.ui.theme.fragment

import android.app.Dialog
//import android.app.DialogFragment
import androidx.fragment.app.DialogFragment // Note that we must import DialogFragment compulsorily
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.content.ContentProviderCompat.requireContext

import com.example.runningapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Then to treat our dialog as a Fragment

@Suppress("DEPRECATION")
class CancelTrackingDialog : DialogFragment() {

    private var yesListener : (() -> Unit)? = null // This is just like an example of the cation that we want to perform

    fun setYesListener(listener: () -> Unit){  // Note that listener here is like the action that we want to pass into our alert dialogue
        yesListener = listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the run and delete all its data")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _,_ ->
                yesListener?.let {
                    yes -> yes()
                }


            }
            .setNegativeButton("No"){ dialogInterface,_ ->
                dialogInterface.cancel()

            }
            .create()


    }
}