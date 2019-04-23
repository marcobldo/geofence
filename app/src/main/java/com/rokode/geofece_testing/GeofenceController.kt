package com.rokode.geofece_testing

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat


class GeofenceController : IntentService("GeofenceController") {
    @SuppressLint("ObsoleteSdkInt")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = NotificationCompat.Builder(this, "GeoFenceService")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText("Geofence Service")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        val notification = builder.build()
        startForeground(1, notification)
        return Service.START_NOT_STICKY
    }

    override fun onHandleIntent(intent: Intent?) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofence event hasError")
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            /*
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                this,
                geofenceTransition,
                triggeringGeofences
            )
            */
            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails)
            Log.i(TAG, geofenceTransition.toString())
            showPushNotification(geofenceTransition.toString())
            //Toast.makeText(applicationContext,"geofenceTransition: $geofenceTransition", Toast.LENGTH_SHORT).show()
        } else {
            // Log the error.
            Log.i(TAG, "transition invalid type")
            //showPushNotification(triggeringGeofences.toString())
            Toast.makeText(applicationContext,"invalid geofenceTransition", Toast.LENGTH_SHORT).show()


        }
    }


    fun showPushNotification (message : String){
        val builder = if (Build.VERSION.SDK_INT > 26) {
            NotificationCompat.Builder(this, applicationContext.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText("Geofence trigger: $message")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true)
        } else {
            NotificationCompat.Builder(this, applicationContext.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText("Geofence trigger: $message")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
        }
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(0, builder.build())
        }
    }
}
