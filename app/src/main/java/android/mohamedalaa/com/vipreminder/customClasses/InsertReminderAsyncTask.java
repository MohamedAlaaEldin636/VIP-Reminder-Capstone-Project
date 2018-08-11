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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/6/2018.
 *
 *
 * Notes
 * 1- to Convert UUID to and from String
 *      String s = uuid.toString();
 *      UUID u = UUID.fromString(s);
 */
public class InsertReminderAsyncTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> contextWeakReference;
    private DataRepository dataRepository;
    private ReminderEntity reminderEntity;
    private String once;
    private String hourly;
    private String daily;
    private String weekly;
    private String monthly;
    private String yearly;

    public InsertReminderAsyncTask(Context context, DataRepository dataRepository, ReminderEntity reminderEntity,
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

    @Override
    protected Void doInBackground(Void... voids) {
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

        long id = dataRepository.insertReminder(reminderEntity);

        Timber.v("Inserted row Id = " + id);

        long initialDelay = reminderEntity.getTime() - System.currentTimeMillis();
        if (initialDelay > 0) {
            // That check if in case that the reminder depend only on place.
            AlarmManagerUtils.setReminderAlarm(contextWeakReference.get(), id, initialDelay);
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
                        reminderEntity.getPlaceId(), (int) id);
            }
        }

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
        final String lowerCaseRepeatDays = repeatDays.toLowerCase();

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
