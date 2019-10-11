package com.example.systemperingatan.User

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.SQLException
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.DataItem
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.App
import com.example.systemperingatan.App.Companion.api
import com.example.systemperingatan.BuildConfig
import com.example.systemperingatan.Notification.GeofenceBroadcastReceiver
import com.example.systemperingatan.Notification.GeofenceTransitionService
import com.example.systemperingatan.R
import com.example.systemperingatan.SQLite.GeofenceContract
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
import com.google.maps.android.SphericalUtil
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
class UserActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status>, GoogleMap.OnInfoWindowClickListener {

    // private val preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    //Retrofit
    private lateinit var titikGps: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        initMap()
        setUpLocation()
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        mpendingIntent = null



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
                .title("lokasi saya = $title")
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

    private fun createGeofencePendingIntent(): PendingIntent {
        Log.d("LOGCREATEPENDING INTENT", "createGeofencePendingIntent")
        if (mpendingIntent != null) {
            Log.d("LOGCREATEPENDING ", "pending isi")
            return mpendingIntent as PendingIntent
        } else {
            Log.d("LOGCREATEPENDING gagal", "pending null")
        }
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        Log.d("LOG Pending test", "pending test")
        val GEOFENCE_REQ_CODE = 0
        return PendingIntent.getBroadcast(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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
        mGoogleApiClient?.connect()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("LOG onMapReady", "onMapReady()")
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        /*
               val location1 = LatLng(13.0356745,77.5881522)
               mMap!!.addMarker(MarkerOptions().position(location1).title("My Location"))
               mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(location1,5f))

               Log.d("GoogleMap", "before location2")
               val location2 = LatLng(9.89,78.11)
               mMap!!.addMarker(MarkerOptions().position(location2).title("Madurai"))

               Log.d("GoogleMap", "before location3")

               val location3 = LatLng(13.029727,77.5933021)
               mMap!!.addMarker(MarkerOptions().position(location3).title("Bangalore"))

               Log.d("GoogleMap", "before URL")
              val URL = getDirectionURL(location2,location3)
               Log.d("GoogleMap", "URL : $URL")
               GetDirection(URL).execute()*/

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
                .title("G:$number area = $message")
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .position(latLng))
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
        //   mMap!!.clear()
        val dbHelper = GeofenceDbHelper(this)
        dbHelper.DeleteAll()
        api.allData().enqueue(object : Callback<com.example.systemperingatan.API.Response> {
            override fun onResponse(call: Call<com.example.systemperingatan.API.Response>, response: Response<com.example.systemperingatan.API.Response>) {
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

                        if (data.data.get(i)?.type == "circle")
                            addMarker(message, radiusMeter, number!!, latitude, longitude)
                        else
                            addMarkerPoint(LatLng(latitude, longitude), message, number!!)

                        val radiusFloat = radiusMeter.toFloat();
                        Log.d("CLOG = ", "radiusFloat = " + radiusFloat.toString())
                        val lat = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        val lang = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        val latlang = LatLng(lat, lang)

                        Log.d("CLOG = ", "data latLang array ke $i = " + latlang.toString())

                        if (this@UserActivity::titikGps.isInitialized) {
                            val distance = SphericalUtil.computeDistanceBetween(titikGps, latlang)

                            //add route direction
                            val URL = getDirectionURL(titikGps,latlang)
                            Log.d("GoogleMap1", "URL : $URL")

                            GetDirection(URL).execute()
                            Log.d("CLOG = ", "distance = " + distance.toString())
                            val helper = GeofenceDbHelper(this@UserActivity)
                            Log.d("CLOGlat", latitude.toString())

                            helper.saveToDb(number, latitude, longitude, expires, message, distance, type)
                            // updateData(number, distance)
                        }else{
                            Log.d("CLOG","titik gps tidak ada")
                        }
                        GetDataSQLite()
                        val geofence = Geofence.Builder()
                                .setRequestId(number)
                                .setCircularRegion(
                                        latitude,
                                        longitude,
                                        radiusFloat
                                )
                                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build()
                        try {
                            LocationServices.GeofencingApi.addGeofences(
                                    mGoogleApiClient,
                                    createGeofenceRequest(geofence),
                                    createGeofencePendingIntent()
                            )

                            //  saveAll(response.body()!!.data)

                        } catch (securityException: SecurityException) {
                            logSecurityException(securityException)
                        } catch (e: SQLException) {
                            e.stackTrace
                        }

                    } else {
                        Toast.makeText(this@UserActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d("test data", "latitude =" + latitude + "longitude =" + longitude + "expires =" + expires)
            }

            override fun onFailure(call: Call<com.example.systemperingatan.API.Response>, t: Throwable) {
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

                val dataFav1 = DataItem(number, null, expires, latitude, null, messages, type, longitude, null, distances, min_dis)
                arrayList.addAll(listOf(dataFav1))
                Log.d(" itemTest = ", "arraylist" + type)
            }
        }
        saveAll(arrayList)
        Log.d(" itemTest = ", "arraylist" + arrayList)
    }

    private fun updateData(number: String, distance: Double) {
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.edit + "/$number", { response ->
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
                params["distance"] = distance.toString()
                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        return false
    }


    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving"
    }

    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap", " data : $data")
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size - 1)) {
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//                    path.add(startLatLng)
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
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
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap!!.addPolyline(lineoption)
        }
    }

    public fun decodePolyline(encoded: String): List<LatLng> {

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

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }




    companion object {

        fun getAll(): List<DataItem> {
            if (preferences!!.contains(MAPS)) {
                val remindersString = preferences!!.getString(MAPS, null)
                val arrayOfReminders = gson.fromJson(remindersString, Array<DataItem>::class.java)
                if (arrayOfReminders != null) {
                    return arrayOfReminders.toList()
                }
                Log.d("shareppref= ", remindersString)
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
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()
        }

        private fun saveAll(list: ArrayList<DataItem>) {
            preferences?.edit()?.clear()?.apply()
            preferences!!.edit().putString(MAPS, gson.toJson(list)).apply()

        }

        lateinit var message: String
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
        internal var radiusMeter: Double = 0.toDouble()
        internal var longitude: Double = 0.toDouble()
        internal var expires: Long = 0
        private val PREFS_NAME = "pref2"
        private val MAPS = "mapsv4"
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