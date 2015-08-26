package de.tudarmstadt.informatik.bp.bonfirechat.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by johannes on 10.07.15.
 */
public class GpsTracker implements LocationListener {

    private static final String TAG = "GpsTracker";

    /*
     * Singleton
     */
    private static GpsTracker instance;

    public static GpsTracker getInstance() {
        return instance;
    }

    public static void init(Context ctx) {
        instance = new GpsTracker(ctx);
    }

    /*
     * location stuff
     */

    private final Context ctx;

    public boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // as often as possible
    private static final long MIN_TIME_BW_UPDATES = 0;

    protected LocationManager locationManager;

    public GpsTracker(Context context) {
        this.ctx = context;
        getLocation();
    }

    private Location getLocation() {
        try {
            locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "isGPSEnabled: " + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "isNetworkLocationEnabled: " + isNetworkEnabled);

            if (isGPSEnabled || isNetworkEnabled) {
                // try network first
                if (isNetworkEnabled) {
                    Log.i(TAG, "requesting continuous updates on location changes via network");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        this.canGetLocation = true;
                        Log.d(TAG, "setting location based on network: " + location.getLatitude() + "N, " + location.getLongitude() + "E");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
                // if GPS Enabled get lat/long using GPS Services (override location)
                if (isGPSEnabled) {
                    Log.i(TAG, "requesting continuous updates on location changes via GPS");
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        this.canGetLocation = true;
                        Log.d(TAG, "setting location based on GPS: " + location.getLatitude() + "N, " + location.getLongitude() + "E");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation && this.location != null;
    }

    /*
     * receive location updates
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(TAG, "updating location: " + location.getLatitude() + "N, " + location.getLongitude() + "E");
            this.location = location;
            this.canGetLocation = true;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}
}
