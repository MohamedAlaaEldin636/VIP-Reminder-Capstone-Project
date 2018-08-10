package android.mohamedalaa.com.vipreminder.customClasses;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.mohamedalaa.com.vipreminder.widgets.ListWidgetProvider;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.work.WorkManager;
import timber.log.Timber;

/**
 * Created by Mohamed on 8/8/2018.
 *
 */
public class DeleteAllAsyncTask extends AsyncTask<Void , Void , Void> {

    private WeakReference<Context> contextWeakReference;
    private DataRepository dataRepository;

    public DeleteAllAsyncTask(Context context, DataRepository dataRepository) {
        this.contextWeakReference = new WeakReference<>(context);
        this.dataRepository = dataRepository;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        List<ReminderEntity> reminderEntityList = dataRepository.getAllRecipeListSync();

        WorkManager workManager = WorkManager.getInstance();

        List<String> placesIdsForGeofencesDeletion = new ArrayList<>();
        for (ReminderEntity reminderEntity : reminderEntityList){
            // Delete all work managers
            try {
                workManager.cancelWorkById(UUID.fromString(reminderEntity.getWorkRequestUUID()));
            }catch (Exception e){
                // If there was no UUID
                Timber.v("Error in deleting workManager by UUID -> "
                        + e.getMessage() + "\nReminder rowId -> " + reminderEntity.getId());
            }

            // Delete all geofences
            boolean shouldRegisterGeofence = !StringUtils.isNullOrEmpty(reminderEntity.getPlaceId())
                    && !reminderEntity.isDateAndTimeCondition();

            if (shouldRegisterGeofence){
                placesIdsForGeofencesDeletion.add(reminderEntity.getPlaceId());
            }
        }
        // Delete all geofences
        if (placesIdsForGeofencesDeletion.size() > 0){
            if (contextWeakReference == null || contextWeakReference.get() == null){
                Timber.v("contextWeakReference was null or its context");
            }else {
                new GeofenceStatic().deleteGeofencesByPlacesIds(
                        contextWeakReference.get(), placesIdsForGeofencesDeletion);
            }
        }

        // Delete all rows in database
        int numberOfRowsDeleted = dataRepository.deleteAll();

        Timber.v("Delete all rows in database, and number of rows deleted = "
                + numberOfRowsDeleted);

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
