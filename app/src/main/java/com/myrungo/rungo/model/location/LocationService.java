package com.myrungo.rungo.model.location;

import android.app.*;
import android.content.Intent;
import android.location.Location;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.location.*;
import com.myrungo.rungo.AppActivity;
import com.myrungo.rungo.R;
import com.myrungo.rungo.Scopes;
import com.myrungo.rungo.model.database.AppDatabase;
import com.myrungo.rungo.model.database.entity.LocationDb;
import toothpick.Toothpick;

import javax.inject.Inject;

public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_1";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 0;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private NotificationManager mNotificationManager;

    private Double mDistance = 0.0;

    @Inject
    AppDatabase database;

    @Inject
    TraininigListener trainingListener;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        Toothpick.inject(this, Toothpick.openScope(Scopes.APP));

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        startForeground(NOTIFICATION_ID, getNotification());
    }


    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    @Override
    public void onDestroy() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        super.onDestroy();
    }

    private void onNewLocation(Location location) {
        if (!trainingListener.isRun()) {
            return;
        }

        database.getLocationDao().insert(new LocationDb(location.getLatitude(), location.getLongitude(), 0));

        //proverka na tochnost
        if (location.distanceTo(mLocation) > location.getAccuracy() + mLocation.getAccuracy()) {
            mDistance += location.distanceTo(mLocation);
            mLocation = location;
        }

        mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        trainingListener.send(mDistance);
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, AppActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Идёт пробежка")
                .setContentText(String.valueOf(mDistance))
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_cat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        return builder.build();
    }
}