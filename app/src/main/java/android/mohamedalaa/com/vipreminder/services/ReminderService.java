package android.mohamedalaa.com.vipreminder.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.customClasses.UpdateReminderAsyncTask;
import android.mohamedalaa.com.vipreminder.model.database.AppDatabase;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.NotificationUtils;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mohamed on 8/11/2018.
 *
 */
public class ReminderService extends IntentService {

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        long rowId = -1;
        if (intent != null){
            // Used action not any other extra so that, I can cancel it with alarmManager.cancel();
            String stringOfRowId = intent.getAction();

            try {
                rowId = Long.parseLong(stringOfRowId);
            }catch (Exception e){
                // In case of any un-expected error
            }
        }

        Context context = getApplicationContext();

        AppDatabase appDatabase = AppDatabase.getInstance(context);

        ReminderEntity reminderEntity = appDatabase
                .getReminderDao().getReminderEntityByIdSync(rowId);
        if (reminderEntity != null){
            String label = reminderEntity.getLabel();

            if (reminderEntity.isDateAndTimeCondition()
                    && !StringUtils.isNullOrEmpty(reminderEntity.getPlaceId())){
                // Then we must check the place as well.
                if (fireNotificationAfterCheckingPlace(context, reminderEntity.getPlaceId())){
                    // Make the notification
                    NotificationUtils.makeReminderNotification(
                            context, context.getString(R.string.both_time_and_place), label);
                }
            }else {
                // Make the notification
                NotificationUtils.makeReminderNotification(
                        context, context.getString(R.string.look_at_the_time), label);
            }

            // Re-scheduling Work Manager -> which will be done in that async task
            // or geofence instead if it depends on place only
            // Mark as done unless there is place dependence
            reminderEntity.setDone(true);
            if (! reminderEntity.getRepeatMode().equals(context.getString(R.string.once))){
                reminderEntity.setTime(getNextReminderTime(context, reminderEntity.getTime(),
                        reminderEntity.getRepeatMode(), reminderEntity.getRepeatDays()));
                reminderEntity.setDone(false);
            }

            DataRepository dataRepository = ((BaseApplication) getApplicationContext()).getRepository();

            new UpdateReminderAsyncTask(getApplicationContext(), dataRepository, reminderEntity, context.getString(R.string.once),
                    context.getString(R.string.hourly), context.getString(R.string.daily),
                    context.getString(R.string.weekly), context.getString(R.string.monthly),
                    context.getString(R.string.yearly)).doInBackground();
        }
    }

    @SuppressWarnings("deprecation")
    private boolean fireNotificationAfterCheckingPlace(Context context, String placeId) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        googleApiClient.blockingConnect();

        // get Current device Location
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (location != null){
                final double currentLatitude = location.getLatitude();
                final double currentLongitude = location.getLongitude();

                PendingResult<PlaceBuffer> placeResultFromId = Places.GeoDataApi.getPlaceById(googleApiClient,
                        placeId);
                PlaceBuffer placeBuffer = placeResultFromId.await();
                Place currentPlace = placeBuffer.get(0);
                final double placeLatitude = currentPlace.getLatLng().latitude;
                final double placeLongitude = currentPlace.getLatLng().longitude;

                placeBuffer.release();

                // Compare radius of 50 m
                float[] distance = new float[1];
                Location.distanceBetween(currentLatitude, currentLongitude,
                        placeLatitude, placeLongitude, distance);

                if(distance[0] < 50){
                    // radius < 50 metre
                    return true;
                }
            }
        }

        // Cannot get current place, so no notification
        return false;
    }

    private long getNextReminderTime(Context context, long reminderTime, String repeatMode, String repeatDays){
        String lowerCaseRepeatDays = repeatDays.toLowerCase();

        boolean continueTheLoop;
        do {
            reminderTime = getNextReminderTimeWithoutLookingToDays(context, reminderTime, repeatMode);

            String dayInWeek = new SimpleDateFormat("EEE", Locale.getDefault())
                    .format(new Date(reminderTime)).toLowerCase();
            dayInWeek = dayInWeek.substring(0, 2);

            continueTheLoop = ! lowerCaseRepeatDays.contains(dayInWeek);
        }while (continueTheLoop);

        return reminderTime;
    }

    private long getNextReminderTimeWithoutLookingToDays(Context context, long reminderTime, String repeatMode) {
        long value = reminderTime;

        // Impossible to be once, as already once is checked before this method
        if (repeatMode.equals(context.getString(R.string.hourly))){
            value += TimeUnit.HOURS
                    .toMillis(1);
        }else if (repeatMode.equals(context.getString(R.string.daily))){
            value += TimeUnit.DAYS
                    .toMillis(1);
        }else if (repeatMode.equals(context.getString(R.string.weekly))){
            value += TimeUnit.DAYS
                    .toMillis(7);
        }else if (repeatMode.equals(context.getString(R.string.monthly))){
            value += TimeUnit.DAYS
                    .toMillis(30);
        }else if (repeatMode.equals(context.getString(R.string.yearly))){
            value += TimeUnit.DAYS
                    .toMillis(365);
        }

        return value;
    }

}
