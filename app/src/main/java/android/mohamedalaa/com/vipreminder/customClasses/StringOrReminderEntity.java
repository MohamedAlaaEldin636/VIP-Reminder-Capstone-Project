package android.mohamedalaa.com.vipreminder.customClasses;

import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;

import java.io.Serializable;

/**
 * Created by Mohamed on 8/5/2018.
 *
 */
public class StringOrReminderEntity implements Serializable {

    private String title;

    private ReminderEntity reminderEntity;

    public StringOrReminderEntity(String title, ReminderEntity reminderEntity) {
        this.title = title;
        this.reminderEntity = reminderEntity;
    }

    public String getTitle() {
        return title;
    }

    public ReminderEntity getReminderEntity() {
        return reminderEntity;
    }

}
