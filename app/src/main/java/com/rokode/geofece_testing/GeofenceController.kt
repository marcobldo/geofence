package com.rokode.geofece_testing

import android.app.IntentService
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent


class GeofenceController : IntentService("GeofenceController") {

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
            Log.i(TAG, triggeringGeofences.toString())
            Toast.makeText(applicationContext,"geofenceTransition: $geofenceTransition", Toast.LENGTH_SHORT).show()
        } else {
            // Log the error.
            Log.i(TAG, "transition invalid type")
            Toast.makeText(applicationContext,"invalid geofenceTransition", Toast.LENGTH_SHORT).show()


        }
    }
}
