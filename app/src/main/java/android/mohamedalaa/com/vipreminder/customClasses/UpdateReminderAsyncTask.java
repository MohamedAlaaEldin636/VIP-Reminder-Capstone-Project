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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import timber.log.Timber;

/**
 * Created by Mohamed on 8/6/2018.
 *
 */
public class UpdateReminderAsyncTask extends AsyncTask<Void , Void , Void> {

    private WeakReference<Context> contextWeakReference;
    private DataRepository dataRepository;
    private ReminderEntity reminderEntity;
    private String once;
    private String hourly;
    private String daily;
    private String weekly;
    private String monthly;
    private String yearly;

    public UpdateReminderAsyncTask(Context context, DataRepository dataRepository, ReminderEntity reminderEntity,
                                   String once, String hourly, String daily, String weekly,
                                   String monthly, String yearly) {
        this.contextWeakReference = new WeakReference<>(context);
        this.dataRepository = dataRepository;
        this.reminderEntity = reminderEntity;
        this.once = once;
        this.hourly = hourly;
        this.daily = daily;
        this.weekly = weekly;
        this.monthly = monthly;
        this.yearly = yearly;
    }

    /**
     * VIP VIP VIP VIP
     * Keep every thing in below method and don't make any UI changes
     * as this is called from worker.
     */
    @Override
    protected Void doInBackground(Void... voids) {
        /*
        Flow in Update
        remove any WorkManager task associated to it.
        and re-schedule if it is not done.
         */
        // Remove WorkManager
        try {
            WorkManager.getInstance().cancelWorkById(
                    UUID.fromString(reminderEntity.getWorkRequestUUID()));
        }catch (Exception e){
            // If there was no UUID
            Timber.v("Error in deleting workManager by UUID -> "
                    + e.getMessage() + "\nReminder rowId -> " + reminderEntity.getId());
        }
        // Remove geofence as well.
        if (! StringUtils.isNullOrEmpty(reminderEntity.getPlaceId()) && ! reminderEntity.isDateAndTimeCondition()){
            if (contextWeakReference == null || contextWeakReference.get() == null){
                Timber.v("contextWeakReference was null or its context");
            }else {
                new GeofenceStatic().deleteGeofenceByPlaceId(
                        contextWeakReference.get(), reminderEntity.getPlaceId());
            }
        }

        // Check if days will delay the time set in reminder a little, if not once,
        // and if not depend on place only, ex. when user sets alarm for 1 hour from now daily
        // and excluding today from the notifying operation.
        boolean dependOnPlaceOnly = !StringUtils.isNullOrEmpty(reminderEntity.getPlaceId())
                && !reminderEntity.isDateAndTimeCondition();
        if (!reminderEntity.getRepeatMode().equals(once) && !dependOnPlaceOnly) {
            long timeInMillis = checkDaysIfWillMakeDelay(reminderEntity.getTime(),
                    reminderEntity.getRepeatMode(), reminderEntity.getRepeatDays());
            reminderEntity.setTime(timeInMillis);
        }

        // check if it really is done or should be not done due to repeat mode.
        if (reminderEntity.isDone()){
            // 1st check if it depends on place only or not, for the sake of done flow approach
            long currentTime = System.currentTimeMillis();
            long reminderTime = reminderEntity.getTime();

            if (currentTime < reminderTime){
                // Check repeat, since there is still some time
                String repeatMode = reminderEntity.getRepeatMode();

                if (! repeatMode.equals(once)){
                    // Then update reminder time, and make done false
                    long newReminderTime = getNextReminderTime(
                            reminderTime, repeatMode, reminderEntity.getRepeatDays());
                    reminderEntity.setTime(newReminderTime);

                    reminderEntity.setDone(false);
                }
                // Else then leave it to be done.
            }
            // Else -> Time passed already, so it is a must that it depends on place,
            // so just leave as it is, to be done, so no WorkManager here.
        }

        // re-schedule WorkManager if not done
        if (! reminderEntity.isDone()){
            // First Work Manager
            long initialDelay = reminderEntity.getTime() - System.currentTimeMillis();
            if (initialDelay > 0) {
                // That check if in case that the reminder depend only on place.
                Data inputData = new Data.Builder()
                        .putLong(ReminderWorker.INPUT_DATA_KEY_REMINDER_ENTITY_ID, reminderEntity.getId())
                        .build();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                        .setInputData(inputData)
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .build();
                WorkManager.getInstance().enqueue(workRequest);

                // we set the UUID in database in case we want to delete a reminder,
                // so we can cancel that work.
                UUID uuid = workRequest.getId();
                String workRequestUUID = uuid.toString();
                reminderEntity.setWorkRequestUUID(workRequestUUID);

                int number = dataRepository.updateReminder(reminderEntity);

                Timber.v("Updated after setting workManager, row with id = " + reminderEntity.getId()
                        + ",\nand number of rows updated = " + number);
            }

            /*
            Register geofence, if reminder depends only on place or condition is false with time,
            Since the case of place And Time must meet specifications is handled by the WorkManager
             */
            boolean shouldRegisterGeofence = !StringUtils.isNullOrEmpty(reminderEntity.getPlaceId())
                    && !reminderEntity.isDateAndTimeCondition();
            Timber.v("shouldRegisterGeofence -> " + shouldRegisterGeofence);
            if (shouldRegisterGeofence){
                if (contextWeakReference == null || contextWeakReference.get() == null){
                    Timber.v("WEAK REFERENCE WAS NULL.");
                }else {
                    new GeofenceStatic().registerPlaceById(contextWeakReference.get(),
                            reminderEntity.getPlaceId(), reminderEntity.getId());
                }
            }
        }

        int number = dataRepository.updateReminder(reminderEntity);

        Timber.v("Number of rows updated = " + number);

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

    private long checkDaysIfWillMakeDelay(long reminderTime, String repeatMode, String repeatDays){
        final String lowerCaseRepeatDays = repeatDays.toLowerCase();

        String dayInWeek = new SimpleDateFormat("EEE", Locale.getDefault())
                .format(new Date(reminderTime)).toLowerCase();
        dayInWeek = dayInWeek.substring(0, 2);

        if (! lowerCaseRepeatDays.contains(dayInWeek)){
            return getNextReminderTime(reminderTime, repeatMode, repeatDays);
        }

        return reminderTime;
    }

    private long getNextReminderTime(long reminderTime, String repeatMode, String repeatDays){
        String lowerCaseRepeatDays = repeatDays.toLowerCase();

        boolean continueTheLoop;
        do {
            reminderTime = getNextReminderTimeWithoutLookingToDays(reminderTime, repeatMode);

            String dayInWeek = new SimpleDateFormat("EEE", Locale.getDefault())
                    .format(new Date(reminderTime)).toLowerCase();
            dayInWeek = dayInWeek.substring(0, 2);

            continueTheLoop = ! lowerCaseRepeatDays.contains(dayInWeek);
        }while (continueTheLoop);

        return reminderTime;
    }

    private long getNextReminderTimeWithoutLookingToDays(long reminderTime, String repeatMode) {
        long value = reminderTime;

        // Impossible to be once, as already once is checked before this method
        if (repeatMode.equals(hourly)){
            value += TimeUnit.HOURS
                    .toMillis(1);
        }else if (repeatMode.equals(daily)){
            value += TimeUnit.DAYS
                    .toMillis(1);
        }else if (repeatMode.equals(weekly)){
            value += TimeUnit.DAYS
                    .toMillis(7);
        }else if (repeatMode.equals(monthly)){
            value += TimeUnit.DAYS
                    .toMillis(30);
        }else if (repeatMode.equals(yearly)){
            value += TimeUnit.DAYS
                    .toMillis(365);
        }

        return value;
    }
}
