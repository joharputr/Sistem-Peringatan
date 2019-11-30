package com.example.systemperingatan.Admin.UI.Activity

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.SQLException
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.App
import com.example.systemperingatan.BuildConfig
import com.example.systemperingatan.R
import com.example.systemperingatan.User.Notification.GeofenceTransitionService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_add_new_point.*
import kotlinx.android.synthetic.main.activity_edit_location_point.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class EditLocationPointActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // private lateinit var map: GoogleMap
    private var mpendingIntent: PendingIntent? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    private var reminder = DataItem(null, null, null, null, null, null, null, null, null, null, null)

    val newGeofenceNumber: Int
        get() {
            val NEW_GEOFENCE_NUMBER_POINT = BuildConfig.APPLICATION_ID + ".NEW_GEOFENCE_NUMBER_POINT"
            val number = mSharedPreferences!!.getInt(NEW_GEOFENCE_NUMBER_POINT, 1)
            val editor = mSharedPreferences!!.edit()
            editor.putInt(NEW_GEOFENCE_NUMBER_POINT, number + 1)
            editor.apply()
            return number
        }

    companion object {
        internal var latitude: Double = 0.toDouble()
        internal var longitude: Double = 0.toDouble()
        internal var expires: Long = 0

        internal var radiusMeter: Double = 0.toDouble()
        private var mSharedPreferences: SharedPreferences? = null
        private var map: GoogleMap? = null
        private var mLocationRequest: LocationRequest? = null
        private val MY_PERMISSION_REQUEST_CODE = 7192
        private var mLastLocation: Location? = null
        private var locationMarker: Marker? = null
        private const val EXTRA_LAT_LNG = "EXTRA_LAT_LNG"
        private const val EXTRA_ZOOM = "EXTRA_ZOOM"
        private val PLAY_SERVICE_RESOLUTION_REQUEST = 300193

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_location_point)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_point) as SupportMapFragment
        setUpLocation()
        mapFragment.getMapAsync(this)
        val SHARED_PREFERENCES_NAME_POINT = BuildConfig.APPLICATION_ID + ".SHARED_PREFERENCES_NAME_POINT"
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME_POINT, Context.MODE_PRIVATE)
        mpendingIntent = null

        enableView()
    }

    private fun AddPointLocation() {
        addMarkerLocationPointEdit.setOnClickListener {
            showConfigureLocationStep()
        }
    }


    private fun finishAddPoint() {
        next_pointEdit.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.uiSettings.isMapToolbarEnabled = false
        map!!.isMyLocationEnabled = true

        AddPointLocation()
        val intent = intent
        val data = intent.getParcelableExtra<DataItem>("editLocationPoint")
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(data.latitude!!.toDouble(),
                data.longitude!!.toDouble()), 14f)
        map!!.animateCamera(cameraUpdate)
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

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
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

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_REQUEST_CODE)
    }

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

        }
    }


    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        displayLocation()
    }

    override fun onConnected(bundle: Bundle?) {
        displayLocation()
        startLocationUpdates()
        //recoverlocationMarker();
    }

    private fun startLocationUpdates() {
        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
    }

    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("LOG ONConnection failed", "failed status code = " + connectionResult.errorMessage!!)
    }


    private fun addMarkerPoint(latLng: LatLng, message: String, number: String) {
        map!!.addMarker(MarkerOptions()
                .title("G:$number area = $message")
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng))
    }

    private fun enableView() {
        addMarkerLocationPointEdit.visibility == View.VISIBLE
        next_pointEdit.visibility = View.VISIBLE
    }

    //step 1
    private fun showConfigureLocationStep() {
        addMarkerLocationPointEdit.visibility = View.GONE
        next_pointEdit.visibility = View.VISIBLE
        markerPointEdit.visibility = View.GONE
        if (map != null) {
            reminder.latlang = map?.cameraPosition?.target
            reminder.latitude = map!!.cameraPosition.target.latitude.toString()
            reminder.longitude = map!!.cameraPosition.target.longitude.toString()
            Log.d("CLOG", "radiusku = " + reminder.latlang.toString())

            val geocoder = Geocoder(this, Locale.getDefault())

            try {

                val address = geocoder.getFromLocation(map!!.cameraPosition.target.latitude, map!!.cameraPosition.target.longitude, 1)
                Log.d("addressTEST = ", address.get(0).getAddressLine(0))
                reminder.address = address.get(0).getAddressLine(0)
            } catch (e: IOException) {
                when {
                    e.message == "grpc failed" -> {/* ignore */
                    }
                    else -> throw e
                }
                Log.d("ErrorGocoder = ", e.localizedMessage)
            }


            if (!mGoogleApiClient!!.isConnected) {

                Toast.makeText(this, "Google Api not connected!", Toast.LENGTH_SHORT).show()
                return
            }

            val key = newGeofenceNumber.toString() + ""
            //   val list = ArrayList<Result>()
            val expTime = System.currentTimeMillis() + MapsAdminActivity.GEOFENCE_EXPIRATION_IN_MILLISECONDS
            val data = intent.getParcelableExtra<DataItem>("editLocationPoint")
            addMarkerPoint(reminder.latlang!!, data.message.toString(), key)
            val geofence = Geofence.Builder()
                    .setRequestId(key)
                    .setCircularRegion(
                            reminder.latitude!!.toDouble(),
                            reminder.longitude!!.toDouble(),
                            100f
                    )
                    .setExpirationDuration(MapsAdminActivity.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()

            try {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        createGeofenceRequest(geofence),
                        createGeofencePendingIntent()
                ).setResultCallback { status ->
                    if (status.isSuccess) {
                        Log.d("__DEBUG", "key :" + key + " Latitude :" + reminder.latitude + " Longitude :" + reminder.longitude + " expTime:" + expTime)
                        val data = intent.getParcelableExtra<DataItem>("editLocationPoint")
                        val tag_string_req = "req_postdata"
                        val strReq = object : StringRequest(Method.POST,
                                NetworkAPI.edit+"/${data.number}", { response ->
                            Log.d("CLOG", "responh: $response")
                            try {
                                val jObj = JSONObject(response)
                                val status1 = jObj.getString("status")
                                if (status1.contains("200")) {
                                    Toast.makeText(this, "sukses", Toast.LENGTH_SHORT).show()

                                } else {
                                    val msg = jObj.getString("message")
                                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                        }, { error ->
                            Log.d("CLOG", "verespon: $error")
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
                                params["latitude"] = reminder.latitude.toString()
                                params["longitude"] = reminder.longitude.toString()
                                params["address"] = reminder.address.toString()

                                return params
                            }
                        }

                        // Adding request to request queue
                        App.instance?.addToRequestQueue(strReq, tag_string_req)
                        Log.d("SAVE", "key = " + key + " lat = " + reminder.latitude + " long = " + reminder.longitude + " exp = " + expTime)
                        Toast.makeText(this@EditLocationPointActivity, "Sukses mengubah posisi zona evakuasi", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = GeofenceTransitionService.getErrorString(status.statusCode)
                        Log.e("ERRORMESSAGE", errorMessage)
                    }
                }
            } catch (securityException: SecurityException) {
                logSecurityException(securityException)
            } catch (e: SQLException) {
                e.stackTrace
            }
            finishAddPoint()
        }
    }


    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
        Log.d("CREATE GEO REQUEST", "createGeofenceRequest")
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
    }

    private fun createGeofencePendingIntent(): PendingIntent {

        Log.d("CREATE PENDING INTENT", "createGeofencePendingIntent")
        if (mpendingIntent != null) {
            Log.d("Pending intent gagal", "pending gagal")
            return mpendingIntent as PendingIntent
        }
        val intent = Intent(this, GeofenceTransitionService::class.java)
        Log.d("Pending test", "pending test")
        val GEOFENCE_REQ_CODE = 0
        return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun logSecurityException(securityException: SecurityException) {
        Log.e("ERROR PERMISSION", "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException)
    }
}
