package android.mohamedalaa.com.vipreminder.fakeData;

import android.app.Application;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mohamed on 8/5/2018.
 *
 * Used in case reviewer or any other person who viewing this app wants to add fake data in the
 * database, note these fake data won't make notification through alarm manager nor geofence
 */
public class InsertFakeDataInDatabase {

    public static void insertInReminderListDatabase(Application application){
        List<ReminderEntity> reminderEntityList = new ArrayList<>();

        long timeYesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        long timeTomorrow = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        long timeToday = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2);
        ArrayList<Long> timeList = new ArrayList<>();
        timeList.add(timeYesterday);
        timeList.add(timeTomorrow);
        timeList.add(timeToday);
        for (int i=0; i<timeList.size(); i++){
            ReminderEntity entity = new ReminderEntity(timeList.get(i), "","", "", "",
                    false, "Label " + i, "Long Descr 1",
                    "Monthly", "SA,SU",
                    i % 2 == 0, false, "2");

            reminderEntityList.add(entity);
        }


        Runnable runnable = ()
                -> ((BaseApplication) application).getRepository().insertReminderList(reminderEntityList);

        new RunnableAsyncTask(runnable).execute();
    }

}
