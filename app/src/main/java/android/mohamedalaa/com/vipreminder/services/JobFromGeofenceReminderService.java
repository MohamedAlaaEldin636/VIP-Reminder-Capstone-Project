package android.mohamedalaa.com.vipreminder.services;

import android.content.Context;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.NotificationUtils;
import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Mohamed on 8/11/2018.
 *
 *
 */
public class JobFromGeofenceReminderService extends JobService {

    public static final String GENERAL_TAG = "JOB_FROM_GEOFENCE_REMINDER_SERVICE_GENERAL_TAG";

    public static final String BUNDLE_KEY_STRING_OF_ROW_ID = "BUNDLE_KEY_STRING_OF_ROW_ID";

    /**
     * @return 	true if your service will continue running, using a separate thread when
     * appropriate. false means that this job has completed its work.
     */
    @Override
    public boolean onStartJob(JobParameters job) {
        final Bundle extras = job.getExtras();
        new Thread(() -> {
            long rowId = -1;
            if (extras != null){
                // Used action not any other extra so that, I can cancel it with alarmManager.cancel();
                String stringOfRowId = extras.getString(BUNDLE_KEY_STRING_OF_ROW_ID);

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
                // in the ReminderService.
            }
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
