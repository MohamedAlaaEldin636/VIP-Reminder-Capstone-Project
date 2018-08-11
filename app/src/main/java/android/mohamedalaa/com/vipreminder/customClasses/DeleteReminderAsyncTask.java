package android.mohamedalaa.com.vipreminder.customClasses;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.AlarmManagerUtils;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.mohamedalaa.com.vipreminder.widgets.ListWidgetProvider;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/7/2018.
 *
 */
public class DeleteReminderAsyncTask extends AsyncTask<ReminderEntity , Void , Void> {

    private WeakReference<Context> contextWeakReference;
    private DataRepository dataRepository;

    public DeleteReminderAsyncTask(Context context, DataRepository dataRepository) {
        this.contextWeakReference = new WeakReference<>(context);
        this.dataRepository = dataRepository;
    }

    @Override
    protected Void doInBackground(ReminderEntity... reminderEntities) {
        List<String> placesIds = new ArrayList<>();
        for (ReminderEntity reminderEntity : reminderEntities){
            // Remove Alarm Manager
            AlarmManagerUtils.cancelAlarmManager(contextWeakReference.get(), reminderEntity.getId());

            if (! StringUtils.isNullOrEmpty(reminderEntity.getPlaceId()) && ! reminderEntity.isDateAndTimeCondition()){
                placesIds.add(reminderEntity.getPlaceId());
            }
        }
        // Remove all associated geofences
        if (placesIds.size() > 0){
            if (contextWeakReference == null || contextWeakReference.get() == null){
                Timber.v("contextWeakReference was null or its context");
            }else {
                new GeofenceStatic().deleteGeofencesByPlacesIds(contextWeakReference.get(), placesIds);
            }
        }

        int number = dataRepository.deleteReminderEntities(reminderEntities);

        Timber.v("Number of rows deleted = " + number);

        // Update the widget list
        if (contextWeakReference != null && contextWeakReference.get() != null){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(contextWeakReference.get());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(contextWeakReference.get().getApplicationContext(), ListWidgetProvider.class));
            ListWidgetProvider.updateAllReminderListAppWidgetsAndNotifyChangeToListView(contextWeakReference.get()
                    .getApplicationContext(), appWidgetManager, appWidgetIds);
        }

        return null;
    }
}
