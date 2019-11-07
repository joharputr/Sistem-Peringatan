package com.example.systemperingatan.Admin.UI.Activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.SQLException
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.App
import com.example.systemperingatan.App.Companion.api
import com.example.systemperingatan.BuildConfig
import com.example.systemperingatan.R
import com.example.systemperingatan.User.Notification.GeofenceTransitionService
import com.example.systemperingatan.User.UI.UserActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_maps.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MapsAdminActivity : AppCompatActivity(), LocationListener, NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status>, GoogleMap.OnInfoWindowClickListener {


    private lateinit var locationManager: LocationManager
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
    private lateinit var mDrawerLayout: DrawerLayout
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
    private var navigationView: NavigationView? = null

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
    var drawerToggle: ActionBarDrawerToggle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
                textview_titik.visibility = View.INVISIBLE
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
                startActivity(Intent(this, MapsAdminActivity::class.java))
            }
        }

        fab2_share!!.setOnClickListener {
            mMap?.run {
                val intent = AddNewMapActivity.newIntent(this@MapsAdminActivity, cameraPosition.target, cameraPosition.zoom)
                startActivityForResult(intent, NEW_REMINDER_REQUEST_CODE)
            }
        }

        fab3_titik!!.setOnClickListener {
            mMap?.run {
                val intent = AddNewPointActivity.newIntent(this@MapsAdminActivity, cameraPosition.target, cameraPosition.zoom)
                startActivityForResult(intent, NEW_REMINDER_REQUEST_CODE)
            }
        }

        fab4_user!!.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }


        setSupportActionBar(toolbarAdmin)
        val actionBar = supportActionBar
        // Set toolbar title/app title
        actionBar?.title = "Admin"
        actionBar?.elevation = 4.0F
        actionBar?.setDisplayHomeAsUpEnabled(true);
        initDrawer()
        nav_view.setNavigationItemSelectedListener(this)

    }

    override fun onLocationChanged(location: Location?) {
        mLastLocation = location
        displayLocation()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        Toast.makeText(this, "GPS NOT DETECTED", Toast.LENGTH_SHORT).show()
    }

    private fun initDrawer() {
        val drawerToggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbarAdmin,
                R.string.drawer_open,
                R.string.drawer_close
        )
        drawerLayout?.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    /*   override fun onCreateOptionsMenu(menu: Menu): Boolean {
           // Inflate the menu to use in the action bar
           val inflater = menuInflater
           inflater.inflate(R.menu.menu_main, menu)
           return super.onCreateOptionsMenu(menu)
       }*/

    /* override fun onOptionsItemSelected(item: MenuItem): Boolean {
         // Handle presses on the action bar menu items
         when (item.itemId) {
             R.id.nav_user -> {
                 val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                 startActivity(intent)
                 return true
             }

         }
         return super.onOptionsItemSelected(item)
     }
 */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.getItemId()

        if (id == R.id.nav_user) {
            startActivity(Intent(this, UserActivity::class.java))

        }
        if (id == R.id.nav_list) {
            startActivity(Intent(this, ListDataAreaActivity::class.java))
        }

        item.setChecked(true)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        initDrawer()
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        drawerToggle?.syncState()
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapAdmin) as SupportMapFragment?
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
    /* private fun displayLocation() {
         Log.d("Cek lokasi", "cekLokasi")
         if (mMap != null) {
             if (checkPermission()) {
                 val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                 try {
                     mFusedLocationClient.lastLocation.addOnSuccessListener(object : OnSuccessListener<Location> {
                         override fun onSuccess(location: Location) {
                             markerLocation(LatLng(location.latitude, location.longitude))
                         }
                     })
                 } catch (e: NullPointerException) {
                     Toast.makeText(this, "GPS NOT DETECTED", Toast.LENGTH_SHORT).show()
                 } catch (e: Exception) {
                     Toast.makeText(this, "GPS NOT DETECTED", Toast.LENGTH_SHORT).show()
                 }
                 *//*  mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
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
             *//*

            } else {
                askPermission()
            }
        }

    }*/

    private fun displayLocation() {
        Log.d("LOG Cek lokasi", "cek lokasi")

        if (mMap != null) {
            if (checkPermission()) {
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_FINE
                val bestProvider = locationManager.getBestProvider(Criteria(), false)
                val location = locationManager.getLastKnownLocation(bestProvider)
                //   val location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    markerLocation(latLng)
                    Log.d("testLocation= ", "lat = " + location.latitude + "long = " + location.longitude)
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } else {
                    Log.w("LOG FAILED", "No locationretrieved yet")
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
        } catch (e: NullPointerException) {
            Log.d("CLOG", "error = " + e.localizedMessage)
        }
        val markerOptions = MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("G:" + 0 + " Lokasi Saya")
                .snippet(title)
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
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder(this)
                    .setMessage("GPS TIDAK AKTIF")
                    .setPositiveButton("Aktifkan") { dialogInterface, i ->
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setCancelable(false)
                    .show()
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            reloadMapMarkers()
        }
        initDrawer()
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
        //   startLocationUpdates()
        //recoverlocationMarker();
    }

    private fun startLocationUpdates() {
        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (checkPermission())
        //     LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            mFusedLocationClient.lastLocation.addOnSuccessListener(object : OnSuccessListener<Location> {
                override fun onSuccess(location: Location) {
                    markerLocation(LatLng(location.latitude, location.longitude))
                }
            })
    }


    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("ONConnection failed", "failed status code = " + connectionResult.errorMessage!!)
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
                     Toast.makeText(this@MapsAdminActivity, "Geofence Added!", Toast.LENGTH_SHORT).show()
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

    private fun addMarkerPoint(latLng: LatLng, message: String, number: String) {
        mMap!!.addMarker(MarkerOptions()
                .title("G:$number Nama Area Evakuasi = $message")
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng))
    }


    //info from marker
    override fun onInfoWindowClick(marker: Marker) {
        val requestId = marker.title.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
        val substring = requestId.split(" ")

        Log.d("CLogSubs", "data= " + substring[0])
        Log.d("CLOGrequestId = ", "id=" + requestId)
        if (!mGoogleApiClient!!.isConnected) {
            Toast.makeText(this, "GeoFence Not connected!", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val idList = ArrayList<String>()
            idList.add(substring[0])
            Log.d("idlist = ", idList.toString())
            deleteData(substring[0])
            val geoClient = LocationServices.getGeofencingClient(this)
            geoClient.removeGeofences(idList).addOnSuccessListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    reloadMapMarkers()
                    Toast.makeText(this@MapsAdminActivity, "Success remove geofence!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MapsAdminActivity::class.java))
                }
            }.addOnFailureListener {
                Log.e("LOG ERROR WINDOW CLICK", it.localizedMessage)
                Toast.makeText(this@MapsAdminActivity, "Error when remove geofence!", Toast.LENGTH_SHORT).show()

            }
            /*     LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idList).setResultCallback { status ->
                     if (status.isSuccess) {
                         //remove from db
                         startActivity(Intent(this, MapsAdminActivity::class.java))
                         Log.d("CLOGREMOVE", "sukses key = $requestId")
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                             reloadMapMarkers()
                         }
                     } else {
                         Log.e("CLOGREMOVE", "ERROR WINDOW CLICK")
                     }
                 }*/

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
        api.allData().enqueue(object : Callback<com.example.systemperingatan.API.Pojo.Response> {
            override fun onResponse(call: Call<com.example.systemperingatan.API.Pojo.Response>, response: Response<com.example.systemperingatan.API.Pojo.Response>) {
                val data = response.body()

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null && data.data.get(i)?.type == "circle") {
                        val number = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        expires = java.lang.Long.parseLong(data.data.get(i)?.expires)
                        radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        message = data.data.get(i)?.message.toString()
                        Toast.makeText(this@MapsAdminActivity, response.message(), Toast.LENGTH_SHORT).show()
                        addMarker(message, radiusMeter, number!!, latitude, longitude)

                    } else if (data.data != null && data.data.get(i)?.type == "point") {
                        val numberPoint = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        message = data.data.get(i)?.message.toString()
                        addMarkerPoint(LatLng(latitude, longitude), message, numberPoint!!)

                    } else {
                        Toast.makeText(this@MapsAdminActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<com.example.systemperingatan.API.Pojo.Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@MapsAdminActivity, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteData(number: String) {
        val tag_string_req = "req_postdata"
        Log.d("CLOG", "deleteId:$number")
        val strReq = object : StringRequest(Method.DELETE,
                NetworkAPI.delete + "/$number", { response ->
            Log.d("CLOG", "responh: $response")
            try {
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("status post  = ", status1)
                if (status1.contains("200")) {
                    //          Toast.makeText(this, "id = $number Geofence Remove!", Toast.LENGTH_SHORT).show()
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
                //      params["distance"] = distance.toString()
                return params
            }

        }

        App.instance?.addToRequestQueue(strReq, tag_string_req)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            reloadMapMarkers()
        }
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