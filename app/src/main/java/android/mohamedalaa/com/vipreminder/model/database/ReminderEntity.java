package android.mohamedalaa.com.vipreminder.model.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by Mohamed on 8/3/2018.
 *
 */
@SuppressWarnings("unused")
@Entity(tableName = "ReminderTable")
public class ReminderEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** by date and time picker */
    private long time;

    private String placeName;

    /**
     * Save only ID ( do not save name, latitude, longitude and when need
     *          them connect to google services) due to terms of use.
     * TODO NOTE (1) whenever we save into database we make placeName, latitude and longitude empty
     * so we save id only, but we have put them here so after we get from database we request
     * placeById from google services and put the data of the place in this object to pass it in
     * activities and to show it to user.
     *
     * To prove we don't save it in database any asyncTask that insert or update database
     * it puts empty value to the cannot be saved fields into database, which means
     * we have data only during the life of the application, so it is impossible
     * that data will exceed the 30 days limit.
     */
    private String placeId;

    private String latitude;

    private String longitude;

    /** Either both conditions are met or not -> true means both MUST meet conditions*/
    private boolean dateAndTimeCondition;

    /** label that appears in screen of the appropriate reminder list */
    private String label;

    /** can be empty, it's just for user if he wants to say detailed info, so he doesn't forget */
    private String longDescription;

    /**
     * format of Custom ==> Custom (Every 3 days)
     * no = anyNumber from min 2 to max 1000
     * mode = minutes - hours - days - weeks - months - years
     *
     * one of -> Once, Hourly, Daily, Weekly, Monthly, Yearly or Custom (Every $no $mode) */
    private String repeatMode;

    /**
     * days in which repeat should occur appears as 1st 2 chars in day
     * ex. MO,TU,WE
     *
     * Note
     * if repeat mode is once, then there is no repeat days.
     */
    private String repeatDays;

    /** in case the user wants to add some to-dos/reminders in favourite,
     *      so he can always see them easily */
    private boolean isFavourite;

    /**
     * Reminder marked as done by the following ways
     * 1- from list
     * 2- from notification, when it's time was up.
     */
    private boolean isDone;

    /** Needed in case if user wants to delete or mark as done, so we can cancel the notification */
    private String workRequestUUID;

    public ReminderEntity(long time, String placeName, String placeId, String latitude, String longitude, boolean dateAndTimeCondition, String label, String longDescription, String repeatMode, String repeatDays, boolean isFavourite, boolean isDone, String workRequestUUID) {
        this.time = time;
        this.placeName = placeName;
        this.placeId = placeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateAndTimeCondition = dateAndTimeCondition;
        this.label = label;
        this.longDescription = longDescription;
        this.repeatMode = repeatMode;
        this.repeatDays = repeatDays;
        this.isFavourite = isFavourite;
        this.isDone = isDone;
        this.workRequestUUID = workRequestUUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isDateAndTimeCondition() {
        return dateAndTimeCondition;
    }

    public void setDateAndTimeCondition(boolean dateAndTimeCondition) {
        this.dateAndTimeCondition = dateAndTimeCondition;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(String repeatMode) {
        this.repeatMode = repeatMode;
    }

    public String getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(String repeatDays) {
        this.repeatDays = repeatDays;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getWorkRequestUUID() {
        return workRequestUUID;
    }

    public void setWorkRequestUUID(String workRequestUUID) {
        this.workRequestUUID = workRequestUUID;
    }
}
