package com.rokode.geofece_testing

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.awareness.Awareness;
import android.content.IntentFilter
import android.app.PendingIntent
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.awareness.fence.FenceState
import com.google.android.gms.awareness.fence.FenceUpdateRequest
import com.google.android.gms.awareness.state.HeadphoneState
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private lateinit var mMap: GoogleMap
    // The fence key is how callback code determines which fence fired.
    val FENCE_KEY = "fence_key"
    private val TAG = "MapsActivity"
    private var googleApiClient : GoogleApiClient? = null
    private var geofencingClient : GeofencingClient? = null
    // The intent action which will be fired when your fence is triggered.
    val FENCE_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION"
    val MY_PERMISSION_LOCATION = 1
    val FENCE_RADIO = 50
    val FENCE_LAT = 20.6565374
    val FENCE_LON = -103.3915136


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //init google apli client
        googleApiClient = GoogleApiClient.Builder(applicationContext)
            .addApi(Awareness.getSnapshotClient(applicationContext).api)
            .addApi(Awareness.getFenceClient(applicationContext).api)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .enableAutoManage(this,this)
            .build()

        val startBtn : Button = findViewById(R.id.startBtn)
        startBtn.setOnClickListener { _ ->
            //getHeadphoneState()
            //startFence()
        }
    }

    private fun startFence() {
        if(checkAndRequestLocationPermissions()){
            //permissions conceded!
            //1. init GeoFence api
            val geofenceList : ArrayList<Geofence> = ArrayList();
            geofencingClient = LocationServices.getGeofencingClient(this)

            //2. Create geofence objects
            geofenceList.add(Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(FENCE_KEY)
                // Set the circular region of this geofence.
                .setCircularRegion(
                    FENCE_LAT,
                    FENCE_LON,
                    FENCE_RADIO.toFloat()
                )
                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                // Create the geofence.
                .build())

            //3. Specify geofences and initial triggers
            val geofenceRequest : GeofencingRequest
            geofenceRequest = GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                addGeofences(geofenceList)
            }.build()


            //4. Define an intent for geofence transitions
            val pendientIntent : PendingIntent;
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            pendientIntent =
                    PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


            //5. Add geofences
            geofencingClient?.addGeofences(geofenceRequest, pendientIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    Log.i(TAG, "addGeofences. Geofences added.")
                }
                addOnFailureListener {
                    // Failed to add geofences
                    Log.e(TAG, "addGeofences. Failed to add geofences")

                }
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleApiClient?.connect()

    }

    fun drawCircle(point: LatLng){
        // Instantiating CircleOptions to draw a circle around the marker
        val circleOptions : CircleOptions  = CircleOptions();
        // Specifying the center of the circle
        circleOptions.center(point);
        // Radius of the circle
        circleOptions.radius(FENCE_RADIO.toDouble());
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);
        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);
        // Border width of the circle
        circleOptions.strokeWidth(2F);
        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onConnected(p0: Bundle?) {
        Log.i(TAG, "GoogleApiClient onConnected.")
        val fencePosition = LatLng(FENCE_LAT,FENCE_LON)
        val cameraUpdate : CameraUpdate = CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(fencePosition)
            .zoom(30f)
            .build())
        mMap.moveCamera(cameraUpdate)
        if(checkAndRequestLocationPermissions()){
            mMap.isIndoorEnabled = true
            mMap.isMyLocationEnabled = true
            startFence()
        }
        drawCircle(fencePosition)
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.w(TAG, "GoogleApiClient onConnectionSuspended.")

    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e(TAG, "GoogleApiClient onConnectionFailed.")
    }

    private fun getHeadphoneState() {
        Log.i(TAG, "getHeadphoneState()")

        // Each type of contextual information in the snapshot API has a corresponding "get" method.
        //  For instance, this is how to get the user's current Activity.
        Awareness.getSnapshotClient(applicationContext).detectedActivity
            .addOnSuccessListener { dar ->
                val arr = dar.activityRecognitionResult
                // getMostProbableActivity() is good enough for basic Activity detection.
                // To work within a threshold of confidence,
                // use ActivityRecognitionResult.getProbableActivities() to get a list of
                // potential current activities, and check the confidence of each one.
                val probableActivity = arr.mostProbableActivity

                val confidence = probableActivity.confidence
                val activityStr = probableActivity.toString()
                Log.i(TAG, "Activity: " + activityStr
                        + ", Confidence: " + confidence + "/100" )
            }
            .addOnFailureListener {
                    e -> Log.e(TAG, "Could not detect activity: $e")
            }


        // Pulling headphone state is similar, but doesn't involve analyzing confidence.
        Awareness.getSnapshotClient(this).headphoneState
            .addOnSuccessListener { headphoneStateResponse ->
                val headphoneState = headphoneStateResponse.headphoneState
                val pluggedIn = headphoneState.state == HeadphoneState.PLUGGED_IN
                val stateStr = "Headphones are " + (if (pluggedIn) "plugged in" else "unplugged")
                Log.i(TAG, "headphoneStateResponse: $stateStr")
                Toast.makeText(applicationContext,"headphoneStateResponse: $stateStr", Toast.LENGTH_SHORT)
            }
            .addOnFailureListener {
                    e -> Log.e(TAG, "Could not get headphone state: $e")
            }

        // Some of the data available via Snapshot API requires permissions that must be checked
        // at runtime.  Weather snapshots are a good example of this.  Since weather is protected
        // by a runtime permission, and permission request callbacks will happen asynchronously,
        // the easiest thing to do is put weather snapshot code in its own method.  That way it
        // can be called from here when permission has already been granted on subsequent runs,
        // and from the permission request callback code when permission is first granted.
        checkAndRequestLocationPermissions()
    }

    /**
     * Helper method to retrieve weather data using the Snapshot API.  Since Weather is protected
     * by a runtime permission, this snapshot code is going to be called in multiple places:
     * [.getHeadphoneState] when the permission has already been accepted, and
     * [.onRequestPermissionsResult] when the permission is requested
     * and has been granted.
     */
    private fun getWeatherSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).weather
                .addOnSuccessListener { weatherResponse ->
                    val weather = weatherResponse.weather
                    weather.conditions
                    Log.i(TAG, "Weather: $weather")
                }
                .addOnFailureListener {
                        e -> Log.e(TAG, "Could not get weather: $e")
                }
        }
    }

    /**
     * Helper method to handle requesting the runtime permissions required for weather snapshots.
     *
     * @return true if the permission has already been granted, false otherwise.
     */
    private fun checkAndRequestLocationPermissions(): Boolean {
        var isPermissionsGaranted = false;

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                Log.i(TAG, "Permission previously denied and app shouldn't ask again.  Skipping" + " weather snapshot.")
                requestPermissions()
            }
            else {
                requestPermissions()
            }
        }else {
            //permissions success
            isPermissionsGaranted = true;
        }

        return isPermissionsGaranted;
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
            MY_PERMISSION_LOCATION
        )
    }

    /**
     * A basic BroadcastReceiver to handle intents from from the Awareness API.
     */
    class FenceReceiver : BroadcastReceiver() {
        // The fence key is how callback code determines which fence fired.
        val FENCE_KEY = "fence_key"
        private val TAG : String = "BroadcastReceiver"
        // The intent action which will be fired when your fence is triggered.
        val FENCE_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION"

        override fun onReceive(context: Context?, intent: Intent?) {
            if (FENCE_RECEIVER_ACTION != (intent?.action).toString()) {
                Log.i(TAG, "Received an unsupported action in FenceReceiver: action=" + intent?.action)
                return
            }
            // The state information for the given fence is em
            val fenceState = FenceState.extract(intent)

            if ( fenceState.fenceKey === FENCE_KEY) {
                val fenceStateStr: String = when (fenceState.currentState) {
                    FenceState.TRUE -> "true"
                    FenceState.FALSE -> "false"
                    FenceState.UNKNOWN -> "unknown"
                    else -> "unknown value"
                }
                Log.i(TAG, "Fence state: $fenceStateStr")
            }
        }

    }
}
