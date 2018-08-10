package android.mohamedalaa.com.vipreminder.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Mohamed on 8/4/2018.
 *
 */
public class ViewModelMainActivity extends AndroidViewModel {

    // to survive orientation changes
    public HashMap<String , List<ReminderEntity>> reminderListMap;

    public ViewModelMainActivity(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<ReminderEntity>> getReminderListFromDatabaseAsync() {
        return ((BaseApplication) getApplication())
                .getRepository().getAllRecipeListAsync();
    }

}
