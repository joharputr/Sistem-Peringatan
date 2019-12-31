package com.example.systemperingatan.Admin.UI.Activity

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.SQLException
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.API.Pojo.Response
import com.example.systemperingatan.App
import com.example.systemperingatan.BuildConfig
import com.example.systemperingatan.R
import com.example.systemperingatan.User.Notification.GeofenceTransitionService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_edit_location_point.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.io.IOException
import java.util.*

class EditZonaActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // private lateinit var map: GoogleMap
    private var mpendingIntent: PendingIntent? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    private var reminder = DataItem()

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
        private var placesClient: PlacesClient? = null

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
        SearchPlace()
    }

    private fun SearchPlace() {

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCRK_C8YiQf46yeP6Usf-_Cqrg2a5-OMuM")
        }

        placesClient = Places.createClient(this)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment_point) as AutocompleteSupportFragment?

        autocompleteFragment?.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.latLng, 14f)
              map!!.animateCamera(cameraUpdate)
                Log.d("PLACESTest", "Place: " + place.getName() + ", " + place.getId() +
                        " Latitude = " + place.latLng?.latitude + " address =" + place.address)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.d("PLACES", "An error occurred: $status")
            }
        })

    }

    private fun AddPointLocation() {
        addMarkerLocationPointEdit.setOnClickListener {
            showConfigureLocationStep()
        }
    }

    override fun onResume() {
        super.onResume()
        reloadMapMarkers()
    }


    private fun finishAddPoint() {
        next_pointEdit.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
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
                        radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        addMarkerPoint(LatLng(latitude, longitude), messages, radiusMeter, numberPoint!!)

                    } else {
                        Toast.makeText(this@EditZonaActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@EditZonaActivity, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
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
                .title("lokasi saya")
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

    private fun addMarker(message: String, radius: Double, key: String, latitude: Double, longitude: Double) {
        val latLng = "$latitude,$longitude".split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val latitude = java.lang.Double.parseDouble(latLng[0])
        val longitude = java.lang.Double.parseDouble(latLng[1])
        val location = LatLng(latitude, longitude)
        map?.addMarker(MarkerOptions()
                .title("Area :$message")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(location))
        map?.addCircle(CircleOptions()
                .center(location)
                .radius(radius)
                .strokeColor(R.color.wallet_holo_blue_light)
                .fillColor(Color.parseColor("#80ff0000")))
    }

    private fun addMarkerPoint(latLng: LatLng, message: String, radius: Double, number: String) {
        val strokeColor = 0x0106001b.toInt(); //red outline
        map?.addMarker(MarkerOptions()
                .title("Zona = $message")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng))
        map?.addCircle(CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(R.color.wallet_holo_blue_light)
                .fillColor(0xff0009ff.toInt()).strokeColor(strokeColor).strokeWidth(2f))
    }

    private fun enableView() {
        addMarkerLocationPointEdit.visibility == View.VISIBLE
        next_pointEdit.visibility = View.GONE
    }

    //step 1
    private fun showConfigureLocationStep() {
        layout_panel_point.visibility =View.GONE
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
            addMarkerPoint(reminder.latlang!!, data.message.toString(), 100.0, key)
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
                                NetworkAPI.edit + "/${data.number}", { response ->
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
                        Toast.makeText(this@EditZonaActivity, "Sukses mengubah posisi zona evakuasi", Toast.LENGTH_SHORT).show()
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
