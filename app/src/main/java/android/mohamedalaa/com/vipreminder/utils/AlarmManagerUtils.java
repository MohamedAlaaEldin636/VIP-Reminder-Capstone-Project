package android.mohamedalaa.com.vipreminder.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.mohamedalaa.com.vipreminder.services.JobRefreshGeofencesService;
import android.mohamedalaa.com.vipreminder.services.JobReminderService;
import android.mohamedalaa.com.vipreminder.services.RefreshGeofencesService;
import android.mohamedalaa.com.vipreminder.services.ReminderService;
import android.os.Build;
import android.os.Bundle;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/11/2018.
 *
 */
public class AlarmManagerUtils {

    private static final int PENDING_INTENT_REMINDER_REQUEST_CODE = 87;

    private static final int PENDING_INTENT_REFRESH_GEOFENCE_REQUEST_CODE = 487;

    /**
     * Below method will trigger the pendingIntent that will start ->
     *      {@link android.mohamedalaa.com.vipreminder.services.ReminderService}
     */
    public static void setReminderAlarm(Context context, long rowId, long initialDelayInMillis){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            long longDelayInSeconds = initialDelayInMillis / 1000;
            int delayInSeconds = Integer.parseInt(String.valueOf(longDelayInSeconds));

            // Made below alternative for android Oreo and above due to the background limitation
            // link -> https://developer.android.com/about/versions/oreo/background
            // because if not done it will make error on devices running Oreo and above
            Driver driver = new GooglePlayDriver(context.getApplicationContext());
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
            Bundle extras = new Bundle();
            extras.putString(JobReminderService.BUNDLE_KEY_STRING_OF_ROW_ID, String.valueOf(rowId));
            Job job = dispatcher.newJobBuilder()
                    .setService(JobReminderService.class)
                    .setTag(String.valueOf(rowId))
                    .setRecurring(false)
                    .setTrigger(Trigger.executionWindow(delayInSeconds, delayInSeconds + 30))
                    .setExtras(extras)
                    .build();
            dispatcher.mustSchedule(job);

            return;
        }

        long fireAlarmAt = System.currentTimeMillis() + initialDelayInMillis;

        // Alarm Manager instance
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null){
            Timber.v("Error -> Couldn't get instance of Alarm Manager");
            return;
        }

        // Get Pending Intent with the ReminderService intent
        PendingIntent pendingIntent = getReminderServicePendingIntent(context, rowId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAlarmAt, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, fireAlarmAt, pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, fireAlarmAt, pendingIntent);
        }
    }

    public static void setRefreshGeofenceAlarm(Context context, long initialDelayInMillis){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            long longDelayInSeconds = initialDelayInMillis / 1000;
            int delayInSeconds = Integer.parseInt(String.valueOf(longDelayInSeconds));

            // Made below alternative for android Oreo and above due to the background limitation
            // link -> https://developer.android.com/about/versions/oreo/background
            // because if not done it will make error on devices running Oreo and above
            Driver driver = new GooglePlayDriver(context.getApplicationContext());
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
            Job job = dispatcher.newJobBuilder()
                    .setService(JobRefreshGeofencesService.class)
                    .setTag(JobRefreshGeofencesService.INTENT_ACTION_REFRESH_GEOFENCE_SERVICE)
                    .setRecurring(false)
                    .setTrigger(Trigger.executionWindow(delayInSeconds, delayInSeconds + 30))
                    .build();
            dispatcher.mustSchedule(job);

            return;
        }

        long fireAlarmAt = System.currentTimeMillis() + initialDelayInMillis;

        // Alarm Manager instance
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null){
            Timber.v("Error -> Couldn't get instance of Alarm Manager");
            return;
        }

        // Get Pending Intent with the ReminderService intent
        PendingIntent pendingIntent = getRefreshGeofencePendingIntent(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAlarmAt, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, fireAlarmAt, pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, fireAlarmAt, pendingIntent);
        }
    }

    public static void cancelAlarmManager(Context context, long rowId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Made below alternative for android Oreo and above due to the background limitation
            // link -> https://developer.android.com/about/versions/oreo/background
            // because if not done it will make error on devices running Oreo and above
            Driver driver = new GooglePlayDriver(context.getApplicationContext());
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
            dispatcher.cancel(String.valueOf(rowId));

            return;
        }

        // Alarm Manager instance
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null){
            Timber.v("Error -> Couldn't get instance of Alarm Manager");
            return;
        }

        // Get Pending Intent with the ReminderService intent
        PendingIntent pendingIntent = getReminderServicePendingIntent(context, rowId);

        manager.cancel(pendingIntent);
    }

    public static void cancelRefreshGeofenceAlarmManager(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Made below alternative for android Oreo and above due to the background limitation
            // link -> https://developer.android.com/about/versions/oreo/background
            // because if not done it will make error on devices running Oreo and above
            Driver driver = new GooglePlayDriver(context.getApplicationContext());
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
            dispatcher.cancel(JobRefreshGeofencesService.INTENT_ACTION_REFRESH_GEOFENCE_SERVICE);

            return;
        }

        // Alarm Manager instance
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null){
            Timber.v("Error -> Couldn't get instance of Alarm Manager");
            return;
        }

        // Get Pending Intent with the ReminderService intent
        PendingIntent pendingIntent = getRefreshGeofencePendingIntent(context);

        manager.cancel(pendingIntent);
    }

    // --- Private Methods

    private static PendingIntent getReminderServicePendingIntent(Context context, long rowId){
        Intent reminderServiceIntent = new Intent(context, ReminderService.class);
        reminderServiceIntent.setAction(String.valueOf(rowId));
        return PendingIntent.getService(context, PENDING_INTENT_REMINDER_REQUEST_CODE,
                reminderServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getRefreshGeofencePendingIntent(Context context){
        Intent intent = new Intent(context, RefreshGeofencesService.class);
        intent.setAction(RefreshGeofencesService.INTENT_ACTION_REFRESH_GEOFENCE_SERVICE);
        return PendingIntent.getService(context, PENDING_INTENT_REFRESH_GEOFENCE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
