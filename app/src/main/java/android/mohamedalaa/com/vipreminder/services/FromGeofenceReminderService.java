package android.mohamedalaa.com.vipreminder.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.NotificationUtils;
import android.support.annotation.Nullable;

/**
 * Created by Mohamed on 8/11/2018.
 *
 */
public class FromGeofenceReminderService extends IntentService {

    public FromGeofenceReminderService() {
        super("FromGeofenceReminderService");
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

        DataRepository dataRepository = ((BaseApplication) getApplicationContext()).getRepository();

        ReminderEntity reminderEntity = dataRepository.getReminderEntityByIdSync(rowId);

        if (reminderEntity != null){
            String label = reminderEntity.getLabel();

            // Make the notification
            NotificationUtils.makeReminderNotification(
                    context, context.getString(R.string.entered_an_important_place), label);

            // No need to schedule alarm manager
            // As this is fired when we depend on places only or depend on both while
            // condition must NOT be met, which means work manager is already scheduled
            // in the ReminderWorker.
        }
    }
}
