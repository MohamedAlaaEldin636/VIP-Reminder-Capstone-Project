package android.mohamedalaa.com.vipreminder.customClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.mohamedalaa.com.vipreminder.services.FromGeofenceReminderService;
import android.mohamedalaa.com.vipreminder.services.JobFromGeofenceReminderService;
import android.mohamedalaa.com.vipreminder.services.JobReminderService;
import android.os.Build;
import android.os.Bundle;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/8/2018.
 *
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int delayInSeconds = 0;

            // Made below alternative for android Oreo and above due to the background limitation
            // link -> https://developer.android.com/about/versions/oreo/background
            // because if not done it will make error on devices running Oreo and above
            Driver driver = new GooglePlayDriver(context.getApplicationContext());
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
            Bundle extras = new Bundle();
            extras.putString(JobFromGeofenceReminderService.BUNDLE_KEY_STRING_OF_ROW_ID, String.valueOf(rowId));
            Job job = dispatcher.newJobBuilder()
                    .setService(JobFromGeofenceReminderService.class)
                    .setTag(JobFromGeofenceReminderService.GENERAL_TAG)
                    .setRecurring(false)
                    .setTrigger(Trigger.executionWindow(delayInSeconds, delayInSeconds + 30))
                    .setExtras(extras)
                    .build();
            dispatcher.mustSchedule(job);
        }else {
            Intent reminderServiceIntent = new Intent(context, FromGeofenceReminderService.class);
            reminderServiceIntent.setAction(String.valueOf(rowId));
            context.startService(reminderServiceIntent);
        }

        Timber.v("inside onReceive of geofence broadcast, And rowId == " + rowId);
    }
}
