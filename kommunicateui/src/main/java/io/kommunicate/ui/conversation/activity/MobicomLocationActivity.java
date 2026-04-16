package io.kommunicate.ui.conversation.activity;

import static io.kommunicate.ui.utils.SentryUtils.configureSentryWithKommunicateUI;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import io.kommunicate.commons.commons.core.utils.PermissionsUtils;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.devkit.broadcast.ConnectivityReceiver;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.activities.KmBaseActivity;
import io.kommunicate.ui.conversation.task.LoadSettingsAsyncTask;
import io.kommunicate.ui.instruction.KmPermissions;
import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.ui.kommunicate.views.KmToast;
import io.kommunicate.ui.utils.InsetHelper;
import io.kommunicate.utils.KmUtils;

public class MobicomLocationActivity extends KmBaseActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ActivityCompat.OnRequestPermissionsResultCallback {

    SupportMapFragment mapFragment;
    GoogleMap googleMap;
    LatLng position;
    RelativeLayout sendLocation;
    private LinearLayout layout;
    public Snackbar snackbar;
    Location mCurrentLocation;
    protected GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    public static final int LOCATION_SERVICE_ENABLE = 1001;
    protected static final long UPDATE_INTERVAL = 5000; // 5 seconds
    protected static final long FASTEST_INTERVAL = 1000; // 1 second
    private ConnectivityReceiver connectivityReceiver;
    CustomizationSettings customizationSettings;
    Marker myLocationMarker;
    KmPermissions kmPermissions;
    static final String TAG = "MobicomLocationActivity";
    private static final String PERF_TAG = "MobiLoc_Perf";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private LinearLayout locationLinearLayout;
    private TextView sendLocationText;
    private Toolbar toolbar;
    private KmThemeHelper themeHelper;
    private ProgressBar progressBar;
    private LoadSettingsAsyncTask loadSettingsAsyncTask;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        customizationSettings = new CustomizationSettings();
        setContentView(R.layout.activity_km_location);

        toolbar = findViewById(R.id.toolbar_map_screen);
        progressBar = findViewById(R.id.km_progress_bar);
        toolbar.setTitle(getResources().getString(R.string.send_location));
        setSupportActionBar(toolbar);

