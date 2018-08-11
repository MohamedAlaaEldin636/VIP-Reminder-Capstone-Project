package android.mohamedalaa.com.vipreminder.customClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.mohamedalaa.com.vipreminder.services.FromGeofenceReminderService;

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

        // No need to make any initial delay as we want it to run immediately.
        Intent reminderServiceIntent = new Intent(context, FromGeofenceReminderService.class);
        reminderServiceIntent.setAction(String.valueOf(rowId));
        context.startService(reminderServiceIntent);

        Timber.v("inside onReceive of geofence broadcast, And rowId == " + rowId);
    }
}
