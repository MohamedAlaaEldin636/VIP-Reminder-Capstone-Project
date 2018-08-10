package android.mohamedalaa.com.vipreminder;

import android.arch.lifecycle.LiveData;
import android.mohamedalaa.com.vipreminder.model.database.AppDatabase;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;

import java.util.List;

/**
 * Created by Mohamed on 8/4/2018.
 *
 */
public class DataRepository {

    private static DataRepository sInstance;

    private final AppDatabase mDatabase;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
    }

    static DataRepository getInstance(final AppDatabase database){
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }

        return sInstance;
    }

    public void insertReminderList(List<ReminderEntity> reminderEntityList){
        mDatabase.getReminderDao().insert(reminderEntityList);
    }

    public long insertReminder(ReminderEntity reminderEntity){
        return mDatabase.getReminderDao().insert(reminderEntity);
    }

    public int updateReminder(ReminderEntity reminderEntity){
        return mDatabase.getReminderDao().update(reminderEntity);
    }

    public int deleteReminderEntities(ReminderEntity... reminderEntities){
        return mDatabase.getReminderDao().delete(reminderEntities);
    }

    public List<ReminderEntity> getAllRecipeListSync(){
        return mDatabase.getReminderDao().getAllSync();
    }

    public LiveData<List<ReminderEntity>> getAllRecipeListAsync(){
        return mDatabase.getReminderDao().getAllAsync();
    }

    public ReminderEntity getReminderEntityByIdSync(long id){
        return mDatabase.getReminderDao().getReminderEntityByIdSync(id);
    }

    public ReminderEntity getReminderEntityByWorkRequestUUIDSync(String uuid){
        return mDatabase.getReminderDao().getReminderEntityByWorkRequestUUIDSync(uuid);
    }

    public int deleteAll(){
        return mDatabase.getReminderDao().deleteAll();
    }

}
