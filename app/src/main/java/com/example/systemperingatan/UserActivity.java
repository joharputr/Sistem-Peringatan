package com.example.systemperingatan;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.systemperingatan.API.Api;
import com.example.systemperingatan.API.Data;
import com.example.systemperingatan.API.NetworkConfig;
import com.example.systemperingatan.API.Result;
import com.example.systemperingatan.SQLite.GeofenceContract;
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

public class UserActivity extends FragmentActivity implements OnMapReadyCallback,
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
    //Retrofit
    Api api = NetworkConfig.getClient().create(Api.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        initMap();
        setUpLocation();
//       seekbarFunction();
     //    setupSpinner();

        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mpendingIntent = null;

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

    //add gps location now
    private void displayLocation() {
        Log.d("LOG Cek lokasi", "cek lokasi");
        if (mMap != null) {
            if (checkPermission()) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    Log.i("LOG LAST GPS LOCATION", "LasKnown location. " +
                            "Long: " + mLastLocation.getLongitude() +
                            " | Lat: " + mLastLocation.getLatitude());
                    //add gps location
                    markerLocation(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                } else {
                    Log.w("LOG FAILED", "No location retrieved yet");
//                startLocationUpdates();
                }
            } else {
                askPermission();
            }
        }
    }

    //move camera to current gps location
    private void markerLocation(LatLng latLng) {
        Log.i("LOG TLOCATION", "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("lokasi saya = "+title);
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


    private PendingIntent createGeofencePendingIntent() {
        Log.d("LOGCREATEPENDING INTENT", "createGeofencePendingIntent");
        if (mpendingIntent != null) {
            Log.d("Pending gagal", "pending ggagal");
            return mpendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        Log.d("LOG Pending test", "pending test");
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("LOG onMapReady", "onMapReady()");
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


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("LOG ONConnection failed", "failed status code = " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }



    @Override
    public void onResult(@NonNull Status status) {

    }


    @Override
    //when click the map
    public void onMapClick(LatLng latLng) {
        Log.d("LOG", "onMapClick(" + latLng + ")");

    }


    private void logSecurityException(SecurityException securityException) {
        Log.e("LOG ERROR PERMISSION", "Invalid location permission. " +
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
    @Override
    public void onInfoWindowClick(Marker marker) {
        final String requestId = marker.getTitle().split(":")[1];
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "GeoFence Not connected!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            List<String> idList = new ArrayList<>();
            idList.add(requestId);
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idList).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        GeofenceStorage.removeGeofence(requestId);
                        Log.d("LOG REMOVE", "key = " + requestId);
                        Toast.makeText(UserActivity.this, "Geofence removed!", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            reloadMapMarkers();
                        }
                    } else {
                        // Get the status code for the error and log it using a UserActivity-friendly message.
                        String errorMessage = GeofenceTransitionService.getErrorString(status.getStatusCode());
                        Log.e("LOG ERROR WINDOW CLICK", errorMessage);
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
    //select all from db
    private void reloadMapMarkers() {
        mMap.clear();
        api.getAllData().enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                Data data = response.body();
                for (int i = 0 ; i < data.getResult().size(); i++){
                    if (data.getResult() != null){
                        number = data.getResult().get(i).getNumbers();
                        latitude = Double.parseDouble(data.getResult().get(i).getLatitude());
                        longitude = Double.parseDouble(data.getResult().get(i).getLongitude());
                        expires = Long.parseLong(data.getResult().get(i).getExpires());
                        Toast.makeText(UserActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                        addMarker(number, new LatLng(latitude, longitude));
                    }else {
                        Toast.makeText(UserActivity.this, "data kososng", Toast.LENGTH_SHORT).show();
                    }
                }

                Log.d("test data", "latitude =" + latitude + "longitude =" + longitude + "expires =" + expires);
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.d("gagal", "gagal =" + t.getLocalizedMessage());
                Toast.makeText(UserActivity.this, "gagal =" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }



    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }
}