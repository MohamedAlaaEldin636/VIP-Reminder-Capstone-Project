package android.mohamedalaa.com.vipreminder.widgets;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.CalenderUtils;
import android.mohamedalaa.com.vipreminder.utils.NetworkUtils;
import android.mohamedalaa.com.vipreminder.utils.SharedPrefUtils;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.mohamedalaa.com.vipreminder.view.AddReminderActivity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/8/2018.
 *
 */
public class WidgetServiceReminders extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Timber.v("onGetViewFactory(Intent intent) -> Is Called");

        String stringOfAppWidgetId = intent.getAction();
        int appWidgetId = -1;
        try {
            appWidgetId = Integer.parseInt(stringOfAppWidgetId);
        }catch (Exception e){
            // In case of string cannot be int
            Timber.v(e.getMessage());
        }

        return new RemindersRemoteViewsFactory(
                getApplication(),
                this.getApplicationContext(),
                appWidgetId);
    }

}

class RemindersRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Application application;
    private final Context context;
    private final int appWidgetId;

    private List<ReminderEntity> reminderEntityList;

    RemindersRemoteViewsFactory(Application application, Context context, int appWidgetId) {
        this.application = application;
        this.context = context;
        this.appWidgetId = appWidgetId;
    }

    @Override
    public void onCreate() {}

    /**
     * Called when notifyDataSetChanged() is triggered on the remote adapter. This allows
     * a RemoteViewsFactory to respond to data changes by updating any internal references.
     * Note: expensive tasks can be safely performed synchronously within this method.
     * In the interim, the old data will be displayed within the widget.
     */
    @Override
    public void onDataSetChanged() {
        // Get list from database
        DataRepository dataRepository = ((BaseApplication) application).getRepository();
        List<ReminderEntity> reminderEntityList = dataRepository.getAllRecipeListSync();

        if (reminderEntityList != null && reminderEntityList.size() > 0){
            String category = SharedPrefUtils.getWidgetChosenCategory(context, appWidgetId);
            reminderEntityList = getReminderEntityListByCategory(reminderEntityList, category);

            Timber.v("reminder list -> " + reminderEntityList.size());

            if (reminderEntityList.size() > 0){
                // check places names
                reminderEntityList = getReminderEntityListWithPlacesDataIfExists(
                        reminderEntityList);



                Timber.v("reminder list -> " + reminderEntityList.size());

                // Assign to the global variable
                this.reminderEntityList = new ArrayList<>(reminderEntityList);
            }else {
                this.reminderEntityList = new ArrayList<>();
            }
        }else {
            this.reminderEntityList = null;
        }
    }

    @Override
    public void onDestroy() {
        reminderEntityList = null;
    }

    @Override
    public int getCount() {
        return reminderEntityList == null ? 0 : reminderEntityList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (reminderEntityList == null || position >= reminderEntityList.size()){
            return null;
        }

        Timber.v("Inside get view at");

        // setup data in remoteViews
        ReminderEntity reminderEntity = reminderEntityList.get(position);

        // Setup RemoteViews
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(), R.layout.item_widget_category);

        // label
        remoteViews.setTextViewText(R.id.labelTextView,
                reminderEntity.getLabel());
        // place
        if (! StringUtils.isNullOrEmpty(reminderEntity.getPlaceId())){
            remoteViews.setViewVisibility(R.id.placeTextView, View.VISIBLE);

            remoteViews.setTextViewText(R.id.placeTextView, reminderEntity.getPlaceName());
        }else {
            remoteViews.setViewVisibility(R.id.placeTextView, View.GONE);
        }
        // date and time
        String[] bothDateAndTime = new SimpleDateFormat("dd MMM, yyyy-hh:mm a", Locale.getDefault())
                .format(new Date(reminderEntity.getTime())).split("-");
        String stringDate = bothDateAndTime[0];
        String stringTime = bothDateAndTime[1];
        String textOfDateAndTime = stringDate + "  at  " + stringTime;
        remoteViews.setTextViewText(R.id.timeTextView,
                textOfDateAndTime);
        // repeat
        remoteViews.setTextViewText(R.id.repeatTextView,
                reminderEntity.getRepeatMode());

        // perform click to launch that specific recipe ingredient
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(AddReminderActivity.INTENT_KEY_REMINDER_ENTITY, reminderEntity);
        fillInIntent.putExtra(AddReminderActivity.INTENT_KEY_CAME_FROM_WIDGET, true);
        remoteViews.setOnClickFillInIntent(R.id.rootLinearLayout, fillInIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // Treat all items in the ListView the same
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    // --- Private Methods

    private List<ReminderEntity> getReminderEntityListByCategory(List<ReminderEntity> list,
                                                                 String category){
        if (category.equals(context.getString(R.string.all))){
            return list;
        }

        List<ReminderEntity> upcomingList = new ArrayList<>();
        List<ReminderEntity> historyList = new ArrayList<>();

        final long currentTime = System.currentTimeMillis();

        for (ReminderEntity reminder : list) {
            if (reminder.isDone()){
                historyList.add(reminder);
                continue;
            }

            String placeId = reminder.getPlaceId();
            boolean alwaysUpcoming = !StringUtils.isNullOrEmpty(placeId)
                    && !reminder.isDateAndTimeCondition();

            if (alwaysUpcoming) {
                upcomingList.add(reminder);
            } else {
                long time = reminder.getTime();

                if (currentTime > time) {
                    // Task is in the past
                    historyList.add(reminder);
                } else {
                    upcomingList.add(reminder);
                }
            }
        }

        // -- Upcoming
        List<ReminderEntity> todayUpcomingList = new ArrayList<>();
        List<ReminderEntity> tomorrowUpcomingList = new ArrayList<>();
        List<ReminderEntity> upcomingUpcomingList = new ArrayList<>();
        for (ReminderEntity reminder : upcomingList){
            long time = reminder.getTime();

            int number = CalenderUtils.getCurrentDayMinusProvidedOne(time);
            if (number == 0 && currentTime < time){
                // task is in today title
                todayUpcomingList.add(reminder);
            }else if (number == -1){
                // task is in tomorrow title
                tomorrowUpcomingList.add(reminder);
            }else {
                // task is in upcoming title
                upcomingUpcomingList.add(reminder);
            }
        }

        // -- History
        List<ReminderEntity> doneHistoryList = new ArrayList<>();
        List<ReminderEntity> overdueHistoryList = new ArrayList<>();
        for (ReminderEntity reminder : historyList){
            if (reminder.isDone()){
                doneHistoryList.add(reminder);
            }else {
                overdueHistoryList.add(reminder);
            }
        }

        if (category.equals(context.getString(R.string.today))){
            return todayUpcomingList;
        }else if (category.equals(context.getString(R.string.tomorrow))){
            return tomorrowUpcomingList;
        }else if (category.equals(context.getString(R.string.upcoming))){
            return upcomingUpcomingList;
        }else if (category.equals(context.getString(R.string.done))){
            return doneHistoryList;
        }else {
            // Then it is overdue
            return overdueHistoryList;
        }
    }

    private List<ReminderEntity> getReminderEntityListWithPlacesDataIfExists(List<ReminderEntity> reminderEntityList){
        // Get valid list of places ids
        List<String> listOfPlacesIds = new ArrayList<>();
        List<Boolean> putPlaceList = new ArrayList<>();
        for (ReminderEntity reminderEntity : reminderEntityList){
            if (StringUtils.isNullOrEmpty(reminderEntity.getPlaceId())){
                putPlaceList.add(false);
            }else {
                listOfPlacesIds.add(reminderEntity.getPlaceId());

                putPlaceList.add(true);
            }
        }

        // to tell user there is place but there is no internet connection
        if (! NetworkUtils.isCurrentlyOnline(context)){
            List<ReminderEntity> newList = new ArrayList<>();
            for (int i=0; i<reminderEntityList.size(); i++){
                ReminderEntity reminderEntity = reminderEntityList.get(i);

                if (putPlaceList.get(i)){
                    reminderEntity.setPlaceName(context.getString(R.string.place) + " -> " +
                            context.getString(R.string.no_internet_connection));
                }

                newList.add(reminderEntity);
            }

            return newList;
        }

        if (listOfPlacesIds.size() > 0){
            // get google api client
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Places.GEO_DATA_API)
                    .build();
            googleApiClient.blockingConnect();

            // get places
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient,
                    listOfPlacesIds.toArray(new String[listOfPlacesIds.size()]));
            @NonNull PlaceBuffer places = placeResult.await();

            // construct the new list
            List<ReminderEntity> newList = new ArrayList<>();
            int counterForPlaces = 0;
            for (int i=0; i<reminderEntityList.size(); i++){
                ReminderEntity reminderEntity = reminderEntityList.get(i);

                if (putPlaceList.get(i)){
                    CharSequence name = places.get(counterForPlaces++).getName();

                    reminderEntity.setPlaceName(name == null
                            ? context.getString(R.string.unknown) : name.toString());
                }

                newList.add(reminderEntity);
            }

            places.release();

            return newList;
        }else {
            return reminderEntityList;
        }
    }

}

