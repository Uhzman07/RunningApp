package com.example.runningapp.ui.theme.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.MAP_ZOOM
import com.example.runningapp.other.Constants.POLYLINE_COLOR
import com.example.runningapp.other.Constants.POLYLINE_WIDTH
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.services.Polyline
import com.example.runningapp.services.TrackingService
import com.example.runningapp.ui.theme.ViewModel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"
// Note that if we are using a map then we can make use of a map view or a map fragment in our xml file
// Note that whenever we are trying to inject something into our android component, we need need to add "@AndroidEntryPoint"
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel : MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()


    private var map : GoogleMap?= null // This is of the type "GoogleMap" and this is the actual map object

    private var curTimeInMillis = 0L

    private var menu: Menu? = null


    @set:Inject // Note that we are also using this because Float is also a primitive data type
    var weight = 80f


    // Then we create this
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) // Note that since we are inside a fragment and then we are not in an activity then we surely need to set this options menu
        // only in the main activity that this is set as default
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapView = view.findViewById<MapView>(R.id.mapView) // This is our map view from the xml, note that we have to put the type there
        // Then to add a lifecycle to the map
        mapView.onCreate(savedInstanceState)


        val btnToggleRun = view.findViewById<View>(R.id.btnToggleRun)
        val btnFinishRun = view.findViewById<View>(R.id.btnFinishRun)

        btnFinishRun.setOnClickListener{
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }



        mapView.getMapAsync{ // Here we are getting the map asynchronously
            map = it // Here we are then setting the map that we had created above to be our map
            // But we need to create a lifecycle for our map view also

            addAllPolylines() // Note that is called together with creating the map
        }

        val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted ->
            var hasNotificationPermissionGranted = isGranted
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                Toast.makeText(requireContext(),"Permission granted!!",Toast.LENGTH_SHORT).show()
                //sendCommandToService(ACTION_START_OR_RESUME_SERVICE)  // sendCommandToService(ACTION_START_OR_RESUME_SERVICE)  // So when we run the app, we get the message in our log place

            }

        }

        btnToggleRun.setOnClickListener{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                toggleRun()

            }

        }

        subscribeToObservers() // this is also very important for the observers to work well


        // Then to prevent the alert dialogue from crashing with resize
        if(savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG
            ) as CancelTrackingDialog? // Note that we must perform the nullable check or else it will crash

            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }




    }
    // This is used to zoom the camera based on the position of the user
    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(), // This is the coordinate point that we want our map to concentrate on
                    MAP_ZOOM

                )

            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val mapView = view?.findViewById<MapView>(R.id.mapView)
        val bounds = LatLngBounds.Builder() // This is the thing that holds all the coordinates
        for(polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        // Note that we are not animating our camera here
        if (mapView != null) {
            map?.moveCamera( // This
                CameraUpdateFactory.newLatLngBounds(
                    bounds.build(),
                    mapView.width,
                    mapView.height,
                    (mapView.height * 0.05).toInt() // This will be the padding so as to centralize the map

                )
            )
        }
    }

    private fun endRunAndSaveToDb(){
        // Note that calling a snapshot on the map gives us a bitmap
        map?.snapshot { bmp->
            var distanceInMeters = 0
            for(polyline in pathPoints){
                // To then save the total distance
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f// This is to get our time in km/hr

            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            // Then to put all the information in a data class for the
            val run = Run( bmp , dateTimeStamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)

            viewModel.insertRun(run) // Since the view model has the repository that we need, we can the use it to insert the run

            // To then show a snackBar to tell a user that the run was saved
            // Note that we must not show the snackBar in the fragment that we are calling it
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView), // This is the id of the main activity
                "Run saved successfully",
                Snackbar.LENGTH_LONG

            ).show()

            stopRun() // Then we want to reset all after saving the run


        }
    }

    // Note that we only observe mutable live data
    private fun subscribeToObservers(){ // Note that observers are just a way of getting fresh and very recent updates about the information that we require
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {  // Note that we have the "viewLifecycleOwner" instead of "this" because we are not in the tracking service class anymore
            updateTracking(it)

        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis,true)
            val timer = view?.findViewById<TextView>(R.id.tvTimer)
            timer?.text = formattedTime

        })
    }

    private fun toggleRun(){
        if(isTracking){ // This is when we are currently tracking so we might need to stop it
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    // then we need to create this also so as to set our menu to this fragment

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu // Then this sets the menu
    }

    //  This is what want to let happen when we are about creating the item
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true // This will make the icon visible once we have started the run

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }

        }
        return super.onOptionsItemSelected(item)
    }


    private fun showCancelTrackingDialog(){

        /*
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)

         */


        val dialog = CancelTrackingDialog()
        dialog.setYesListener {
            stopRun()
        }
        dialog.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)


    }

    private fun stopRun(){
        val tvTimer = view?.findViewById<TextView>(R.id.tvTimer)
        tvTimer?.text = "00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking : Boolean){
        val btnToggleRun = view?.findViewById<Button>(R.id.btnToggleRun)
        val btnFinishRun = view?.findViewById<Button>(R.id.btnFinishRun)
        this.isTracking = isTracking
        if(!isTracking && curTimeInMillis > 0L){ // This is when it is not tracking currently

            btnToggleRun?.text = "Start"
            btnFinishRun?.visibility = View.VISIBLE

        }else if(isTracking){ // this is when we are currently tracking
            menu?.getItem(0)?.isVisible = true

            btnToggleRun?.text = "STOP"
            btnFinishRun?.visibility = View.GONE


        }
    }




    // This is to add all the polylines just in case our app starts again
    private fun addAllPolylines(){
        for(polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }


    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size>1){ // This is to check if the pathPoints is not empty and if the last coordinate point in the map contains 2 values which is the latitude and the longitude
            val preLastLastLng = pathPoints.last()[pathPoints.last().size-2] // This is used to get the second to the last coordinate
            val lastLatLng = pathPoints.last().last() // This is the last coordinate
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLastLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)


        }
    }

    // Then we create the function that will help us to send the service
    private fun sendCommandToService(action:String){
        Intent(requireContext(),TrackingService::class.java).also {  // This is used to send the intent to the service
            it.action= action // "it" here is referring to the intent
            requireContext().startService(it) // This is the function that delivers our service to our intent and then reacts to our command
            // Then we should know that we should always add our service to our maifest file

        }
    }

    // Then we create the lifecycle of the map so that our application does not crash
    override fun onResume() {
        super.onResume()
        val mapView = view?.findViewById<MapView>(R.id.mapView)
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        val mapView = view?.findViewById<MapView>(R.id.mapView)
        mapView?.onStart()


    }

    override fun onStop() {
        super.onStop()
        val mapView = view?.findViewById<MapView>(R.id.mapView)
        mapView?.onStop()

    }

    override fun onPause() {
        super.onPause()
        val mapView = view?.findViewById<MapView>(R.id.mapView)
        mapView?.onPause()

    }

    override fun onLowMemory() {
        super.onLowMemory()
        val mapView = view?.findViewById<MapView>(R.id.mapView)
        mapView?.onLowMemory()

    }
    /*
    // Note that since we had got the onStop() function then this might not be too necessary
    override fun onDestroy() {
        super.onDestroy()
        val mapView = view?.findViewById<MapView>(R.id.mapView)
        mapView?.onDestroy()

    }
     */

    // This is used to cache the map so it is saved temporarily and then we do not have to load it everytime
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapView = view?.findViewById<MapView>(R.id.mapView)
        mapView?.onSaveInstanceState(outState)

    }

}
