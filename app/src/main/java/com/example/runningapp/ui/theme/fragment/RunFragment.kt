package com.example.runningapp.ui.theme.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningapp.R
import com.example.runningapp.adapters.RunAdapter
import com.example.runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runningapp.other.SortType
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.theme.ViewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

// Note that whenever we are trying to inject something into our android component, we need need to add "@AndroidEntryPoint"
@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks { // Note that we need to add this interface to the class or fragment that is requesting the permission using EasyPermissions
    // To inject view model from dagger

    private val viewModel : MainViewModel by viewModels() // Note that  dagger will automatically check through and then identify the view model for us

    // Then to put in the recycler view
    private lateinit var runAdapter: RunAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        //requestNotificationPermission()

        // Then to set up the recycler view
        val rvRuns = view?.findViewById<RecyclerView>(R.id.rvRuns)
        if (rvRuns != null) {
            setUpRecyclerView(rvRuns)
        }

        val spFilter = view.findViewById<Spinner>(R.id.spFilter)
        when(viewModel.sortType){
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE-> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{ // Note that this object is one that we can use in association with clicking something
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) { // Note that we re named p0, p1, p2 and p3
                when(pos){
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }



        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })
        val fab = view.findViewById<View>(R.id.fab)
        fab.setOnClickListener{
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)

        }
    }

    //
    /*
    private fun setUpRecyclerView() = rvRuns?.apply {
        runAdapter = RunAdapter() // Then we create an instance of it here
        // then to set the adapter of the recycler view
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())

    }

     */

    private fun setUpRecyclerView(rvRuns: RecyclerView) {
        runAdapter = RunAdapter()
        rvRuns.adapter = runAdapter
        rvRuns.layoutManager = LinearLayoutManager(requireContext())
    }



    private fun requestPermission(){
        if(TrackingUtility.hasLocationPermissions(requireContext())){ // Note that "requireContext()" here is used so as not to leave a nullable context there
            return // This is needed since the user probably does not need to be asked about the permission anymore
        }
        // Just in case the user has turned off the permission in Android Q below
        if(Build.VERSION.SDK_INT <Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this, // This is for the fragment
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,// This will let us know if the user has accepted or rejected permission // So we can save it as constant "0" initially
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION // This will ask for the permission for the fine and coarse location


            )
        } else{
            EasyPermissions.requestPermissions(
                this, // This is for the fragment
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,// This will let us know if the user has accepted or rejected permission // So we can save it as constant "0" initially
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION, // This is for android Q and above




            )

        }
    }

    // Note that this two functions below are autogenerated after adding the interface "EasyPermissions.PermissionCallbacks " to this fragment by pressing "Ctrl + I"

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }



    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){ // "this" is referring to this fragment and then the "perms" here refer to all the possible permissions
            AppSettingsDialog.Builder(this).build().show() // This will direct the user to the settings page

        } else {
            requestPermission()
        }

    }

    // Then we add this function to check the result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Then we add this
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this) // "this" here is used to add it to the fragment
    }



}