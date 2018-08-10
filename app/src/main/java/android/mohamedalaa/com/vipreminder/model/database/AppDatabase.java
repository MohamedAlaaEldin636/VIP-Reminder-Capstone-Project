package android.mohamedalaa.com.vipreminder.model.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Mohamed on 8/4/2018.
 *
 */
@Database(entities = {ReminderEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "app-database.db";
    private static AppDatabase appDatabaseInstance;

    public static AppDatabase getInstance(Context context){
        if (appDatabaseInstance == null){
            appDatabaseInstance = Room.databaseBuilder(context,
                    AppDatabase.class, DATABASE_NAME).build();
            // Note since .allowMainThreadQueries() not there, any CRUD operation should be done
            // in any thread other than Main UI thread, otherwise an error exception will be thrown.
        }

        return appDatabaseInstance;
    }

    // ---- Getting Dao

    public abstract ReminderDao getReminderDao();

}
