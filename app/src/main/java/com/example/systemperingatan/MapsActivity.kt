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
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.Api
import com.example.systemperingatan.API.Data
import com.example.systemperingatan.API.NetworkConfig
import com.example.systemperingatan.SQLite.GeofenceDbHelper
import com.example.systemperingatan.SQLite.GeofenceStorage
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
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_maps.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status>, GoogleMap.OnInfoWindowClickListener {
    private var mMap: GoogleMap? = null
    //sharedpref
    private var mSharedPreferences: SharedPreferences? = null
    private val KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE"
    private val KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE"

    // Draw Geofence circle on GoogleMap
    private var geoFenceLimits: Circle? = null
    private var mLocationRequest: LocationRequest? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private var mpendingIntent: PendingIntent? = null

    private var locationMarker: Marker? = null
    internal var geofenceDbHelper: GeofenceDbHelper? = null

    //spinner
    private var spName: Spinner? = null

    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    internal var expires: Long = 0
    internal var area: String? = null

    internal var add: FloatingActionButton? = null
    //Retrofit
    internal var api = NetworkConfig.client.create<Api>(Api::class.java!!)
    private var fab_main: FloatingActionButton? = null
    private var fab1_mail: FloatingActionButton? = null
    private var fab2_share: FloatingActionButton? = null
    private var fab_open: Animation? = null
    private var fab_close: Animation? = null
    private var fab_clock: Animation? = null
    private var fab_anticlock: Animation? = null

    internal var isOpen: Boolean? = false

    private//        int a[] = {1,2,3};
    //        int number = Array.getInt(a, 1);
    val newGeofenceNumber: Int
        get() {
            val number = mSharedPreferences!!.getInt(NEW_GEOFENCE_NUMBER, 1)
            val editor = mSharedPreferences!!.edit()
            editor.putInt(NEW_GEOFENCE_NUMBER, number + 1)
            editor.apply()
            return number
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initMap()
        setUpLocation()
        seekbarFunction()
        //  clickStart();
        //  clickRemove();


        spName = findViewById<View>(R.id.spinner) as Spinner

        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        mpendingIntent = null


        fab_main = findViewById(R.id.fab)
        fab1_mail = findViewById(R.id.fab1)
        fab2_share = findViewById(R.id.fab2)
        fab_close = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        fab_open = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fab_clock = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_fab_clock)
        fab_anticlock = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_anticlock)


        fab_main!!.setOnClickListener {
            if (isOpen!!) {
                textview_mail.visibility = View.INVISIBLE
                textview_share.visibility = View.INVISIBLE
                fab2_share!!.startAnimation(fab_close)
                fab1_mail!!.startAnimation(fab_close)
                fab_main!!.startAnimation(fab_anticlock)
                fab2_share!!.isClickable = false
                fab1_mail!!.isClickable = false
                isOpen = false
            } else {
                textview_mail.visibility = View.VISIBLE
                textview_share.visibility = View.VISIBLE
                fab2_share!!.startAnimation(fab_open)
                fab1_mail!!.startAnimation(fab_open)
                fab_main!!.startAnimation(fab_clock)
                fab2_share!!.isClickable = true
                fab1_mail!!.isClickable = true
                isOpen = true
            }
        }


        fab2_share!!.setOnClickListener { Toast.makeText(applicationContext, "Share", Toast.LENGTH_SHORT).show() }

        fab1_mail!!.setOnClickListener { Toast.makeText(applicationContext, "Email", Toast.LENGTH_SHORT).show() }

    }


    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        assert(mapFragment != null)
        mapFragment!!.getMapAsync(this)
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


    /*   private void clickRemove() {
           Button removeGeo = findViewById(R.id.removeGeofence);
           removeGeo.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   removeGeofence();
               }
           });
       }
   */
    private fun removeGeofence() {
        Log.d("", "clearGeofence()")
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback { status ->
            if (status.isSuccess) {
                // remove drawing
                removeGeofenceDraw()
            }
        }
    }

    private fun removeGeofenceDraw() {
        Log.d("", "removeGeofenceDraw()")
        if (locationMarker != null) {
            locationMarker!!.remove()
        }
        if (null != geoFenceLimits) {
            geoFenceLimits!!.remove()
        }
    }

    /*  private void clickStart() {
        Button startGeo = (Button) findViewById(R.id.startGeofence);
        startGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGeofence();
            }
        });
    }*/

    //add gps location now
    private fun displayLocation() {
        Log.d("Cek lokasi", "cek lokasi")
        if (mMap != null) {
            if (checkPermission()) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                if (mLastLocation != null) {
                    Log.i("LAST GPS LOCATION", "LasKnown location. " +
                            "Long: " + mLastLocation!!.longitude +
                            " | Lat: " + mLastLocation!!.latitude)
                    //add gps location
                    markerLocation(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
                } else {
                    Log.w("FAILED", "No location retrieved yet")
                    //                startLocationUpdates();
                }
            } else {
                askPermission()
            }
        }
    }

    //move camera to current gps location
    private fun markerLocation(latLng: LatLng) {
        Log.i("MARKER CURRENT LOCATION", "markerLocation($latLng)")
        val title = latLng.latitude.toString() + ", " + latLng.longitude
        val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title)
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

    private fun startGeofence() {
        Log.i("START GEOFENCE", "startGeofence()")
        if (locationMarker != null) {
            val geofence = createGeofence(locationMarker!!.position)
            val geofenceRequest = createGeofenceRequest(geofence)
            addGeofence(geofenceRequest)
        } else {
            Log.e("GEOFENCE MARKER NULL", "Geofence marker is null")
        }
    }

    private fun createGeofence(latLng: LatLng): Geofence {
        Log.d("CREATE GEOFENCE", "createGeofence")
        return Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        MapsActivity.GEOFENCE_RADIUS)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }

    // Create a Geofence Request
    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
        Log.d("CREATE GEO REQUEST", "createGeofenceRequest")
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private fun addGeofence(request: GeofencingRequest) {
        Log.d("", "addGeofence")
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this)
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
        Log.d("onMapReady", "onMapReady()")
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

    // Recovering last Geofence marker
    private fun recoverlocationMarker() {
        Log.d("", "recoverlocationMarker")
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        if (sharedPref.contains(KEY_GEOFENCE_LAT) && sharedPref.contains(KEY_GEOFENCE_LON)) {
            val lat = java.lang.Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LAT, -1))
            val lon = java.lang.Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LON, -1))
            val latLng = LatLng(lat, lon)
            markerForGeofence(latLng)
            drawGeofence()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("ONConnection failed", "failed status code = " + connectionResult.errorMessage!!)
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        displayLocation()
    }

    private fun seekbarFunction() {

        verticalSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(progress.toFloat()), 2000, null)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    override fun onResult(status: Status) {

    }

    private fun saveGeofence() {
        Log.d("", "saveGeofence()")
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putLong(KEY_GEOFENCE_LAT, java.lang.Double.doubleToRawLongBits(locationMarker!!.position.latitude))
        editor.putLong(KEY_GEOFENCE_LON, java.lang.Double.doubleToRawLongBits(locationMarker!!.position.longitude))
        editor.apply()
    }

    //add circle area
    private fun drawGeofence() {
        Log.d("", "drawGeofence()")
        //remove last circle draw
        if (geoFenceLimits != null)
            geoFenceLimits!!.remove()

        val circleOptions = CircleOptions()
                .center(locationMarker!!.position)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS.toDouble())
        geoFenceLimits = mMap!!.addCircle(circleOptions)
    }

    override//when click the map
    fun onMapClick(latLng: LatLng) {
        Log.d("", "onMapClick($latLng)")
        //  linearLayout.setVisibility(View.VISIBLE);
        meter.visibility = View.VISIBLE
        radius.visibility = View.VISIBLE
        meter.visibility = View.VISIBLE
        spName!!.visibility = View.GONE
        markerForGeofence(latLng)
        //  setupSpinner();
    }

    private fun markerForGeofence(latLng: LatLng) {
        if (!mGoogleApiClient!!.isConnected) {
            Toast.makeText(this, "Google Api not connected!", Toast.LENGTH_SHORT).show()
            return
        }
        val key = newGeofenceNumber.toString() + ""
        val expTime = System.currentTimeMillis() + GEOFENCE_EXPIRATION_IN_MILLISECONDS
        addMarker(key, latLng)
        val geofence = Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    createGeofenceRequest(geofence),
                    createGeofencePendingIntent()
            ).setResultCallback { status ->
                if (status.isSuccess) {
                    //    saveGeofence();
                    //    drawGeofence();
                    GeofenceDbHelper.saveToDb(key, latLng.latitude, latLng.longitude, expTime)
                    Log.d("__DEBUG", "key :" + key + " Latitude :" + latLng.latitude + " Longitude :" + latLng.longitude + " expTime:" + expTime)
                    val tag_string_req = "req_postdata"
                    val strReq = object : StringRequest(Request.Method.POST,
                            NetworkConfig.post, { response ->
                        Log.d("CLOG", "responh: $response")
                        try {
                            val jObj = JSONObject(response)
                            val status1 = jObj.getString("status")
                            if (status1.contains("200")) {
                                val jArray = jObj.getJSONArray("data")
                                for (i in 0 until jArray.length()) {
                                    val jData = jArray.getJSONObject(i)
                                    val gson = Gson()
                                    val parser = JsonParser()
                                    val mJson = parser.parse(jData.toString())
                                }

                            } else {

                                val msg = jObj.getString("message")
                                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }


                    }, { error ->
                        Log.d("CLOG", "verespon: $error")
                        var json: String? = null
                        val response = error.networkResponse
                        if (response != null && response.data != null) {
                            json = String(response.data)
                            var jObj: JSONObject? = null
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
                            params["numbers"] = key
                            params["latitude"] = latLng.latitude.toString()
                            params["longitude"] = latLng.longitude.toString()
                            params["expires"] = expTime.toString()
                            return params
                        }
                    }

                    // Adding request to request queue
                    App.instance?.addToRequestQueue(strReq, tag_string_req)


                    Log.d("SAVE", "key = " + key + " lat = " + latLng.latitude + " long = " + latLng.longitude + " exp = " + expTime)
                    Toast.makeText(this@MapsActivity, "Geofence Added!", Toast.LENGTH_SHORT).show()
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

    private fun logSecurityException(securityException: SecurityException) {
        Log.e("ERROR PERMISSION", "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException)
    }

    private fun addMarker(key: String, latLng: LatLng) {
        mMap!!.addMarker(MarkerOptions()
                .title("G:$key")
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng))
        mMap!!.addCircle(CircleOptions()
                .center(latLng)
                .radius(GEOFENCE_RADIUS_IN_METERS.toDouble())
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#80ff0000")))
    }


    //info from marker
    override fun onInfoWindowClick(marker: Marker) {
        val requestId = marker.title.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
        if (!mGoogleApiClient!!.isConnected) {
            Toast.makeText(this, "GeoFence Not connected!", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val idList = ArrayList<String>()
            // perlu dikembangkan
            idList.add(requestId)
            Log.d("idlist = ", idList.toString())
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idList).setResultCallback { status ->
                if (status.isSuccess) {
                    //remove from db
                    GeofenceStorage.removeGeofence(requestId)
                    Log.d("REMOVE", "key = $requestId")
                    Toast.makeText(this@MapsActivity, "Geofence removed!", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        reloadMapMarkers()
                    }
                } else {
                    // Get the status code for the error and log it using a UserActivity-friendly message.
                    val errorMessage = GeofenceTransitionService.getErrorString(status.statusCode)
                    Log.e("ERROR WINDOW CLICK", errorMessage)
                }
            }

        } catch (e: SecurityException) {
            e.localizedMessage
        } catch (e: SQLException) {
            e.stackTrace
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private//load from db
    fun reloadMapMarkers() {
        mMap!!.clear()
        api.allData.enqueue(object : Callback<Data> {
            override fun onResponse(call: Call<Data>, response: Response<Data>) {
                val data = response.body()
                for (i in 0 until data!!.result!!.size) {
                    if (data!!.result != null) {
                        val number = data.result?.get(i)?.numbers
                        latitude = java.lang.Double.parseDouble(data.result?.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.result?.get(i)?.longitude)
                        expires = java.lang.Long.parseLong(data.result?.get(i)?.expires)
                        Toast.makeText(this@MapsActivity, response.message(), Toast.LENGTH_SHORT).show()
                        addMarker(number!!, LatLng(latitude, longitude))
                    } else {
                        Toast.makeText(this@MapsActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }

                Log.d("test data", "latitude =" + latitude + "longitude =" + longitude + "expires =" + expires)
            }

            override fun onFailure(call: Call<Data>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@MapsActivity, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })


    }


    override fun onMarkerClick(marker: Marker): Boolean {

        return false
    }

    companion object {
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