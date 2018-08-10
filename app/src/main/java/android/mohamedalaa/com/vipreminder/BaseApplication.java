package android.mohamedalaa.com.vipreminder;

import android.app.Application;
import android.mohamedalaa.com.vipreminder.model.database.AppDatabase;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/3/2018.
 *
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Planting timber tree
        Timber.plant(new Timber.DebugTree());

        // TODO NOTE (2) If u wanna add fake data instead of performing ui flow yourself.
        //InsertFakeDataInDatabase.insertInReminderListDatabase(this);
    }

    // ---- Database && Data Repository

    private AppDatabase getDatabase(){
        return AppDatabase.getInstance(this);
    }

    public DataRepository getRepository(){
        return DataRepository.getInstance(getDatabase());
    }

}
