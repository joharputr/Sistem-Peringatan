package com.example.systemperingatan.Admin

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
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
import android.widget.SeekBar
import android.widget.Toast
import com.example.systemperingatan.App.Companion.api
import com.example.systemperingatan.BuildConfig
import com.example.systemperingatan.Notification.GeofenceTransitionService
import com.example.systemperingatan.R
import com.example.systemperingatan.SQLite.GeofenceStorage
import com.example.systemperingatan.User.UserActivity
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
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status>, GoogleMap.OnInfoWindowClickListener {
    private var mMap: GoogleMap? = null
    //sharedpref
    private var mSharedPreferences: SharedPreferences? = null
    private var mLocationRequest: LocationRequest? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private var mpendingIntent: PendingIntent? = null
    lateinit var message: String
    private var locationMarker: Marker? = null
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    internal var radiusMeter: Double = 0.toDouble()
    internal var expires: Long = 0

    //   internal var api = NetworkConfig.client.create<Api>(Api::class.java)
    private var fab_main: FloatingActionButton? = null
    private var fab1_mail: FloatingActionButton? = null
    private var fab2_share: FloatingActionButton? = null
    private var fab3_titik: FloatingActionButton? = null
    private var fab4_user: FloatingActionButton? = null
    private var fab_open: Animation? = null
    private var fab_close: Animation? = null
    private var fab_clock: Animation? = null
    private var fab_anticlock: Animation? = null

    /*  val newGeofenceNumber: Int
          get() {
              val number = mSharedPreferences!!.getInt(NEW_GEOFENCE_NUMBER, 1)
              val editor = mSharedPreferences!!.edit()
              editor.putInt(NEW_GEOFENCE_NUMBER, number + 1)
              editor.apply()
              return number
          }
  */

    internal var isOpen: Boolean? = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initMap()
        setUpLocation()
        seekbarFunction()

        /* mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)*/
        mpendingIntent = null
        fab_main = findViewById(R.id.fab)
        fab1_mail = findViewById(R.id.fab1)
        fab2_share = findViewById(R.id.fab2)
        fab3_titik = findViewById(R.id.fab2_titik)
        fab4_user = findViewById(R.id.fab2_User)
        fab_close = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        fab_open = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fab_clock = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_fab_clock)
        fab_anticlock = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_anticlock)


        fab_main!!.setOnClickListener {
            if (isOpen!!) {
                textview_mail.visibility = View.INVISIBLE
                textview_share.visibility = View.INVISIBLE
                textview_titik.visibility =View.INVISIBLE
                textview_User.visibility = View.INVISIBLE

                fab2_share!!.startAnimation(fab_close)
                fab1_mail!!.startAnimation(fab_close)
                fab3_titik!!.startAnimation(fab_close)
                fab4_user!!.startAnimation(fab_close)
                fab_main!!.startAnimation(fab_anticlock)

                fab2_share!!.isClickable = false
                fab1_mail!!.isClickable = false
                fab3_titik!!.isClickable = false
                fab4_user!!.isClickable = false
                isOpen = false
            } else {
                textview_mail.visibility = View.VISIBLE
                textview_share.visibility = View.VISIBLE
                textview_titik.visibility = View.VISIBLE
                textview_User.visibility = View.VISIBLE

                fab2_share!!.startAnimation(fab_open)
                fab1_mail!!.startAnimation(fab_open)
                fab2_titik!!.startAnimation(fab_open)
                fab4_user!!.startAnimation(fab_open)
                fab_main!!.startAnimation(fab_clock)

                fab2_share!!.isClickable = true
                fab1_mail!!.isClickable = true
                fab2_titik!!.isClickable = true
                fab4_user!!.isClickable = true
                isOpen = true
            }
        }

        fab1_mail!!.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                reloadMapMarkers()
            }
        }

        fab2_share!!.setOnClickListener {
            mMap?.run {
                val intent = AddNewMap.newIntent(this@MapsActivity, cameraPosition.target, cameraPosition.zoom)
                startActivityForResult(intent, NEW_REMINDER_REQUEST_CODE)
            }
        }
        fab3_titik!!.setOnClickListener {
            mMap?.run {
                val intent = AddNewPoint.newIntent(this@MapsActivity, cameraPosition.target, cameraPosition.zoom)
                startActivityForResult(intent, NEW_REMINDER_REQUEST_CODE)
            }
        }
        fab4_user!!.setOnClickListener {
            startActivity(Intent(this,UserActivity::class.java))
        }

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
        val lat = latLng.latitude
        val long = latLng.longitude
        val latLng = "$lat,$long".split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val latitude = java.lang.Double.parseDouble(latLng[0])
        val longitude = java.lang.Double.parseDouble(latLng[1])
        val location = LatLng(latitude, longitude)
        try {
            titikGps = location
            Log.d("titikgps = ", titikGps.toString())
        }catch (e : NullPointerException){
           Log.d("CLOG","error = "+e.localizedMessage)
        }
        val markerOptions = MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title)
        if (mMap != null) {
            // Remove the anterior marker
            if (locationMarker != null)
                locationMarker!!.remove()
            locationMarker = mMap!!.addMarker(markerOptions)
            val zoom = 14f
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, zoom)
            mMap!!.animateCamera(cameraUpdate)
        }
    }

    // Create a Geofence Request
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

    override//when click the map
    fun onMapClick(latLng: LatLng) {
        Log.d("", "onMapClick($latLng)")
        //  linearLayout.setVisibility(View.VISIBLE);
        meter.visibility = View.VISIBLE
        radius.visibility = View.VISIBLE
        meter.visibility = View.VISIBLE
        //  markerForGeofence(latLng)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NEW_REMINDER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                reloadMapMarkers()
                Toast.makeText(this, "sukses menambahkan lokasi", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
               reloadMapMarkers()
        }
    }

    /* private fun markerForGeofence(latLng: LatLng) {
         if (!mGoogleApiClient!!.isConnected) {
             Toast.makeText(this, "Google Api not connected!", Toast.LENGTH_SHORT).show()
             return
         }
         val key = newGeofenceNumber.toString() + ""

         val list = ArrayList<Result>()
         val expTime = System.currentTimeMillis() + GEOFENCE_EXPIRATION_IN_MILLISECONDS
        // addMarker(key, latLng)
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
                             params["message"] = "dfdfdf"
                             params["type"] = "circle"

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
     }*/

    private fun logSecurityException(securityException: SecurityException) {
        Log.e("ERROR PERMISSION", "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException)
    }

    private fun addMarker(message: String, radius: Double, key: String, latitude: Double, longitude: Double) {
        val latLng = "$latitude,$longitude".split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val latitude = java.lang.Double.parseDouble(latLng[0])
        val longitude = java.lang.Double.parseDouble(latLng[1])
        val location = LatLng(latitude, longitude)
        mMap!!.addMarker(MarkerOptions()
                .title("G:$key pesan =  $message")
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(location))
        mMap!!.addCircle(CircleOptions()
                .center(location)
                .radius(radius)
                .strokeColor(R.color.wallet_holo_blue_light)
                .fillColor(Color.parseColor("#80ff0000")))
    }

    //info from marker
    override fun onInfoWindowClick(marker: Marker) {
        val requestId = marker.title.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
        Log.d("CLOGrequestId = ","id="+requestId)
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
                    //   val errorMessage = GeofenceTransitionService.getErrorString(status.statusCode)

                    //    Log.e("ERROR WINDOW CLICK", errorMessage)
                }
            }

        } catch (e: SecurityException) {
            e.localizedMessage
        } catch (e: SQLException) {
            e.stackTrace
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    //load from db
    fun reloadMapMarkers() {
        //   mMap!!.clear()
        api.allData().enqueue(object : Callback<com.example.systemperingatan.API.Response> {
            override fun onResponse(call: Call<com.example.systemperingatan.API.Response>, response: Response<com.example.systemperingatan.API.Response>) {
                val data = response.body()

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null && data.data.get(i)?.type == "circle") {
                        val number = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        expires = java.lang.Long.parseLong(data.data.get(i)?.expires)
                        radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        message = data.data.get(i)?.message.toString()
                        Toast.makeText(this@MapsActivity, response.message(), Toast.LENGTH_SHORT).show()
                        addMarker(message, radiusMeter, number!!, latitude, longitude)

                    } else {
                        Toast.makeText(this@MapsActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<com.example.systemperingatan.API.Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@MapsActivity, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onMarkerClick(marker: Marker): Boolean {

        return false
    }


    companion object {
        lateinit var titikGps: LatLng
        private const val NEW_REMINDER_REQUEST_CODE = 330
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