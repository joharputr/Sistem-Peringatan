package com.example.systemperingatan.Admin.UI.Activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.API.Pojo.Response
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_add_new_map.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.util.*
import kotlin.math.roundToInt

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddAreaActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mGoogleApiClient: GoogleApiClient? = null
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    internal var expires: Long = 0
    lateinit var messages: String
    internal var radiusMeter: Double = 0.toDouble()
    private var mSharedPreferences: SharedPreferences? = null
    private val predictionList: List<AutocompletePrediction>? = null
    private var placesClient: PlacesClient? = null
    private var reminder = DataItem(null, null, null, null, null, null, null, null, null, null)
    val newGeofenceNumber: Int
        get() {
            val number = mSharedPreferences!!.getInt(MapsAdminActivity.NEW_GEOFENCE_NUMBER, 1)
            val editor = mSharedPreferences!!.edit()
            editor.putInt(MapsAdminActivity.NEW_GEOFENCE_NUMBER, number + 1)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                reloadMapMarkers()
            }
        }
    }

    private fun updateRadiusWithProgress(progress: Int) {
        Log.d("IntRadius = ", progress.toString())
        val radius = getRadius(progress)
        reminder.radius = radius.toString()
        radiusDescription.text = getString(R.string.radius_description, radius.roundToInt().toString())
    }

    companion object {
        private var map: GoogleMap? = null
        private var mLocationRequest: LocationRequest? = null
        private val MY_PERMISSION_REQUEST_CODE = 7192
        private var mLastLocation: Location? = null
        private var locationMarker: Marker? = null
        private const val EXTRA_LAT_LNG = "EXTRA_LAT_LNG"
        private const val EXTRA_ZOOM = "EXTRA_ZOOM"
        private val PLAY_SERVICE_RESOLUTION_REQUEST = 300193

        fun newIntent(context: Context, latLng: LatLng, zoom: Float): Intent {
            val intent = Intent(context, AddAreaActivity::class.java)
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
        mSharedPreferences = getSharedPreferences(MapsAdminActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpLocation()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            reloadMapMarkers()
        }

        SearchPlace()
    }

    private fun SearchPlace() {

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCRK_C8YiQf46yeP6Usf-_Cqrg2a5-OMuM")
        }

        placesClient = Places.createClient(this)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

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


    override fun onStop() {
        super.onStop()
        mGoogleApiClient!!.disconnect()
    }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            reloadMapMarkers()
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    reloadMapMarkers()
                }
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
                .title("lokasi saya")
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


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.uiSettings.isMapToolbarEnabled = false
        map!!.isMyLocationEnabled = true
        centerCamera()
        showConfigureLocationStep()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            reloadMapMarkers()
        }
    }


    private fun centerCamera() {
        val latLng = intent.extras.get(EXTRA_LAT_LNG) as LatLng
        val zoom = intent.extras.get(EXTRA_ZOOM) as Float
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    //step 1
    private fun showConfigureLocationStep() {
        layout_panel.visibility = View.VISIBLE
        marker.visibility = View.VISIBLE
        instructionTitle.visibility = View.VISIBLE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.GONE

        instructionTitle.text = getString(R.string.instruction_where_description)
        next.setOnClickListener {

            reminder.latlang = map!!.cameraPosition.target
            reminder.latitude = map!!.cameraPosition.target.latitude.toString()
            reminder.longitude = map!!.cameraPosition.target.longitude.toString()
            Log.d("CLOG", "radiusku = " + reminder.latlang.toString())
            showConfigureRadiusStep()
        }
        //   showReminderUpdate()
    }

    //step 2
    private fun showConfigureRadiusStep() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            reloadMapMarkers()
        }
        layout_panel.visibility = View.GONE
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        radiusBar.visibility = View.VISIBLE
        radiusDescription.visibility = View.VISIBLE
        message.visibility = View.GONE
        instructionTitle.text = getString(R.string.instruction_radius_description)

        radiusBar.setOnSeekBarChangeListener(radiusBarChangeListener)
        updateRadiusWithProgress(radiusBar.progress)

        map!!.animateCamera(CameraUpdateFactory.zoomTo(15f))

        showReminderUpdate()

        next.setOnClickListener {
            showConfigureMessageStep()
        }
    }

    private fun getRadius(progress: Int) = 1500 + (2 * progress.toDouble() + 2) * 100

    //step 3
    private fun showConfigureMessageStep() {
        layout_panel.visibility = View.GONE
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.VISIBLE
        instructionTitle.text = "Tulis nama area"
        next.setOnClickListener {
            hideKeyboard(this, message)
            reminder.message = message.text.toString()
            if (reminder.message.isNullOrEmpty()) {
                message.error = "Nama area wajib diisi"
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
        map?.clear()
        showReminderInMap(this, map!!, reminder)
    }

    //circle at set radius
    fun showReminderInMap(context: Context, map: GoogleMap, reminder: DataItem) {
        if (reminder.latlang != null) {
            Log.d("CLOG", "latlang = " + reminder.latlang)
            val latLng = reminder.latlang as LatLng
            val marker = map.addMarker(MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            // marker.tag = reminder.id
            if (reminder.radius != null) {
                val radius = java.lang.Double.parseDouble(reminder.radius!!)
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

        val expTime = System.currentTimeMillis() + MapsAdminActivity.GEOFENCE_EXPIRATION_IN_MILLISECONDS
        val key = newGeofenceNumber.toString() + ""
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.post, { response ->
            Log.d("CLOG", "responh: $response")
            try {
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("status post  = ", status1)
                if (status1.contains("200")) {
                    Toast.makeText(this, "Sukses Menambahkan Area", Toast.LENGTH_SHORT).show()
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

        // Adding request to request queue
        App.instance?.addToRequestQueue(strReq, tag_string_req)
        Log.d("CLOG", "number = " + key + " lat = " + reminder.latitude.toString() + " long = " +
                reminder.longitude.toString() + " exp = " + expTime + "radius = " + reminder.radius +
                " message = " + reminder.message + " latlang = " + reminder.latlang)

        setResult(Activity.RESULT_OK)
        finish()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    //load from db
    fun reloadMapMarkers() {
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
                        messages = data.data.get(i)?.message.toString()
                        //        Toast.makeText(this@MapsAdminActivity, response.message(), Toast.LENGTH_SHORT).show()
                        addMarker(messages, radiusMeter, number!!, latitude, longitude)

                    } else if (data.data != null && data.data.get(i)?.type == "point") {
                        val numberPoint = data.data.get(i)?.number
                        latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        messages = data.data.get(i)?.message.toString()
                        radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        addMarkerPoint(LatLng(latitude, longitude), messages, radiusMeter, numberPoint!!)

                    } else {
                        Toast.makeText(this@AddAreaActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@AddAreaActivity, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMarker(message: String, radius: Double, key: String, latitude: Double, longitude: Double) {

        val latLng = "$latitude,$longitude".split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val latitude = java.lang.Double.parseDouble(latLng[0])
        val longitude = java.lang.Double.parseDouble(latLng[1])
        val location = LatLng(latitude, longitude)
        map!!.addMarker(MarkerOptions()
                .title("Area :$message")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(location))
        map!!.addCircle(CircleOptions()
                .center(location)
                .radius(radius)
                .strokeColor(R.color.wallet_holo_blue_light)
                .fillColor(Color.parseColor("#80ff0000")))
    }

    private fun addMarkerPoint(latLng: LatLng, message: String, radius: Double, number: String) {
        val strokeColor = 0x0106001b.toInt(); //red outline
        map!!.addMarker(MarkerOptions()
                .title("Zona = $message")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(latLng))
        map!!.addCircle(CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(R.color.wallet_holo_blue_light)
                .fillColor(0xff0009ff.toInt()).strokeColor(strokeColor).strokeWidth(2f))
    }


}
