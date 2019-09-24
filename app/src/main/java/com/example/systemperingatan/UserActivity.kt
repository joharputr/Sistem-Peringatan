package com.example.systemperingatan

import android.Manifest
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
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Spinner
import android.widget.Toast
import com.example.systemperingatan.API.Api
import com.example.systemperingatan.API.Data
import com.example.systemperingatan.API.NetworkConfig
import com.example.systemperingatan.API.Result
import com.example.systemperingatan.SQLite.GeofenceDbHelper
import com.example.systemperingatan.SQLite.GeofenceStorage
import com.firebase.geofire.GeoFire
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class UserActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status>, GoogleMap.OnInfoWindowClickListener {

   // private val preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    //Retrofit
    internal var api = NetworkConfig.client.create(Api::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        initMap()
        setUpLocation()
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        mpendingIntent = null
        Log.d("dataGet = ", get("test").toString())
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = (supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpLocation()
            } else {
                permissionDenied()
            }

        }
    }

    private fun permissionDenied() {
        Log.d("TEST", "permission denied")
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_REQUEST_CODE)
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

    //add gps location now
    private fun displayLocation() {
        Log.d("LOG Cek lokasi", "cek lokasi")
        if (mMap != null) {
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

    //move camera to current gps location
    private fun markerLocation(latLng: LatLng) {
        Log.i("LOG TLOCATION", "markerLocation($latLng)")
        val title = latLng.latitude.toString() + ", " + latLng.longitude
        val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("lokasi saya = $title")
        if (mMap != null) {
            // Remove the anterior marker
            if (locationMarker != null)
                locationMarker!!.remove()
            locationMarker = mMap!!.addMarker(markerOptions)
            val zoom = 14f
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            mMap!!.animateCamera(cameraUpdate)
        }
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


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("LOG onMapReady", "onMapReady()")
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        //add gps logo
        mMap!!.isMyLocationEnabled = true
        mMap!!.setOnMapClickListener(this)
        mMap!!.setOnMarkerClickListener(this)
        mMap!!.setOnInfoWindowClickListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            reloadMapMarkers()
        }
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

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        displayLocation()
    }


    override fun onResult(status: Status) {

    }


    override//when click the map
    fun onMapClick(latLng: LatLng) {
        Log.d("LOG", "onMapClick($latLng)")

    }


    private fun logSecurityException(securityException: SecurityException) {
        Log.e("LOG ERROR PERMISSION", "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException)
    }

    private fun addMarker(key: String?, latLng: LatLng) {
        mMap!!.addMarker(MarkerOptions()
                .title("G:" + key!!)
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng))
        mMap!!.addCircle(CircleOptions()
                .center(latLng)
                .radius(GEOFENCE_RADIUS_IN_METERS.toDouble())
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#80ff0000")))

    }

    override fun onInfoWindowClick(marker: Marker) {
        val requestId = marker.title.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        if (!mGoogleApiClient!!.isConnected) {
            Toast.makeText(this, "GeoFence Not connected!", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val idList = ArrayList<String>()
            idList.add(requestId)
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idList).setResultCallback { status ->
                if (status.isSuccess) {
                    GeofenceStorage.removeGeofence(requestId)
                    Log.d("LOG REMOVE", "key = $requestId")
                    Toast.makeText(this@UserActivity, "Geofence removed!", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        reloadMapMarkers()
                    }
                } else {
                    // Get the status code for the error and log it using a UserActivity-friendly message.
                    val errorMessage = GeofenceTransitionService.getErrorString(status.statusCode)
                    Log.e("LOG ERROR WINDOW CLICK", errorMessage)
                }
            }

        } catch (e: SecurityException) {
            e.localizedMessage
        } catch (e: SQLException) {
            e.stackTrace
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private//select all from db
    fun reloadMapMarkers() {
        mMap!!.clear()
        api.allData().enqueue(object : Callback<Data> {
            override fun onResponse(call: Call<Data>, response: Response<Data>) {
                val data = response.body()
                Log.d("responseBody= ", data.toString())

                for (i in 0 until data!!.result!!.size) {
                    if (data.result != null) {
                        number = data.result!![i].numbers
                        latitude = java.lang.Double.parseDouble(data.result!![i].latitude!!)
                        longitude = java.lang.Double.parseDouble(data.result!![i].longitude!!)
                        expires = java.lang.Long.parseLong(data.result!![i].expires!!)
                        Toast.makeText(this@UserActivity, response.message(), Toast.LENGTH_SHORT).show()
                        addMarker(number, LatLng(latitude, longitude))

                        if (!mGoogleApiClient!!.isConnected) {
                            return
                        }

                        val geofence = Geofence.Builder()
                                .setRequestId(number)
                                .setCircularRegion(
                                        latitude,
                                        longitude,
                                        MapsActivity.GEOFENCE_RADIUS_IN_METERS
                                )
                                .setExpirationDuration(MapsActivity.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
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

                    } else {
                        Toast.makeText(this@UserActivity, "data kosong", Toast.LENGTH_SHORT).show()
                    }
                }

                saveAll(data.result!!)

                Log.d("dataSebernya = ", data.result.toString())
                Log.d("dataSharedPref  = ",getAll().toString())
                Log.d("test data", "latitude =" + latitude + "longitude =" + longitude + "expires =" + expires)
            }

            override fun onFailure(call: Call<Data>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@UserActivity, "gagal = " + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onMarkerClick(marker: Marker): Boolean {

        return false
    }


    companion object {

        fun getAll(): List<Result> {
            if (preferences!!.contains(MAPS)) {
                val remindersString = preferences!!.getString(MAPS, null)
                val arrayOfReminders = gson.fromJson(remindersString, Array<Result>::class.java)
                if (arrayOfReminders != null) {
                    return arrayOfReminders.toList()
                }
                Log.d("shareppref= ",remindersString)
            }
            return listOf()
        }
        fun getLast() = getAll().lastOrNull()

        fun get(requestId: String?) = getAll().firstOrNull {
            it.numbers == requestId

        }

        private fun getFirstReminder(triggeringGeofences: List<Geofence>): Result? {
            val firstGeofence = triggeringGeofences[0]
            return UserActivity.get(firstGeofence.requestId)
        }



        private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
            Log.d("CREATE GEO REQUEST", "createGeofenceRequest")
            return GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()
        }

        private fun saveAll(list: List<Result>) {
            preferences!!.edit().putString(MAPS, gson.toJson(list)).apply()
        }

        private var mMap: GoogleMap? = null
        //sharedpref
        private var preferences: SharedPreferences? = null
        private var mLocationRequest: LocationRequest? = null
        private var mGoogleApiClient: GoogleApiClient? = null
        private var mLastLocation: Location? = null
        private var mpendingIntent: PendingIntent? = null
        private var locationMarker: Marker? = null
        //spinner
        private val gson = Gson()
        internal var number: String? = null
        internal var latitude: Double = 0.toDouble()
        internal var longitude: Double = 0.toDouble()
        internal var expires: Long = 0
        private  val PREFS_NAME = "pref2"
        private  val MAPS = "mapsv3"
        val NEW_GEOFENCE_NUMBER = BuildConfig.APPLICATION_ID + ".NEW_GEOFENCE_NUMBER"
        private val MY_PERMISSION_REQUEST_CODE = 7192
        private val GEO_DURATION = (60 * 60 * 1000).toLong()
        private val PLAY_SERVICE_RESOLUTION_REQUEST = 300193
        private val GEOFENCE_RADIUS = 1000.0f // in meters
        private val GEOFENCE_REQ_ID = "My Geofence"
        val GEOFENCE_RADIUS_IN_METERS = 100f // 100 m
        val GEOFENCE_EXPIRATION_IN_HOURS: Long = 12
        val GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 1000
        val SHARED_PREFERENCES_NAME = BuildConfig.APPLICATION_ID + ".SHARED_PREFERENCES_NAME"
    }
}