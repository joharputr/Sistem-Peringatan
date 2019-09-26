package com.example.systemperingatan.Admin

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.SQLException
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.DataItem
import com.example.systemperingatan.API.NetworkConfig
import com.example.systemperingatan.Admin.MapsActivity.Companion.GEOFENCE_EXPIRATION_IN_MILLISECONDS
import com.example.systemperingatan.App
import com.example.systemperingatan.Notification.GeofenceTransitionService
import com.example.systemperingatan.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_add_new_map.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.math.roundToInt

class AddNewMap : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mGoogleApiClient: GoogleApiClient? = null

    private var mSharedPreferences: SharedPreferences? = null

    private var reminder = DataItem(null, null, null, null, null, null, null, null, null)
    val newGeofenceNumber: Int
        get() {
            val number = mSharedPreferences!!.getInt(MapsActivity.NEW_GEOFENCE_NUMBER, 1)
            val editor = mSharedPreferences!!.edit()
            editor.putInt(MapsActivity.NEW_GEOFENCE_NUMBER, number + 1)
            editor.apply()
            return number
        }

    //set ukuran radius
    private val radiusBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateRadiusWithProgress(progress)
            showReminderUpdate()
        }
    }

    private fun updateRadiusWithProgress(progress: Int) {
        val radius = getRadius(progress)
        reminder.radius = radius.toString()
        radiusDescription.text = getString(R.string.radius_description, radius.roundToInt().toString())
    }

    companion object {
        private var map: GoogleMap? = null
        val GEOFENCE_RADIUS_IN_METERS = 100f // 100 m
        val GEOFENCE_EXPIRATION_IN_HOURS: Long = 12

        private var mLocationRequest: LocationRequest? = null
        private val MY_PERMISSION_REQUEST_CODE = 7192
        private var mLastLocation: Location? = null
        private var mpendingIntent: PendingIntent? = null
        private var locationMarker: Marker? = null
        private const val EXTRA_LAT_LNG = "EXTRA_LAT_LNG"
        private const val EXTRA_ZOOM = "EXTRA_ZOOM"
        private val PLAY_SERVICE_RESOLUTION_REQUEST = 300193
        fun newIntent(context: Context, latLng: LatLng, zoom: Float): Intent {
            val intent = Intent(context, AddNewMap::class.java)
            intent.putExtra(EXTRA_LAT_LNG, latLng).putExtra(EXTRA_ZOOM, zoom)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_map)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        instructionTitle.visibility = View.GONE

        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.GONE
        mSharedPreferences = getSharedPreferences(MapsActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpLocation()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient!!.disconnect()
    }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
    }

    private fun setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //request runtime permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_REQUEST_CODE)

        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient()
                createLocationRequest()
                displayLocation()
            }
        }
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("LOG ONConnection failed", "failed status code = " + connectionResult.errorMessage!!)
    }

    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        displayLocation()
        startLocationUpdates()
        //recoverlocationMarker();
    }

    //add gps location now
    private fun displayLocation() {
        Log.d("LOG Cek lokasi", "cek lokasi")
        if (map != null) {
            if (checkPermission()) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                if (mLastLocation != null) {
                    Log.i("LOG LAST GPS LOCATION", "LasKnown location. " +
                            "Long: " + mLastLocation!!.longitude +
                            " | Lat: " + mLastLocation!!.latitude)
                    //add gps location
                    markerLocation(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
                } else {
                    Log.w("LOG FAILED", "No location retrieved yet")
                    //                startLocationUpdates();
                }
            } else {
                askPermission()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        displayLocation()
    }

    private fun startLocationUpdates() {
        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
    }

    //move camera to current gps location
    private fun markerLocation(latLng: LatLng) {
        Log.i("LOG TLOCATION", "markerLocation($latLng)")
        val title = latLng.latitude.toString() + ", " + latLng.longitude
        val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("lokasi saya = $title")
        if (map != null) {
            // Remove the anterior marker
            if (locationMarker != null)
                locationMarker!!.remove()
            locationMarker = map!!.addMarker(markerOptions)
            val zoom = 14f
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        //    map!!.animateCamera(cameraUpdate)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpLocation()
            } else {
                permissionDenied()
            }

        }
    }

    private fun logSecurityException(securityException: SecurityException) {
        Log.e("LOG ERROR PERMISSION", "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException)
    }

    private fun permissionDenied() {
        Log.d("TEST", "permission denied")
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), AddNewMap.MY_PERMISSION_REQUEST_CODE)
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        val UPDATE_INTERVAL = 5000
        mLocationRequest!!.interval = UPDATE_INTERVAL.toLong()
        val FATEST_INTERVAL = 3000
        mLocationRequest!!.fastestInterval = FATEST_INTERVAL.toLong()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val DISPLACEMENT = 10
        mLocationRequest!!.smallestDisplacement = DISPLACEMENT.toFloat()
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()

    }

    private fun checkPlayServices(): Boolean {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode))
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, PLAY_SERVICE_RESOLUTION_REQUEST).show()
            else {
                Toast.makeText(this, "This device not supported", Toast.LENGTH_SHORT).show()
                finish()
            }
            return false
        }
        return true
    }

    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
        Log.d("CREATE GEO REQUEST", "createGeofenceRequest")
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.uiSettings.isMapToolbarEnabled = false
        map!!.isMyLocationEnabled = true
        centerCamera()

        showConfigureLocationStep(googleMap.cameraPosition.target)
    }

    private fun createGeofencePendingIntent(): PendingIntent {
        Log.d("LOGCREATEPENDING INTENT", "createGeofencePendingIntent")
        if (mpendingIntent != null) {
            Log.d("Pending gagal", "pending ggagal")
            return mpendingIntent as PendingIntent
        }
        val intent = Intent(this, GeofenceTransitionService::class.java)
        Log.d("LOG Pending test", "pending test")
        val GEOFENCE_REQ_CODE = 0
        return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun centerCamera() {
        val latLng = intent.extras.get(EXTRA_LAT_LNG) as LatLng
        val zoom = intent.extras.get(EXTRA_ZOOM) as Float
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    //step 1
    private fun showConfigureLocationStep(latLng: LatLng) {
        marker.visibility = View.VISIBLE
        instructionTitle.visibility = View.VISIBLE

        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.GONE
        instructionTitle.text = getString(R.string.instruction_where_description)
        next.setOnClickListener {
            reminder.latlang = map!!.cameraPosition.target
            reminder.latitude = latLng.latitude.toString()
            reminder.longitude = latLng.longitude.toString()
            showConfigureRadiusStep()
        }
        showReminderUpdate()
    }

    //step 2
    private fun showConfigureRadiusStep() {
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE

        radiusBar.visibility = View.VISIBLE
        radiusDescription.visibility = View.VISIBLE
        message.visibility = View.GONE
        instructionTitle.text = getString(R.string.instruction_radius_description)
        next.setOnClickListener {
            showConfigureMessageStep()
        }
        radiusBar.setOnSeekBarChangeListener(radiusBarChangeListener)
        updateRadiusWithProgress(radiusBar.progress)

        map!!.animateCamera(CameraUpdateFactory.zoomTo(15f))

        showReminderUpdate()
    }

    private fun getRadius(progress: Int) = 100 + (2 * progress.toDouble() + 1) * 100

    //step 3
    private fun showConfigureMessageStep() {
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.VISIBLE
        instructionTitle.text = getString(R.string.instruction_message_description)
        next.setOnClickListener {
            hideKeyboard(this, message)
            reminder.message = message.text.toString()
            if (reminder.message.isNullOrEmpty()) {
                message.error = getString(R.string.error_required)
            } else {
                addLocation()
                Log.d("reminderMessage =", reminder.message)
            }

        }
        // message.requestFocusWithKeyboard()

    }

    fun hideKeyboard(context: Context, view: View) {
        val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showReminderUpdate() {
        map!!.clear()
        showReminderInMap(this, map!!, reminder)
    }

    fun showReminderInMap(context: Context,
                          map: GoogleMap,
                          reminder: DataItem) {
        if (reminder.latlang != null) {
            Log.d("CLOG", "latlang = " + reminder.latlang)
            val latLng = reminder.latlang as LatLng
            val marker = map.addMarker(MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            marker.tag = reminder.id
            if (reminder.radius != null) {
                val radius = java.lang.Double.parseDouble(reminder.radius)
                map.addCircle(CircleOptions()
                        .center(reminder.latlang)
                        .radius(radius)
                        .strokeColor(ContextCompat.getColor(context, R.color.colorAccent))
                        .fillColor(ContextCompat.getColor(context, R.color.wallet_holo_blue_light)))
            }
        }
    }

    //step 4
    private fun addLocation() {
        map!!.clear()

        val expTime = System.currentTimeMillis() + MapsActivity.GEOFENCE_EXPIRATION_IN_MILLISECONDS
        val key = newGeofenceNumber.toString() + ""
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkConfig.post, { response ->
            Log.d("CLOG", "responh: $response")
            try {
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("status post  = ", status1)
                if (status1.contains("200")) {
                    Toast.makeText(this, "Geofence Added!", Toast.LENGTH_SHORT).show()
                } else {

                    val msg = jObj.getString("message")
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("error catch = ", e.toString())
            }

        }, { error ->
            Log.d("CLOG", "verespon: ${error.localizedMessage}")
            val json: String?
            val response = error.networkResponse
            if (response != null && response.data != null) {
                json = String(response.data)
                val jObj: JSONObject?
                try {
                    jObj = JSONObject(json)
                    val msg = jObj.getString("message")
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        }) {
            override fun getParams(): Map<String, String> {
                // Posting parameters to login url
                val params = HashMap<String, String>()
                params["number"] = key
                params["latitude"] = reminder.latitude.toString()
                params["longitude"] = reminder.longitude.toString()
                params["expires"] = expTime.toString()
                params["radius"] = reminder.radius.toString()
                params["message"] = reminder.message.toString()
                //  params["latlang"] = reminder.latlang.toString()
                params["type"] = "circle"
                return params
            }
        }

        val geofence = Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(
                        java.lang.Double.parseDouble(reminder.latitude),
                        java.lang.Double.parseDouble(reminder.longitude),
                        java.lang.Float.parseFloat(reminder.radius)
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    createGeofenceRequest(geofence),
                    createGeofencePendingIntent()
            )
        } catch (securityException: SecurityException) {
            logSecurityException(securityException)
        } catch (e: SQLException) {
            e.stackTrace
        }

        // Adding request to request queue
        App.instance?.addToRequestQueue(strReq, tag_string_req)
        Log.d("CLOG", "number = " + key + " lat = " + reminder.latitude.toString() + " long = " +
                reminder.longitude.toString() + " exp = " + expTime + "radius = " + reminder.radius +
                " message = " + reminder.message + " latlang = " + reminder.latlang)


        setResult(Activity.RESULT_OK)
        finish()
    }
}
