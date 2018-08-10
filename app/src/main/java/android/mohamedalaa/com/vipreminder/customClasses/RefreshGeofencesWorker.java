package android.mohamedalaa.com.vipreminder.customClasses;

import android.content.Context;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import androidx.work.Worker;
import timber.log.Timber;

/**
 * Created by Mohamed on 8/8/2018.
 *
 */
public class RefreshGeofencesWorker extends Worker {

    @NonNull
    @Override
    public Result doWork() {
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

        return Result.SUCCESS;
    }
}
