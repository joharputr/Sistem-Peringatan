package com.example.systemperingatan.Admin

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.SQLException
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Response
import com.example.systemperingatan.App
import com.example.systemperingatan.BuildConfig
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
import kotlinx.android.synthetic.main.activity_add_new_point.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.util.*

class AddNewPoint : AppCompatActivity(), GoogleMap.OnMapClickListener, OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // private lateinit var map: GoogleMap
    private var mpendingIntent: PendingIntent? = null
    private var mGoogleApiClient: GoogleApiClient? = null
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

        fun newIntent(context: Context, latLng: LatLng, zoom: Float): Intent {
            val intent = Intent(context, AddNewPoint::class.java)
            intent.putExtra(EXTRA_LAT_LNG, latLng).putExtra(EXTRA_ZOOM, zoom)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_point)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_point) as SupportMapFragment
        setUpLocation()
        mapFragment.getMapAsync(this)
        val SHARED_PREFERENCES_NAME_POINT = BuildConfig.APPLICATION_ID + ".SHARED_PREFERENCES_NAME_POINT"
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME_POINT, Context.MODE_PRIVATE)
        mpendingIntent = null
        reloadMapMarkers()
        finishAddPoint()
    }

    private fun finishAddPoint() {
        next_point.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.uiSettings.isMapToolbarEnabled = false
        map!!.isMyLocationEnabled = true
        centerCamera()
        map!!.setOnMapClickListener(this)
        reloadMapMarkers()
    }

    override fun onResume() {
        super.onResume()
        reloadMapMarkers()
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    reloadMapMarkers()
                }
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
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            //    map!!.animateCamera(cameraUpdate)
        }
    }

    private fun centerCamera() {
        val latLng = intent.extras.get(EXTRA_LAT_LNG) as LatLng
        val zoom = intent.extras.get(EXTRA_ZOOM) as Float
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
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

    fun reloadMapMarkers() {
        //   mMap!!.clear()
        App.api.allData().enqueue(object : Callback<Response> {
            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                val data = response.body()

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null && data.data.get(i)?.type == "circle") {
                        val number = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        expires = java.lang.Long.parseLong(data.data.get(i)?.expires)
                        radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        val messages = data.data.get(i)?.message.toString()
                        Log.d("CLOG", "test response " + response.message())
                        addMarker(messages, radiusMeter, number!!, latitude, longitude)

                    } else if (data.data != null && data.data.get(i)?.type == "point") {
                        val numberPoint = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        val messages = data.data.get(i)?.message.toString()
                        addMarkerPoint(LatLng(latitude, longitude), messages, numberPoint!!)

                    } else {
                        Toast.makeText(this@AddNewPoint, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }


            }

            override fun onFailure(call: Call<Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@AddNewPoint, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMarkerPoint(latLng: LatLng, message: String, number: String) {
        map!!.addMarker(MarkerOptions()
                .title("G:$number area = $message")
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng))
    }

    override fun onMapClick(latLng: LatLng) {
        Log.d("onMapClick", "map")
        pointTitle.visibility = View.VISIBLE
        message_point.visibility = View.VISIBLE
        ok_title_point.visibility = View.VISIBLE
        next_point.visibility = View.GONE

        ok_title_point.setOnClickListener {
            markerForGeofence(latLng)
            pointTitle.visibility = View.GONE
            message_point.visibility = View.GONE
            ok_title_point.visibility = View.GONE
            next_point.visibility = View.VISIBLE
        }
    }

    private fun addMarker(message: String, radius: Double, key: String, latitude: Double, longitude: Double) {
        val latLng = "$latitude,$longitude".split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val latitude = java.lang.Double.parseDouble(latLng[0])
        val longitude = java.lang.Double.parseDouble(latLng[1])
        val location = LatLng(latitude, longitude)
        map!!.addMarker(MarkerOptions()
                .title("G:$message")
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(location))
        map!!.addCircle(CircleOptions()
                .center(location)
                .radius(radius)
                .strokeColor(R.color.wallet_holo_blue_light)
                .fillColor(Color.parseColor("#80ff0000")))
    }

    private fun markerForGeofence(latLng: LatLng) {
        if (!mGoogleApiClient!!.isConnected) {
            Toast.makeText(this, "Google Api not connected!", Toast.LENGTH_SHORT).show()
            return
        }
        val key = newGeofenceNumber.toString() + ""
        //   val list = ArrayList<Result>()
        val expTime = System.currentTimeMillis() + MapsActivity.GEOFENCE_EXPIRATION_IN_MILLISECONDS

        addMarkerPoint(latLng, message_point.text.toString(), key)
        val geofence = Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        100f
                )
                .setExpirationDuration(MapsActivity.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    createGeofenceRequest(geofence),
                    createGeofencePendingIntent()
            ).setResultCallback { status ->
                if (status.isSuccess) {
                    Log.d("__DEBUG", "key :" + key + " Latitude :" + latLng.latitude + " Longitude :" + latLng.longitude + " expTime:" + expTime)

                    val tag_string_req = "req_postdata"
                    val strReq = object : StringRequest(Request.Method.POST,
                            NetworkAPI.post, { response ->
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
                            params["number"] = key
                            params["latitude"] = latLng.latitude.toString()
                            params["longitude"] = latLng.longitude.toString()
                            params["expires"] = expTime.toString()
                            params["message"] = message_point.text.toString()
                            params["type"] = "point"

                            return params
                        }
                    }

                    // Adding request to request queue
                    App.instance?.addToRequestQueue(strReq, tag_string_req)
                    Log.d("SAVE", "key = " + key + " lat = " + latLng.latitude + " long = " + latLng.longitude + " exp = " + expTime)
                    Toast.makeText(this@AddNewPoint, "Geofence Added!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = GeofenceTransitionService.getErrorString(status.statusCode)
                    Log.e("ERROR MESSAGE", errorMessage)
                }
            }
        } catch (securityException: SecurityException) {
            logSecurityException(securityException)
        } catch (e: SQLException) {
            e.stackTrace
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