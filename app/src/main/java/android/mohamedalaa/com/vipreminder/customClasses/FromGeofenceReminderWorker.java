package android.mohamedalaa.com.vipreminder.customClasses;

import android.content.Context;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.NotificationUtils;
import android.support.annotation.NonNull;

/**
 * Created by Mohamed on 8/8/2018.
 *
 */
public class FromGeofenceReminderWorker {

    /*static final String INPUT_DATA_KEY_REMINDER_ENTITY_ID = "INPUT_DATA_KEY_REMINDER_ENTITY_ID";

    @NonNull
    @Override
    public aResult doWork() {
        aData data = getInputData();
        long rowId = data.getLong(INPUT_DATA_KEY_REMINDER_ENTITY_ID, -1);

        Context context = getApplicationContext();

        DataRepository dataRepository = ((BaseApplication) getApplicationContext()).getRepository();

        ReminderEntity reminderEntity = dataRepository.getReminderEntityByIdSync(rowId);

        if (reminderEntity != null){
            String label = reminderEntity.getLabel();

            // Make the notification
            NotificationUtils.makeReminderNotification(
                    context, context.getString(R.string.entered_an_important_place), label);

            // No need to schedule work manager
            // As this is fired when we depend on places only or depend on both while
            // condition must NOT be met, which means work manager is already scheduled
            // in the ReminderWorker.
        }

        return aResult.SUCCESS;
    }*/
}
