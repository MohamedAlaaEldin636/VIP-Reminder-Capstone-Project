package android.mohamedalaa.com.vipreminder.services;

import android.content.Context;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.customClasses.GeofenceStatic;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.AlarmManagerUtils;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/11/2018.
 *
 */
public class JobRefreshGeofencesService extends JobService {

    /**
     * Used to be set as intent action so we can cancel any alarm manager that scheduled it.
     */
    public static final String INTENT_ACTION_REFRESH_GEOFENCE_SERVICE = "INTENT_ACTION_REFRESH_GEOFENCE_SERVICE";

    /**
     * @return 	true if your service will continue running, using a separate thread when
     * appropriate. false means that this job has completed its work.
     */
    @Override
    public boolean onStartJob(JobParameters job) {
        new Thread(() -> {
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
        }).start();

        jobFinished(job, false);

        return true;
    }

    /**
     * link -> https://developer.android.com/reference/android/app/job/JobService#onStopJob(android.app.job.JobParameters)
     *
     * @return true to indicate to the JobManager whether you'd like to reschedule this job based on
     * the retry criteria provided at job creation-time; or false to end the job entirely.
     * Regardless of the value returned, your job must stop executing.
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

}
