package com.example.systemperingatan.User.UI

import android.Manifest
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
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.Admin.Adapter.ListDataEvacuationZoneAdapter
import com.example.systemperingatan.Admin.UI.Activity.ListDataAreaActivity
import com.example.systemperingatan.Admin.UI.Activity.MapsAdminActivity
import com.example.systemperingatan.App
import com.example.systemperingatan.App.Companion.api
import com.example.systemperingatan.LoginRegister.Login
import com.example.systemperingatan.R
import com.example.systemperingatan.User.Helper.GoogleMapDTO
import com.example.systemperingatan.User.Notification.GeofenceBroadcastReceiver
import com.example.systemperingatan.User.SQLite.GeofenceContract
import com.example.systemperingatan.User.SQLite.GeofenceDbHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_user.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class UserActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, NavigationView.OnNavigationItemSelectedListener {
    private var location: Location? = null
    private val arrayListZona = ArrayList<DataItem>()
    val adapterZona = ListDataEvacuationZoneAdapter(arrayListZona, this::onClick)
    var geofencingClient: GeofencingClient? = null
    private lateinit var locationManager: LocationManager
    private lateinit var titikGps: LatLng

    companion object {
        var user = "user"
        fun getAll(): List<DataItem> {
            if (preferences!!.contains(MAPS)) {
                val remindersString = preferences!!.getString(MAPS, null)
                val arrayOfReminders = gson.fromJson(remindersString, Array<DataItem>::class.java)
                if (arrayOfReminders != null) {
                    return arrayOfReminders.toList()
                }

            } else {
                Log.d("cekError", "Error")
            }
            return listOf()
        }

        fun get(requestId: String?) = getAll().firstOrNull {
            it.number == requestId
        }

        private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
            Log.d("CREATE GEO REQUEST", "createGeofenceRequest")
            return GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER /*or GeofencingRequest.INITIAL_TRIGGER_DWELL*/)
                    .addGeofence(geofence)
                    .build()
        }

        private fun saveAll(list: ArrayList<DataItem>) {
            preferences?.edit()?.clear()?.apply()
            preferences!!.edit().putString(MAPS, gson.toJson(list)).apply()
        }

        var message: String? = null
        var address: String? = null
        private var mMap: GoogleMap? = null
        //sharedpref
        private var preferences: SharedPreferences? = null

        private var mpendingIntent: PendingIntent? = null
        private var locationMarker: Marker? = null
        //spinner
        private val gson = Gson()
        internal var number: String? = null
        internal var latitude: Double = 0.toDouble()
        internal var radiusMeter: Double = 0.toDouble()
        internal var longitude: Double = 0.toDouble()
        internal var expires: Long = 0
        private val PREFS_NAME = "pref2"
        private val MAPS = "mapsv4"

        private val MY_PERMISSION_REQUEST_CODE = 7192

        private val PLAY_SERVICE_RESOLUTION_REQUEST = 300193

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        geofencingClient = LocationServices.getGeofencingClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        initMap()
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        mpendingIntent = null
        setUpLocation()
        Log.d("SharedPref = ", getAll().toString())
        setSupportActionBar(toolbarUser)
        val actionBar = supportActionBar
        actionBar?.title = "User"
        actionBar?.elevation = 4.0F
        actionBar?.setDisplayHomeAsUpEnabled(true);
        initDrawer()
        nav_viewuser.setNavigationItemSelectedListener(this)
        Log.d("dataUSERHPUser = ", "login = " + App.preferenceHelper.is_login + "" +
                " nama = " + App.preferenceHelper.nama +
                " tipe = " + App.preferenceHelper.tipe +
                " hp = " + App.preferenceHelper.hp +
                " password = " + App.preferenceHelper.password)
        checkUser()


    }

    private fun checkUser() {
        Log.d("dataUSERHP = ", App.preferenceHelper.is_login)
        if (App.preferenceHelper.is_login == "") {
            AlertDialog.Builder(this)
                    .setMessage("Anda harus login")
                    .setPositiveButton("Login") { dialogInterface, i ->
                        startActivity(Intent(this, Login::class.java))
                    }
                    .setCancelable(false)
                    .show()
        }
    }

    private fun initDrawer() {
        val drawerToggle = ActionBarDrawerToggle(
                this,
                drawerLayoutUser,
                toolbarUser,
                R.string.drawer_open,
                R.string.drawer_close
        )
        drawerLayout?.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_viewuser)
        val headerView: View = navigationView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById(R.id.nama)
        navUsername.text = "Selamat Datang, " + App.preferenceHelper.nama

        if (App.preferenceHelper.tipe != "admin") {
            val nav_Menu: Menu = navigationView.getMenu()
            nav_Menu.findItem(R.id.nav_admin).setVisible(false)

            nav_Menu.findItem(R.id.nav_user).setVisible(true)
        } else {
            val nav_Menu: Menu = navigationView.getMenu()
            nav_Menu.findItem(R.id.nav_admin).setVisible(true)

            nav_Menu.findItem(R.id.nav_user).setVisible(false)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.getItemId()

        if (id == R.id.nav_admin) {
            startActivity(Intent(this, MapsAdminActivity::class.java))
        }

        if (id == R.id.nav_user) {
            startActivity(Intent(this, UserActivity::class.java))
        }

        if (id == R.id.editUser) {
            startActivity(Intent(this, EditUser::class.java))
        }
        if (id == R.id.logout) {
            logout()
            startActivity(Intent(this, Login::class.java))
        }

        item.setChecked(true)
        drawerLayoutUser.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {

        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.logout, { response ->
            Log.d("CLOG", "responh: $response")
            try {

                val jObj = JSONObject(response)
                val status = jObj.getString("status")
                Log.d("dataStatus = ", status.toString())
                val data = jObj.get("data")
                Log.d("dataUSER = ", data.toString())
                if (status.contains("200")) {
                    Toast.makeText(this, "Logout Success!", Toast.LENGTH_SHORT).show()

                } else {
                    val msg = jObj.getString("message")
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("errorcatch = ", e.toString())
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
                    Toast.makeText(applicationContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                Log.d("dataDiri = ", "hp = " + App.preferenceHelper.hp + " password = " + App.preferenceHelper.password)
                params["hp"] = App.preferenceHelper.hp
                params["password"] = App.preferenceHelper.password

                return params
            }
        }

        // Adding request to request queue
        App.instance?.addToRequestQueue(strReq, tag_string_req)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        initDrawer()
    }


    private fun percobaan() {
        val geofence = Geofence.Builder()
                .setRequestId(857.toString())
                .setCircularRegion(
                        -7.690715292002636,
                        110.42399603873491,
                        1000.0.toFloat()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        try {

            geofencingClient?.addGeofences(
                    createGeofenceRequest(geofence),
                    createGeofencePendingIntent()
            )?.run {
                addOnSuccessListener {
                    Log.d("CLOGsukses = ", "sukses = $it")
                }
                addOnFailureListener {
                    Log.d("CLOGerror = ", it.localizedMessage)
                }
            }
            //  saveAll(response.body()!!.data)

        } catch (securityException: SecurityException) {
            logSecurityException(securityException)
        } catch (e: SQLException) {
            e.stackTrace
        }

        val arrayList = ArrayList<DataItem>()
        val dataFav1 = DataItem(857.toString(), null, null, "-7.689958402400068",
                null, "KOSKOSAN", null, 110.41690729558468.toString(), null, null, "Percobaan")
        arrayList.addAll(listOf(dataFav1))
        saveAll(arrayList)
        addMarker("KOSOSAN", 1000.0, 857.toString(), -7.690715292002636, 110.42399603873491)
    }


    private fun initRecyclerView() {
        recyclerviewzona.run {
            layoutManager = LinearLayoutManager(this@UserActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterZona
        }
    }

    private fun initMap() {
        val mapFragment = (supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //request runtime permission
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSION_REQUEST_CODE)

        } else {
            if (checkPlayServices()) {
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
        Toast.makeText(this, "permisson Denied", Toast.LENGTH_SHORT).show()
    }


    private fun askPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_CODE)
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


    override fun onStart() {
        super.onStart()
        setUpLocation()
    }

    //add gps location now
    private fun displayLocation() {
        Log.d("LOG Cek lokasi", "cek lokasi")
        if (mMap != null) {
            if (checkPermission()) {
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_FINE

                val bestProvider = locationManager.getBestProvider(Criteria(), false)
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.d("CEKLOKASI", "GPS")
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    Log.d("CEKLOKASI", "NETWORK")
                }
                //   val location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                if (location != null) {
                    val latLng = LatLng(location!!.latitude, location!!.longitude)
                    markerLocation(latLng)
                    Log.d("testLocation= ", "lat = " + location!!.latitude + "long = " + location!!.longitude)
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
        Log.i("LOG TLOCATION", "markerLocation($latLng)")
        val title = latLng.latitude.toString() + ", " + latLng.longitude
        val lat = latLng.latitude
        val long = latLng.longitude
        val latLng = "$lat,$long".split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val latitude = java.lang.Double.parseDouble(latLng[0])
        val longitude = java.lang.Double.parseDouble(latLng[1])
        val location = LatLng(latitude, longitude)
        titikGps = location
        Log.d("titikGps = ", titikGps.toString())
        val markerOptions = MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("lokasi saya")
        if (mMap != null) {
            if (locationMarker != null)
                locationMarker!!.remove()
            locationMarker = mMap?.addMarker(markerOptions)
            val zoom = 14f
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, zoom)
            mMap!!.animateCamera(cameraUpdate)
        }
    }

    private fun createGeofencePendingIntent(): PendingIntent {
        if (mpendingIntent != null) {
            Log.d("LOGCREATEPENDING ", "pending isi")
            return mpendingIntent as PendingIntent
        } else {
            Log.d("LOGCREATEPENDING gagal", "pending null")
        }
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)

        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun reloadMapMarkersZonaWithoutRecyclerView() {
        api.allData().enqueue(object : Callback<com.example.systemperingatan.API.Pojo.Response> {
            override fun onResponse(call: Call<com.example.systemperingatan.API.Pojo.Response>, response: Response<com.example.systemperingatan.API.Pojo.Response>) {
                val data = response.body()
                Log.d("dataAPi = ", data.toString())

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val number = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        expires = java.lang.Long.parseLong(data.data.get(i)?.expires)
                        radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        message = data.data.get(i)?.message.toString()
                        address = data.data.get(i)?.address.toString()
                        val type = data.data.get(i)?.type

                        if (data.data.get(i)?.type == "point") {
                            val radiusFloat = radiusMeter.toFloat()
                            Log.d("CLOG = ", "radiusFloat = " + radiusFloat.toString())
                            val lat = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                            val lang = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                            val latlang = LatLng(lat, lang)

                            if (this@UserActivity::titikGps.isInitialized) {
                                val URL = getDirectionURL(titikGps, latlang)
                                GetDirection(URL).execute()
                            } else {
                                Toast.makeText(this@UserActivity, "TITIK GPS TIDAK TERDITEKSI ", Toast.LENGTH_SHORT).show()
                            }
                        }


                    } else {
                        Toast.makeText(this@UserActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<com.example.systemperingatan.API.Pojo.Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@UserActivity, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("CEKCLOGProvider", "ProviderGPS is not avaible");
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.v("CEKCLOGProvider", " ProviderGPS is avaible");
        }
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.e("CEKCLOGNetwork Provider", "ProviderNetwork is not avaible");
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.v("CEKCLOGNetwork Provider", "providerNetwork is avaible");
        }
        setUpLocation()
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder(this)
                    .setMessage("GPS TIDAK AKTIF")
                    .setPositiveButton("Aktifkan") { dialogInterface, i ->
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setCancelable(false)
                    .show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (App.preferenceHelper.is_login == "1") {
                reloadMapMarkers()
                reloadMapMarkersZonaWithoutRecyclerView()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("LOG onMapReady", "onMapReady()")
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mMap!!.isMyLocationEnabled = true
        mMap!!.setOnMapClickListener(this)
        mMap!!.setOnMarkerClickListener(this)
        mMap!!.setOnInfoWindowClickListener(this)

        displayLocation()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            reloadMapMarkersZona()
        }
        //    percobaan()
    }

    //when click the map
    override fun onMapClick(latLng: LatLng) {
        Log.d("LOG", "onMapClick($latLng)")
    }

    private fun logSecurityException(securityException: SecurityException) {
        Log.e("LOG ERROR PERMISSION", "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException)
    }

    private fun addMarker(message: String, radius: Double, key: String, latitude: Double, longitude: Double) {
        val latLng = "$latitude,$longitude".split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val latitude = java.lang.Double.parseDouble(latLng[0])
        val longitude = java.lang.Double.parseDouble(latLng[1])
        val location = LatLng(latitude, longitude)
        val strokeColor = 0xffff0000.toInt(); //red outline
        val shadeColor = 0x44ff0000; //opaque red fill
        mMap!!.addMarker(MarkerOptions()
                .title("G:$key pesan =  $message")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(location))
        mMap!!.addCircle(CircleOptions()
                .center(location)
                .radius(radius)
                .fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(2F))
    }

    private fun addMarkerPoint(latLng: LatLng, message: String, number: String) {
        mMap!!.addMarker(MarkerOptions()
                .title("G:$number area = $message")
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(latLng))
    }

    private fun onClick(dataItem: DataItem) {
        if (mMap != null) {
            // Remove the anterior marker
            if (locationMarker != null) {
                //    dataItem.number = locationMarker!!.title.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                val gmmIntentUri = Uri.parse("google.navigation:q=" + dataItem.latitude + "," + dataItem.longitude + "&mode=d")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        val requestId = marker.title.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val substring = requestId.split(" ")
        try {
            val idList = ArrayList<String>()
            idList.add(substring[0])

            geofencingClient?.removeGeofences(idList)?.addOnSuccessListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    reloadMapMarkers()
                }
            }?.addOnFailureListener {
                Log.e("LOG ERROR WINDOW CLICK", it.localizedMessage)
                Toast.makeText(this@UserActivity, "Error when remove geofence!", Toast.LENGTH_SHORT).show()
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
        //   mMap!!.clear()
        val dbHelper = GeofenceDbHelper(this)
        dbHelper.DeleteAll()
        api.allData().enqueue(object : Callback<com.example.systemperingatan.API.Pojo.Response> {
            override fun onResponse(call: Call<com.example.systemperingatan.API.Pojo.Response>, response: Response<com.example.systemperingatan.API.Pojo.Response>) {
                val data = response.body()
                Log.d("dataAPi = ", data.toString())

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val number = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        expires = java.lang.Long.parseLong(data.data.get(i)?.expires)
                        radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        message = data.data.get(i)?.message.toString()
                        val type = data.data.get(i)?.type

                        if (data.data.get(i)?.type == "circle") {
                            addMarker(message!!, radiusMeter, number!!, latitude, longitude)
                        } else {
                            addMarkerPoint(LatLng(latitude, longitude), message!!, number!!)
                        }

                        val radiusFloat = radiusMeter.toFloat()
                        Log.d("CLOG = ", "radiusFloat = " + radiusFloat.toString())
                        val lat = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        val lang = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        val latlang = LatLng(lat, lang)

                        Log.d("CLOG = ", "data latLang array ke $i = " + latlang.toString())

                        if (this@UserActivity::titikGps.isInitialized) {
                            val distance = SphericalUtil.computeDistanceBetween(titikGps, latlang)

                            //add route direction

                            //    val URL = getDirectionURL(titikGps, latlang)
                            //     Log.d("GoogleMap1", "URL : $URL")


                            Log.d("CLOG = ", "distance = " + distance.toString())
                            val helper = GeofenceDbHelper(this@UserActivity)
                            Log.d("CLOGlat", latitude.toString())

                            helper.saveToDb(number, latitude, longitude, expires, message!!, distance, type)

                            // updateData(number, distance)
                        } else {
                            Toast.makeText(this@UserActivity, "TITIK GPS TIDAK TERDITEKSI ", Toast.LENGTH_SHORT).show()
                        }

                        GetDataSQLite()

                        val idList = ArrayList<String>()
                        idList.add(number)
                        Log.d("idlist = ", idList.toString())

                        val geoClient = LocationServices.getGeofencingClient(this@UserActivity)
                        geoClient.removeGeofences(idList).addOnSuccessListener {

                        }.addOnFailureListener {
                            Log.e("LOGERROR WINDOW CLICK", it.localizedMessage)
                            Toast.makeText(this@UserActivity, "Error when remove geofence!", Toast.LENGTH_SHORT).show()

                        }

                        val geofence = Geofence.Builder()
                                .setRequestId(number)
                                .setCircularRegion(
                                        latitude,
                                        longitude,
                                        radiusFloat
                                )
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(/*Geofence.GEOFENCE_TRANSITION_DWELL or*/ Geofence.GEOFENCE_TRANSITION_EXIT or
                                        Geofence.GEOFENCE_TRANSITION_ENTER)
                                /*.setLoiteringDelay(10000)*/
                                .setNotificationResponsiveness(0)
                                .build()

                        val geofencingClient = LocationServices.getGeofencingClient(this@UserActivity)
                        geofencingClient.addGeofences(
                                createGeofenceRequest(geofence),
                                createGeofencePendingIntent()
                        ).run {
                            addOnSuccessListener {
                                Log.d("CLOGsukses = ", "sukses = $it")
                                Toast.makeText(this@UserActivity, "Geofences set up", Toast.LENGTH_SHORT).show();

                            }
                            addOnFailureListener {
                                Log.d("CLOGerror = ", it.localizedMessage)
                            }
                        }
                        //  saveAll(response.body()!!.data)

                    } else {
                        Toast.makeText(this@UserActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d("test data", "latitude =" + latitude + "longitude =" + longitude + "expires =" + expires)
            }

            override fun onFailure(call: Call<com.example.systemperingatan.API.Pojo.Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@UserActivity, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun reloadMapMarkersZona() {
        //   mMap!!.clear()
        val dbHelper = GeofenceDbHelper(this)
        dbHelper.DeleteAll()
        api.allData().enqueue(object : Callback<com.example.systemperingatan.API.Pojo.Response> {
            override fun onResponse(call: Call<com.example.systemperingatan.API.Pojo.Response>, response: Response<com.example.systemperingatan.API.Pojo.Response>) {
                val data = response.body()
                Log.d("dataAPi = ", data.toString())

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val number = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        expires = java.lang.Long.parseLong(data.data.get(i)?.expires)
                        radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        message = data.data.get(i)?.message.toString()
                        address = data.data.get(i)?.address.toString()
                        val type = data.data.get(i)?.type

                        if (data.data.get(i)?.type == "point") {
                            val radiusFloat = radiusMeter.toFloat()
                            Log.d("CLOG = ", "radiusFloat = " + radiusFloat.toString())
                            val lat = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                            val lang = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                            val latlang = LatLng(lat, lang)

                            if (this@UserActivity::titikGps.isInitialized) {
                                val distance = SphericalUtil.computeDistanceBetween(titikGps, latlang)

                                val URL = getDirectionURL(titikGps, latlang)
                                Log.d("GoogleMap1", "URL : $URL")
                                GetDirection(URL).execute()

                                Log.d("CLOG = ", "distance = " + distance.toString())

                                Log.d("CLOGaddress", address.toString())

                                val dataZona = DataItem(number, null, null, latitude.toString(), null, message,
                                        type, longitude.toString(), null, address, distance.toString(), null)
                                Log.d("dataZONN = ", dataZona.toString())
                                arrayListZona.addAll(listOf(dataZona))
                                initRecyclerView()
                            } else {
                                Toast.makeText(this@UserActivity, "TITIK GPS TIDAK TERDITEKSI ", Toast.LENGTH_SHORT).show()
                            }
                        }


                    } else {
                        Toast.makeText(this@UserActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<com.example.systemperingatan.API.Pojo.Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@UserActivity, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun GetDataSQLite() {
        val arrayList = ArrayList<DataItem>()
        val dbHelper = GeofenceDbHelper(this)
        dbHelper.getCursor().use { cursor ->
            while (cursor.moveToNext()) {
                val number = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_NUMBERS))
                val latitude = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_LAT))
                val longitude = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_LNG))
                val expires = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_EXPIRES))
                val messages = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_MESSAGE))
                val distances = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_DISTANCE))
                val min_dis = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_MIN_DISTANCE))
                val type = cursor.getString(cursor.getColumnIndex(GeofenceContract.GeofenceEntry.COLUMN_NAME_TYPE))

                val dataFav1 = DataItem(number, null, expires, latitude, null, messages, type, longitude, null, null, distances, min_dis)
                arrayList.addAll(listOf(dataFav1))
            }
        }
        saveAll(arrayList)

        if (arrayList.isEmpty()) {
            Toast.makeText(this, "arraylist is empty", Toast.LENGTH_SHORT).show()
            Log.d("itemTest", "arraylist is empty")
        }
        Log.d(" itemTest = ", "arraylist" + arrayList)
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    fun getDirectionURL(origin: LatLng, dest: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyBcgkU-gP-QU-53LmGoh4TQ87yMDLl2hXc"
    }

    private inner class GetDirection(val url: String) : AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()?.string()
            Log.d("GoogleMap", " data : $data")
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size - 1)) {
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices) {
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap!!.addPolyline(lineoption)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }
}