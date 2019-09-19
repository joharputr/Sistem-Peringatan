package com.example.systemperingatan;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.systemperingatan.API.Api;
import com.example.systemperingatan.API.Data;
import com.example.systemperingatan.API.NetworkConfig;
import com.example.systemperingatan.SQLite.GeofenceDbHelper;
import com.example.systemperingatan.SQLite.GeofenceStorage;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMapClickListener
        , GoogleMap.OnMarkerClickListener, ResultCallback<Status>, GoogleMap.OnInfoWindowClickListener {
    public static final String NEW_GEOFENCE_NUMBER = BuildConfig.APPLICATION_ID + ".NEW_GEOFENCE_NUMBER";
    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final int PLAY_SERVICE_RESOLUTION_REQUEST = 300193;
    private static final float GEOFENCE_RADIUS = 1000.0f; // in meters
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    public static final float GEOFENCE_RADIUS_IN_METERS = 100; // 100 m
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 1000;
    //sharedpref
    private SharedPreferences mSharedPreferences;
    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";
    public static final String SHARED_PREFERENCES_NAME = BuildConfig.APPLICATION_ID + ".SHARED_PREFERENCES_NAME";

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private PendingIntent mpendingIntent;
    DatabaseReference ref;
    GeoFire geofire;
    private Marker locationMarker;
    GeofenceDbHelper geofenceDbHelper = null;
    VerticalSeekBar mSeekbar;
    //spinner
    private Spinner spName;
    String number;
    double latitude;
    double longitude;
    long expires;
    String area;
    LinearLayout linearLayout;
    SeekBar radius;
    TextView textRadius, textMeter;
    FloatingActionButton add;
    //Retrofit
    Api api = NetworkConfig.getClient().create(Api.class);
    private FloatingActionButton fab_main, fab1_mail, fab2_share;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;
    TextView textview_mail, textview_share;
    Boolean isOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initMap();
        setupFirebase();
        setUpLocation();
        seekbarFunction();
        //  clickStart();
        //  clickRemove();
        linearLayout = (LinearLayout) findViewById(R.id.collapse);
        radius = (SeekBar) findViewById(R.id.seekbar);

        spName = (Spinner) findViewById(R.id.spinner);
        textRadius = (TextView) findViewById(R.id.radius);
        textMeter = (TextView) findViewById(R.id.meter);
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mpendingIntent = null;


        fab_main = findViewById(R.id.fab);
        fab1_mail = findViewById(R.id.fab1);
        fab2_share = findViewById(R.id.fab2);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_fab_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);

        textview_mail = (TextView) findViewById(R.id.textview_mail);
        textview_share = (TextView) findViewById(R.id.textview_share);

        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isOpen) {
                    textview_mail.setVisibility(View.INVISIBLE);
                    textview_share.setVisibility(View.INVISIBLE);
                    fab2_share.startAnimation(fab_close);
                    fab1_mail.startAnimation(fab_close);
                    fab_main.startAnimation(fab_anticlock);
                    fab2_share.setClickable(false);
                    fab1_mail.setClickable(false);
                    isOpen = false;
                } else {
                    textview_mail.setVisibility(View.VISIBLE);
                    textview_share.setVisibility(View.VISIBLE);
                    fab2_share.startAnimation(fab_open);
                    fab1_mail.startAnimation(fab_open);
                    fab_main.startAnimation(fab_clock);
                    fab2_share.setClickable(true);
                    fab1_mail.setClickable(true);
                    isOpen = true;
                }

            }
        });


        fab2_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Share", Toast.LENGTH_SHORT).show();

            }
        });

        fab1_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Email", Toast.LENGTH_SHORT).show();

            }
        });

    }



    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpLocation();
            } else {
                permissionDenied();
            }

        }
    }

    private void permissionDenied() {
        Log.d("TEST", "permission denied");
    }

    private boolean checkPermission() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, MY_PERMISSION_REQUEST_CODE);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        int UPDATE_INTERVAL = 5000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        int FATEST_INTERVAL = 3000;
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        int DISPLACEMENT = 10;
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode))
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, PLAY_SERVICE_RESOLUTION_REQUEST).show();
            else {
                Toast.makeText(this, "This device not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
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
    private void removeGeofence() {
        Log.d("", "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d("", "removeGeofenceDraw()");
        if (locationMarker != null) {
            locationMarker.remove();
        }
        if (null != geoFenceLimits) {
            geoFenceLimits.remove();
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
    private void displayLocation() {
        Log.d("Cek lokasi", "cek lokasi");
        if (mMap != null) {
            if (checkPermission()) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    Log.i("LAST GPS LOCATION", "LasKnown location. " +
                            "Long: " + mLastLocation.getLongitude() +
                            " | Lat: " + mLastLocation.getLatitude());
                    //add gps location
                    markerLocation(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                } else {
                    Log.w("FAILED", "No location retrieved yet");
//                startLocationUpdates();
                }
            } else {
                askPermission();
            }
        }
    }

    //move camera to current gps location
    private void markerLocation(LatLng latLng) {
        Log.i("MARKER CURRENT LOCATION", "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (mMap != null) {
            // Remove the anterior marker
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = mMap.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mMap.animateCamera(cameraUpdate);
        }
    }

    private void startGeofence() {
        Log.i("START GEOFENCE", "startGeofence()");
        if (locationMarker != null) {
            Geofence geofence = createGeofence(locationMarker.getPosition());
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e("GEOFENCE MARKER NULL", "Geofence marker is null");
        }
    }

    private Geofence createGeofence(LatLng latLng) {
        Log.d("CREATE GEOFENCE", "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        MapsActivity.GEOFENCE_RADIUS)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d("CREATE GEO REQUEST", "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d("", "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    private PendingIntent createGeofencePendingIntent() {
        Log.d("CREATE PENDING INTENT", "createGeofencePendingIntent");
        if (mpendingIntent != null) {
            Log.d("Pending gagal", "pending ggagal");
            return mpendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        Log.d("Pending test", "pending test");
        int GEOFENCE_REQ_CODE = 0;
        return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

  /*  private void setupSpinner( ) {

        ArrayAdapter<CharSequence> charSequenceArrayAdapter = ArrayAdapter.createFromResource(this, R.array.spinner_method_geofence, R.layout.support_simple_spinner_dropdown_item);
        charSequenceArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spName.setAdapter(charSequenceArrayAdapter);
        spName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                if (position == 0 ) {
                    Toast.makeText(MapsActivity.this, text, Toast.LENGTH_SHORT).show();
                } else if (position == 1) {
                    Toast.makeText(MapsActivity.this, text, Toast.LENGTH_SHORT).show();
                } else if (position == 2) {
                    Toast.makeText(MapsActivity.this, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }*/

    private void setupFirebase() {
        String array[] = {"", ""};
        ref = FirebaseDatabase.getInstance().getReference("MyLocation");
        geofire = new GeoFire(ref);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "onMapReady()");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //add gps logo
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            reloadMapMarkers();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
        //recoverlocationMarker();
    }

    private void startLocationUpdates() {
        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    // Recovering last Geofence marker
    private void recoverlocationMarker() {
        Log.d("", "recoverlocationMarker");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.contains(KEY_GEOFENCE_LAT) && sharedPref.contains(KEY_GEOFENCE_LON)) {
            double lat = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LAT, -1));
            double lon = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LON, -1));
            LatLng latLng = new LatLng(lat, lon);
            markerForGeofence(latLng);
            drawGeofence();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("ONConnection failed", "failed status code = " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    private void seekbarFunction() {
        mSeekbar = (VerticalSeekBar) findViewById(R.id.verticalSeekbar);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(progress), 2000, null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    private int getNewGeofenceNumber() {
//        int a[] = {1,2,3};
//        int number = Array.getInt(a, 1);
        int number = mSharedPreferences.getInt(NEW_GEOFENCE_NUMBER, 1);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(NEW_GEOFENCE_NUMBER, number + 1);
        editor.apply();
        return number;
    }

    private void saveGeofence() {
        Log.d("", "saveGeofence()");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(KEY_GEOFENCE_LAT, Double.doubleToRawLongBits(locationMarker.getPosition().latitude));
        editor.putLong(KEY_GEOFENCE_LON, Double.doubleToRawLongBits(locationMarker.getPosition().longitude));
        editor.apply();
    }

    //add circle area
    private void drawGeofence() {
        Log.d("", "drawGeofence()");
        //remove last circle draw
        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(locationMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = mMap.addCircle(circleOptions);
    }

    @Override
    //when click the map
    public void onMapClick(LatLng latLng) {
        Log.d("", "onMapClick(" + latLng + ")");
      //  linearLayout.setVisibility(View.VISIBLE);
        textMeter.setVisibility(View.VISIBLE);
        radius.setVisibility(View.VISIBLE);
        textMeter.setVisibility(View.VISIBLE);
        spName.setVisibility(View.GONE);
        markerForGeofence(latLng);
        //  setupSpinner();
    }

    private void markerForGeofence(final LatLng latLng) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "Google Api not connected!", Toast.LENGTH_SHORT).show();
            return;
        }
        final String key = getNewGeofenceNumber() + "";
        final long expTime = System.currentTimeMillis() + GEOFENCE_EXPIRATION_IN_MILLISECONDS;
        addMarker(key, latLng);
        Geofence geofence = new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        GEOFENCE_RADIUS_IN_METERS
                )

                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    createGeofenceRequest(geofence),
                    createGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        //    saveGeofence();
                        //    drawGeofence();
                        GeofenceDbHelper.saveToDb(key, latLng.latitude, latLng.longitude, expTime);
                        Log.d("__DEBUG", "key :" + key + " Latitude :" + latLng.latitude + " Longitude :" + latLng.longitude + " expTime:" + expTime);
                        String tag_string_req = "req_postdata";
                        StringRequest strReq = new StringRequest(Request.Method.POST,
                                NetworkConfig.post, response -> {
                            Log.d("CLOG", "responh: " + response);
                            try {
                                JSONObject jObj = new JSONObject(response);
                                String status1 = jObj.getString("status");
                                if (status1.contains("200")) {
                                    JSONArray jArray = jObj.getJSONArray("data");
                                    for (int i = 0; i < jArray.length(); i++) {
                                        JSONObject jData = jArray.getJSONObject(i);
                                        Gson gson = new Gson();
                                        JsonParser parser = new JsonParser();
                                        JsonElement mJson = parser.parse(jData.toString());
                                    }

                                } else {

                                    String msg = jObj.getString("message");
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }, error -> {
                            Log.d("CLOG", "verespon: " + error.toString());
                            String json = null;
                            NetworkResponse response = error.networkResponse;
                            if (response != null && response.data != null) {
                                json = new String(response.data);
                                JSONObject jObj = null;
                                try {
                                    jObj = new JSONObject(json);
                                    String msg = jObj.getString("message");
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                // Posting parameters to login url
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("numbers", key);
                                params.put("latitude", String.valueOf(latLng.latitude));
                                params.put("longitude", String.valueOf(latLng.longitude));
                                params.put("expires", String.valueOf(expTime));
                                return params;
                            }
                        };

                        // Adding request to request queue
                        App.getInstance().addToRequestQueue(strReq, tag_string_req);


                        Log.d("SAVE", "key = " + key + " lat = " + latLng.latitude + " long = " + latLng.longitude + " exp = " + expTime);
                        Toast.makeText(MapsActivity.this, "Geofence Added!", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = GeofenceTransitionService.getErrorString(status.getStatusCode());
                        Log.e("ERROR MESSAGE", errorMessage);
                    }
                }
            });
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        } catch (SQLException e) {
            e.getStackTrace();
        }

    }

    private void logSecurityException(SecurityException securityException) {
        Log.e("ERROR PERMISSION", "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    private void addMarker(String key, LatLng latLng) {
        mMap.addMarker(new MarkerOptions()
                .title("G:" + key)
                .snippet("Click here if you want delete this geofence")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng));
        mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(GEOFENCE_RADIUS_IN_METERS)
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#80ff0000")));
    }


    //info from marker
    @Override
    public void onInfoWindowClick(Marker marker) {
        final String requestId = marker.getTitle().split(":")[1];
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "GeoFence Not connected!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            List<String> idList = new ArrayList<>();
            // perlu dikembangkan
            idList.add(requestId);
            Log.d("idlist = ",idList.toString());
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idList).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        //remove from db
                        GeofenceStorage.removeGeofence(requestId);
                        Log.d("REMOVE", "key = " + requestId);
                        Toast.makeText(MapsActivity.this, "Geofence removed!", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            reloadMapMarkers();
                        }
                    } else {
                        // Get the status code for the error and log it using a UserActivity-friendly message.
                        String errorMessage = GeofenceTransitionService.getErrorString(status.getStatusCode());
                        Log.e("ERROR WINDOW CLICK", errorMessage);
                    }
                }
            });

        } catch (SecurityException e) {
            e.getLocalizedMessage();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    //load from db
    private void reloadMapMarkers() {
        mMap.clear();
        api.getAllData().enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                Data data = response.body();
                for (int i = 0; i < data.getResult().size(); i++) {
                    if (data.getResult() != null) {
                        number = data.getResult().get(i).getNumbers();
                        latitude = Double.parseDouble(data.getResult().get(i).getLatitude());
                        longitude = Double.parseDouble(data.getResult().get(i).getLongitude());
                        expires = Long.parseLong(data.getResult().get(i).getExpires());
                        Toast.makeText(MapsActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                        addMarker(number, new LatLng(latitude, longitude));
                    } else {
                        Toast.makeText(MapsActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                Log.d("test data", "latitude =" + latitude + "longitude =" + longitude + "expires =" + expires);
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.d("gagal", "gagal =" + t.getLocalizedMessage());
                Toast.makeText(MapsActivity.this, "gagal =" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }
}