        loadSettingsAsyncTask = new LoadSettingsAsyncTask(this, new LoadSettingsAsyncTask.TaskListener() {
            @Override
            public void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPostExecute(CustomizationSettings customizationSettings) {
                if (customizationSettings == null) {
                    customizationSettings = new CustomizationSettings();
                }
                MobicomLocationActivity.this.customizationSettings = customizationSettings;
                progressBar.setVisibility(View.GONE);
                setupEdgeToEdge(customizationSettings);
                configureSentryWithKommunicateUI(MobicomLocationActivity.this, customizationSettings.toString());
                themeHelper = KmThemeHelper.getInstance(MobicomLocationActivity.this, customizationSettings);

                toolbar.setBackgroundColor(themeHelper.getToolbarColor());
                toolbar.setTitleTextColor(themeHelper.getToolbarTitleColor());
                KmUtils.setStatusBarColor(MobicomLocationActivity.this, themeHelper.getStatusBarColor());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                int iconColor = themeHelper.parseColorWithDefault(customizationSettings.getAttachmentIconsBackgroundColor().get(themeHelper.isDarkModeEnabledForSDK() ? 1 : 0),
                        themeHelper.parseColorWithDefault(customizationSettings.getToolbarColor().get(themeHelper.isDarkModeEnabledForSDK() ? 1 : 0), themeHelper.getPrimaryColor()));
                KmUtils.setGradientSolidColor(findViewById(R.id.locationIcon), iconColor);

                if (themeHelper.isDarkModeEnabledForSDK()) {
                    locationLinearLayout.setBackgroundColor(getResources().getColor(R.color.dark_mode_default));
                    sendLocationText.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });
        loadSettingsAsyncTask.execute();

        layout = findViewById(R.id.footerAd);
        sendLocation = findViewById(R.id.sendLocation);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        kmPermissions = new KmPermissions(MobicomLocationActivity.this, layout);
        locationLinearLayout = findViewById(R.id.km_location_linear_layout);
        sendLocationText = findViewById(R.id.km_send_location_text);

        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        processLocation();
        onNewIntent(getIntent());
        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        setupInsets();
    }

    private void setupInsets() {
        InsetHelper.configureSystemInsets(
                toolbar,
                -1,
                0,
                true
        );
        InsetHelper.configureSystemInsets(
                sendLocation,
                0,
                -1,
                false
        );
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            if (mCurrentLocation != null) {
                Utils.printLog(this, PERF_TAG, "onMapReady: Found existing location. Updating camera.");
                updateMapCamera(mCurrentLocation);
            }

            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) { }

                @Override
                public void onMarkerDrag(Marker marker) { }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    if (myLocationMarker != null) {
                        myLocationMarker.remove();
                    }
                    MarkerOptions newMarkerOptions = new MarkerOptions();
                    newMarkerOptions.draggable(true);
                    myLocationMarker = googleMap.addMarker(newMarkerOptions.position(marker.getPosition()).title(""));
                }
            });

            sendLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myLocationMarker != null) {
                        Intent intent = new Intent();
                        intent.putExtra(LATITUDE, myLocationMarker.getPosition().latitude);
                        intent.putExtra(LONGITUDE, myLocationMarker.getPosition().longitude);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        } catch (Exception e) {
            Utils.printLog(MobicomLocationActivity.this, TAG, "Check if location permission are added");
        }
    }

    private void updateMapCamera(Location location) {
        if (googleMap == null) {
            return;
        }
        position = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);
        if (myLocationMarker == null) {
            myLocationMarker = googleMap.addMarker(markerOptions.position(position).title(""));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 20));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
        } else {
            myLocationMarker = googleMap.addMarker(markerOptions.position(position).title(""));
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(position));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SERVICE_ENABLE) {
            if (((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                googleApiClient.connect();
            } else {
                KmToast.error(MobicomLocationActivity.this, R.string.unable_to_fetch_location, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void processingLocation() {
        boolean isGpsProviderEnabled = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
        Utils.printLog(this, PERF_TAG, "processingLocation: GPS provider enabled = " + isGpsProviderEnabled + " at " + (System.currentTimeMillis() - startTime) + "ms");

        if (!isGpsProviderEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.location_services_disabled_title)
                    .setMessage(R.string.location_services_disabled_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.location_service_settings, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, LOCATION_SERVICE_ENABLE);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            KmToast.error(MobicomLocationActivity.this, R.string.location_sending_cancelled, Toast.LENGTH_LONG).show();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            googleApiClient.disconnect();
            googleApiClient.connect();
        }
    }

    public void processLocation() {
        if (Utils.hasMarshmallow()) {
            kmPermissions.checkRuntimePermissionForLocationActivity();
        } else {
            processingLocation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        Utils.printLog(this, PERF_TAG, "onConnected: Start at " + (System.currentTimeMillis() - startTime) + "ms");
        try {
            boolean fineLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarseLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            Utils.printLog(this, PERF_TAG, "onConnected: Permissions fine=" + fineLocationPermission + ", coarse=" + coarseLocationPermission);

            if (!fineLocationPermission && !coarseLocationPermission) {
                Utils.printLog(this, PERF_TAG, "onConnected: No location permissions, exiting.");
                return;
            }

            // Bypassing getLastLocation() due to ANR. Directly requesting fresh location.
            KmToast.error(this, R.string.waiting_for_current_location, Toast.LENGTH_SHORT).show();
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);

            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                Utils.printLog(this, PERF_TAG, "onConnected: requestLocationUpdates call succeeded at " + (System.currentTimeMillis() - startTime) + "ms");
            } catch (Exception e) {
                Utils.printLog(this, PERF_TAG, "onConnected: requestLocationUpdates FAILED with exception: " + e.getMessage());
            }

        } catch (Exception e) {
            Utils.printLog(this, PERF_TAG, "onConnected: FAILED with general exception: " + e.getMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Utils.printLog(this, PERF_TAG, "onLocationChanged: New location received at " + (System.currentTimeMillis() - startTime) + "ms");
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            if (location != null) {
                mCurrentLocation = location;
                updateMapCamera(location);
            }
        } catch (Exception e) {
        }
    }

    public void showSnackBar(int resId) {
        try {
            snackbar = Snackbar.make(layout, resId,
                    Snackbar.LENGTH_SHORT);
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Utils.printLog(this, PERF_TAG, "onConnectionFailed: " + connectionResult + " at " + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Override
    protected void onDestroy() {
        if (loadSettingsAsyncTask != null) {
            loadSettingsAsyncTask.cancel(true);
        }
        super.onDestroy();
        try {
            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsUtils.REQUEST_LOCATION) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.location_permission_granted);
                processingLocation();
            } else {
                showSnackBar(R.string.location_permission_not_granted);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
