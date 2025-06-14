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
import android.os.Bundle;
import android.provider.Settings;

import io.kommunicate.ui.kommunicate.utils.KmThemeHelper;
import io.kommunicate.ui.kommunicate.views.KmToast;
import io.kommunicate.ui.utils.InsetHelper;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.kommunicate.devkit.broadcast.ConnectivityReceiver;
import io.kommunicate.ui.CustomizationSettings;
import io.kommunicate.ui.R;
import io.kommunicate.ui.conversation.ConversationUIService;
import io.kommunicate.ui.instruction.KmPermissions;
import io.kommunicate.commons.commons.core.utils.PermissionsUtils;
import io.kommunicate.commons.commons.core.utils.Utils;
import io.kommunicate.commons.file.FileUtils;
import io.kommunicate.commons.json.GsonUtils;
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

import io.kommunicate.utils.KmUtils;


public class MobicomLocationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ActivityCompat.OnRequestPermissionsResultCallback {

    SupportMapFragment mapFragment;
    LatLng position;
    RelativeLayout sendLocation;
    private LinearLayout layout;
    public Snackbar snackbar;
    Location mCurrentLocation;
    protected GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    public static final int LOCATION_SERVICE_ENABLE = 1001;
    protected static final long UPDATE_INTERVAL = 5;
    protected static final long FASTEST_INTERVAL = 1;
    private ConnectivityReceiver connectivityReceiver;
    CustomizationSettings customizationSettings;
    Marker myLocationMarker;
    KmPermissions kmPermissions;
    static final String TAG = "MobicomLocationActivity";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private LinearLayout locationLinearLayout;
    private TextView sendLocationText;
    private Toolbar toolbar;
    private KmThemeHelper themeHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_km_location);

        toolbar = findViewById(R.id.toolbar_map_screen);
        toolbar.setTitle(getResources().getString(R.string.send_location));
        setSupportActionBar(toolbar);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            customizationSettings = (CustomizationSettings) GsonUtils.getObjectFromJson(jsonString, CustomizationSettings.class);
        } else {
            customizationSettings = new CustomizationSettings();
        }

        configureSentryWithKommunicateUI(this, customizationSettings.toString());
        themeHelper = KmThemeHelper.getInstance(this, customizationSettings);

        toolbar.setBackgroundColor(themeHelper.getToolbarColor());
        toolbar.setTitleTextColor(themeHelper.getToolbarTitleColor());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int iconColor = themeHelper.parseColorWithDefault(customizationSettings.getAttachmentIconsBackgroundColor().get(themeHelper.isDarkModeEnabledForSDK() ? 1 : 0),
                themeHelper.parseColorWithDefault(customizationSettings.getToolbarColor().get(themeHelper.isDarkModeEnabledForSDK() ? 1 : 0), themeHelper.getPrimaryColor()));
        KmUtils.setGradientSolidColor(findViewById(R.id.locationIcon), iconColor);
        KmUtils.setStatusBarColor(this, themeHelper.getStatusBarColor());

        layout = (LinearLayout) findViewById(R.id.footerAd);
        sendLocation = (RelativeLayout) findViewById(R.id.sendLocation);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        kmPermissions = new KmPermissions(MobicomLocationActivity.this, layout);
        locationLinearLayout = findViewById(R.id.km_location_linear_layout);
        sendLocationText = findViewById(R.id.km_send_location_text);
        if (themeHelper.isDarkModeEnabledForSDK()) {
            locationLinearLayout.setBackgroundColor(getResources().getColor(R.color.dark_mode_default));
            sendLocationText.setTextColor(getResources().getColor(R.color.white));
        }
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
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
        try {
            if (mCurrentLocation != null) {
                position = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                googleMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.draggable(true);
                if (myLocationMarker == null) {
                    myLocationMarker = googleMap.addMarker(markerOptions.position(position).title(""));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 20));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
                } else {
                    googleMap.addMarker(markerOptions.position(myLocationMarker.getPosition()).title(""));
                }
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {

                    }

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
            }

            sendLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.printLog(MobicomLocationActivity.this, TAG, "On click of send location button");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        new ConversationUIService(this).onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SERVICE_ENABLE) {
            if (((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                googleApiClient.connect();
            } else {
                KmToast.error(MobicomLocationActivity.this, R.string.unable_to_fetch_location, Toast.LENGTH_LONG).show();
            }
            return;
        }
    }

    public void processingLocation() {
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG,
                "onConnectionSuspended() called.");

    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (mCurrentLocation == null) {
                KmToast.error(this, R.string.waiting_for_current_location, Toast.LENGTH_SHORT).show();
                locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                locationRequest.setInterval(UPDATE_INTERVAL);
                locationRequest.setFastestInterval(FASTEST_INTERVAL);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }

            if (mCurrentLocation != null) {
                mapFragment.getMapAsync(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            boolean reloadMap = false;
            if (location != null) {
                if (mCurrentLocation == null) {
                    reloadMap = true;
                }
                mCurrentLocation = location;
                if (reloadMap) {
                    mapFragment.getMapAsync(this);
                }
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

    }

    @Override
    protected void onDestroy() {
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
