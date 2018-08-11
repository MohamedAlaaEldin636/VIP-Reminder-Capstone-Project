package android.mohamedalaa.com.vipreminder.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.customClasses.GeofenceStatic;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.AlarmManagerUtils;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/11/2018.
 *
 */
public class RefreshGeofencesService extends IntentService {

    /**
     * Used to be set as intent action so we can cancel any alarm manager that scheduled it.
     */
    public static final String INTENT_ACTION_REFRESH_GEOFENCE_SERVICE = "INTENT_ACTION_REFRESH_GEOFENCE_SERVICE";

    public RefreshGeofencesService() {
        super("RefreshGeofencesService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DataRepository dataRepository = ((BaseApplication) getApplicationContext()).getRepository();

        List<ReminderEntity> reminderEntityList = dataRepository.getAllRecipeListSync();

        // Delete all geofences to ensure there is no duplication, then
        // Loop through all reminder entities and check who can be registered in geofence
        // But first check which row has geofence
        Context context = getApplicationContext();
        List<String> placesIds = new ArrayList<>();
        List<Integer> rowsIds = new ArrayList<>();
        if (reminderEntityList != null && reminderEntityList.size() > 0){
            for (ReminderEntity reminderEntity : reminderEntityList){
                /*
                Register geofence, if reminder depends only on place or condition is false with time,
                Since the case of place And Time must meet specifications is handled by the WorkManager
                 */
                boolean shouldRegisterGeofence = !StringUtils.isNullOrEmpty(reminderEntity.getPlaceId())
                        && !reminderEntity.isDateAndTimeCondition();
                Timber.v("shouldRegisterGeofence -> " + shouldRegisterGeofence);
                if (shouldRegisterGeofence){
                    placesIds.add(reminderEntity.getPlaceId());

                    rowsIds.add(reminderEntity.getId());
                }
            }
        }

        // Delete all, then register all
        if (placesIds.size() > 0){
            // Delete All
            new GeofenceStatic().deleteGeofencesByPlacesIds(context, placesIds);

            // Register all
            /*
            NOTE
            we register one by one, because the pending intent extra differs from one to another.
            if add geofences has a method of List<PendingIntent>, I would 've used it.
             */
            for (int i=0; i<placesIds.size(); i++){
                new GeofenceStatic().registerPlaceById(
                        context, placesIds.get(i), rowsIds.get(i));
            }
        }

        // Since we registered all geofences now, so cancel any alarm manager that will use this
        // service, and re-schedule new 30 days for all.
        // -- Cancel All
        AlarmManagerUtils.cancelRefreshGeofenceAlarmManager(context);
        // -- Schedule All
        AlarmManagerUtils.setRefreshGeofenceAlarm(context, GeofenceStatic.GEOFENCE_TIMEOUT);

    }
}
