package android.mohamedalaa.com.vipreminder.customClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import timber.log.Timber;

/**
 * Created by Mohamed on 8/8/2018.
 *
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    // --- Constants

    public static final String INTENT_KEY_ROW_ID = "INTENT_KEY_ROW_ID";

    @Override
    public void onReceive(Context context, Intent intent) {

        int rowId = -1;
        if (intent != null && intent.hasExtra(INTENT_KEY_ROW_ID)){
            rowId = intent.getIntExtra(INTENT_KEY_ROW_ID, rowId);
        }

        // No need to make any initial delay as we want it to run immediately.
        Data inputData = new Data.Builder()
                .putLong(FromGeofenceReminderWorker.INPUT_DATA_KEY_REMINDER_ENTITY_ID, rowId)
                .build();
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(FromGeofenceReminderWorker.class)
                .setInputData(inputData)
                .build();
        WorkManager.getInstance().enqueue(workRequest);

        Timber.v("inside onReceive of geofence broadcast, And rowId == " + rowId);
    }
}
