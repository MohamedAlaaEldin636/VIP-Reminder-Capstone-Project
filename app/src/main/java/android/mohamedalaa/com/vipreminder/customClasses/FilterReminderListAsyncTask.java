package android.mohamedalaa.com.vipreminder.customClasses;

import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.CalenderUtils;
import android.mohamedalaa.com.vipreminder.utils.ConstantsUtils;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mohamed on 8/4/2018.
 *
 */
public class FilterReminderListAsyncTask
        extends AsyncTask<Void , Void , HashMap<String , List<ReminderEntity>>> {

    private GoogleApiClient googleApiClient;
    private boolean isCurrentlyOnline;
    private String noInternetConnection;
    private FilterReminderListAsyncTaskListener listener;
    private List<ReminderEntity> allReminderList;

    public FilterReminderListAsyncTask(GoogleApiClient googleApiClient,
                                       boolean isCurrentlyOnline,
                                       String noInternetConnection,
                                       FilterReminderListAsyncTaskListener listener,
                                       List<ReminderEntity> allReminderList) {
        this.googleApiClient = googleApiClient;
        this.isCurrentlyOnline = isCurrentlyOnline;
        this.noInternetConnection = noInternetConnection;
        this.listener = listener;
        this.allReminderList = allReminderList;
    }

    /**
     * Flow
     * check if is done, if not
     * then check if there is place,
     * then time,
     * if time passed -> check condition with time
     * if both must meet condition -> then it will be in the history otherwise,
     * it will be in the upcoming
     */
    @Override
    protected HashMap<String , List<ReminderEntity>> doInBackground(Void... aVoids) {
        // Get place data from google services since we can have only ID in database
        setupPlacesData();

        // filter into 3 categories
        // 1- Upcoming
        // 2- Favourites
        // 3- History
        List<ReminderEntity> upcomingList = new ArrayList<>();
        List<ReminderEntity> favouritesList = new ArrayList<>();
        List<ReminderEntity> historyList = new ArrayList<>();

        final long currentTime = System.currentTimeMillis();

        for (ReminderEntity reminder : allReminderList) {
            boolean isFav = reminder.isFavourite();
            if (isFav){
                favouritesList.add(reminder);
            }

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

        ////////////////////////////////////////////////////////////////////////////////////////////
        //      Enter More detailed filtering
        ////////////////////////////////////////////////////////////////////////////////////////////

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

        // -- Favourite
        List<ReminderEntity> todayFavouriteList = new ArrayList<>();
        List<ReminderEntity> tomorrowFavouriteList = new ArrayList<>();
        List<ReminderEntity> upcomingFavouriteList = new ArrayList<>();
        List<ReminderEntity> doneFavouriteList = new ArrayList<>();
        List<ReminderEntity> overdueFavouriteList = new ArrayList<>();
        for (ReminderEntity reminder : favouritesList){

            if (reminder.isDone()){
                doneFavouriteList.add(reminder);
            }else {
                long time = reminder.getTime();

                if (currentTime > time){
                    // In the past
                    overdueFavouriteList.add(reminder);
                }else {
                    int number = CalenderUtils.getCurrentDayMinusProvidedOne(time);

                    if (number == 0){
                        // task is in today title
                        todayFavouriteList.add(reminder);
                    }else if (number == -1){
                        // task is in tomorrow title
                        tomorrowFavouriteList.add(reminder);
                    }else {
                        // task is in upcoming title
                        upcomingFavouriteList.add(reminder);
                    }
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //      Exit More detailed filtering
        ////////////////////////////////////////////////////////////////////////////////////////////

        HashMap<String , List<ReminderEntity>> reminderListMap = new HashMap<>();
        reminderListMap.put(ConstantsUtils.MAP_UPCOMING_LIST, upcomingList);
        reminderListMap.put(ConstantsUtils.MAP_TODAY_UPCOMING_LIST, todayUpcomingList);
        reminderListMap.put(ConstantsUtils.MAP_TOMORROW_UPCOMING_LIST, tomorrowUpcomingList);
        reminderListMap.put(ConstantsUtils.MAP_UPCOMING_UPCOMING_LIST, upcomingUpcomingList);

        reminderListMap.put(ConstantsUtils.MAP_FAVOURITE_LIST, favouritesList);
        reminderListMap.put(ConstantsUtils.MAP_TODAY_FAVOURITE_LIST, todayFavouriteList);
        reminderListMap.put(ConstantsUtils.MAP_TOMORROW_FAVOURITE_LIST, tomorrowFavouriteList);
        reminderListMap.put(ConstantsUtils.MAP_UPCOMING_FAVOURITE_LIST, upcomingFavouriteList);
        reminderListMap.put(ConstantsUtils.MAP_DONE_FAVOURITE_LIST, doneFavouriteList);
        reminderListMap.put(ConstantsUtils.MAP_OVERDUE_FAVOURITE_LIST, overdueFavouriteList);

        reminderListMap.put(ConstantsUtils.MAP_HISTORY_LIST, historyList);
        reminderListMap.put(ConstantsUtils.MAP_DONE_HISTORY_LIST, doneHistoryList);
        reminderListMap.put(ConstantsUtils.MAP_OVERDUE_HISTORY_LIST, overdueHistoryList);

        return reminderListMap;
    }

    @Override
    protected void onPostExecute(HashMap<String , List<ReminderEntity>> reminderListMap) {
        if (listener != null){
            listener.onFilterDone(reminderListMap);
        }
    }

    // ----- Own Listener

    public interface FilterReminderListAsyncTaskListener {

        void onFilterDone(HashMap<String , List<ReminderEntity>> reminderListMap);

    }

    // ---- Private Methods

    private void setupPlacesData(){
        if (isCurrentlyOnline && ! googleApiClient.isConnected()){
            // To ensure it is connected.
            googleApiClient.blockingConnect();
        }
        if (isCurrentlyOnline && googleApiClient.isConnected()){
            List<String> listOfPlacesIds = new ArrayList<>();
            for (ReminderEntity reminder : allReminderList){
                String placeId = reminder.getPlaceId();

                if (! StringUtils.isNullOrEmpty(placeId)){
                    listOfPlacesIds.add(placeId);
                }
            }

            if (listOfPlacesIds.size() > 0){
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient,
                        listOfPlacesIds.toArray(new String[listOfPlacesIds.size()]));
                // Since we are in background thread, it's better to get the data Sync
                @NonNull PlaceBuffer places = placeResult.await();

                List<String> listPlaceName = new ArrayList<>();
                List<String> listPlaceLatitude = new ArrayList<>();
                List<String> listPlaceLongitude = new ArrayList<>();
                for (Place place : places){
                    listPlaceName.add(
                            place.getName() == null ? "" : place.getName().toString());
                    listPlaceLatitude.add(
                            place.getLatLng() == null
                                    ? ""
                                    : String.valueOf(place.getLatLng().latitude));
                    listPlaceLongitude.add(
                            place.getLatLng() == null
                                    ? ""
                                    : String.valueOf(place.getLatLng().longitude));
                }

                int listCounter = 0;
                List<ReminderEntity> withPlacesDataAllReminderList = new ArrayList<>();
                for (ReminderEntity reminder : allReminderList){
                    String placeId = reminder.getPlaceId();

                    if (! StringUtils.isNullOrEmpty(placeId)){
                        reminder.setPlaceName(listPlaceName.get(listCounter));
                        reminder.setLatitude(listPlaceLatitude.get(listCounter));
                        reminder.setLongitude(listPlaceLongitude.get(listCounter));

                        listCounter++;
                    }

                    withPlacesDataAllReminderList.add(reminder);
                }

                places.release();

                allReminderList = new ArrayList<>(withPlacesDataAllReminderList);
            }
        }else {
            // we put in the place name noInternetConnection to show to user that there is place
            // provided in the reminder but we can't know what it is because there is no internet
            // connection.
            List<ReminderEntity> withPlacesDataAllReminderList = new ArrayList<>();
            for (ReminderEntity reminder : allReminderList){
                String placeId = reminder.getPlaceId();

                if (! StringUtils.isNullOrEmpty(placeId)){
                    reminder.setPlaceName(noInternetConnection);
                }

                withPlacesDataAllReminderList.add(reminder);
            }

            allReminderList = new ArrayList<>(withPlacesDataAllReminderList);
        }
    }

}
