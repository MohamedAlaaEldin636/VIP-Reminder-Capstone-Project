package android.mohamedalaa.com.vipreminder.customClasses;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.mohamedalaa.com.vipreminder.utils.AlarmManagerUtils;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/8/2018.
 *
 */
@SuppressWarnings({"deprecation", "WeakerAccess"})
public class GeofenceStatic {

    // --- Constants

    private static final float GEOFENCE_RADIUS = 50; // 50 meters
    public static final long GEOFENCE_TIMEOUT = TimeUnit.DAYS.toMillis(30); // 1 Month

    private static final int PENDING_INTENT_REQUEST_CODE = 302;

    // --- Private Variables

    private GoogleApiClient googleApiClient;

    private ResultCallback<Status> resultCallback = status
            -> Timber.v("inside ResultCallback<Status> resultCallback - " + status.toString());

    public GeofenceStatic() {}

    /**
     * MUST Run in background Thread as it runs long or short operations synchronously,
     *      in the current thread.
     */
    public void registerPlaceById(Context context, String placeId, int rowId){
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        googleApiClient.blockingConnect();

        if (!googleApiClient.isConnected()) {
            Timber.v("Google client isn't connected");
            return;
        }
        if (StringUtils.isNullOrEmpty(placeId)){
            Timber.v("Place ID is null or empty");
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    getGeofencingRequestFromPlaceId(placeId),
                    getGeofencePendingIntent(context, rowId)
            ).setResultCallback(resultCallback);

            Timber.v("Done with registering place by id");

            // Make alarm manager to be notified after 1 month to re-register all geofences
            // since timeout cannot be FOREVER since that thing is dangerous to be used
            // since even when this app is un-installed the geofence will remain
            // so better to have timeout and i chose 1 month
            AlarmManagerUtils.setRefreshGeofenceAlarm(context, GEOFENCE_TIMEOUT);

            Timber.v("Done with making geofence worker");
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Timber.v("SecurityException when adding geofences " + securityException.getMessage());
        }
    }

    public void deleteGeofencesByPlacesIds(Context context, List<String> placesIds){
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        googleApiClient.blockingConnect();

        if (!googleApiClient.isConnected()) {
            Timber.v("Google client isn't connected -> in removing");
            return;
        }

        try {
            LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient,
                    placesIds
            ).setResultCallback(resultCallback);

            Timber.v("Done with removing place by id");
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Timber.v("SecurityException when removing geofences " + securityException.getMessage());
        }
    }

    public void deleteGeofenceByPlaceId(Context context, String placeId){
        List<String> placesIds = new ArrayList<>();
        placesIds.add(placeId);
        deleteGeofencesByPlacesIds(context, placesIds);
    }

    // ---- Private Methods

    private PendingIntent getGeofencePendingIntent(Context context, int rowId) {
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        intent.setAction(String.valueOf(rowId));

        return PendingIntent.getBroadcast(
                context, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequestFromPlaceId(String placeId) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(getGeofenceObjectGivenPlacesId(placeId));
        return builder.build();
    }

    /** {@link #getGeofencingRequestFromPlaceId(String)} */

    private Geofence getGeofenceObjectGivenPlacesId(String placeId){
        // Get latitude and longitude
        PendingResult<PlaceBuffer> placeResultFromId = Places.GeoDataApi.getPlaceById(googleApiClient,
                placeId);
        PlaceBuffer placeBuffer = placeResultFromId.await();
        Place currentPlace = placeBuffer.get(0);
        final double placeLatitude = currentPlace.getLatLng().latitude;
        final double placeLongitude = currentPlace.getLatLng().longitude;
        placeBuffer.release();

        Timber.v("latit -> " + placeLatitude + ", - longitude -> " + placeLongitude);

        // Return the geofence object
        return new Geofence.Builder()
                .setRequestId(placeId)
                .setExpirationDuration(GEOFENCE_TIMEOUT)
                .setCircularRegion(placeLatitude, placeLongitude, GEOFENCE_RADIUS)
                /* We need it when we enter only */
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }
}
