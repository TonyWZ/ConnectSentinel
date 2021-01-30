package group2.connectsentinel.background;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import group2.connectsentinel.activities.MainActivity;
import group2.connectsentinel.data.Coordinates;
import group2.connectsentinel.datamanagement.FakeLocationData;
import group2.connectsentinel.datamanagement.FakeTripData;
import group2.connectsentinel.datamanagement.LocationDataSource;
import group2.connectsentinel.datamanagement.LocationServerData;
import group2.connectsentinel.datamanagement.TripDataSource;

public class AlarmReceiver extends BroadcastReceiver {

    private FusedLocationProviderClient locationClient;
    private static boolean isOnTrip = false;
    private static long id;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Alarm", "Broadcast Received");
        if(intent.getAction() != null && intent.getAction().equals("group2.connectsentinel.SetTripStatus")) {
            Log.v("Alarm", "Setting trip status");
            isOnTrip = intent.getBooleanExtra("isOnTrip", false);
            Log.v("Alarm", "status is " + isOnTrip);
            return;
        }
        Log.v("Alarm", "Not set trip status");
        id = intent.getLongExtra("id", -1l);
        Log.v("LocationLog", "got id " + id);
        if(id < 0){
            return;
        }
        Log.v("Alarm", "Non negative id");
        locationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.v("Alarm", "No Permission");
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lo = location.getLongitude();
                            double la = location.getLatitude();
                            MainActivity.currLongitude = lo;
                            MainActivity.currLatitude = la;
                            Coordinates coord = new Coordinates(la, lo);
                            Log.v("Alarm", "Got isOnTrip " + isOnTrip);
                            if(isOnTrip) {
                                TripDataSource tripDB = FakeTripData.getInstance();
                                tripDB.addLocationToTrip(id, coord);
                            }
                            Log.v("Alarm","Updated location");
                            Log.v("LocationLog", "logged location " + la + " " + lo);
                            Log.v("LocationLog","Got id " + id);
                            LocationDataSource locationDB = LocationServerData.getInstance();
                            locationDB.updateLocation(id, coord);
                        }
                    }
                });
    }
}
