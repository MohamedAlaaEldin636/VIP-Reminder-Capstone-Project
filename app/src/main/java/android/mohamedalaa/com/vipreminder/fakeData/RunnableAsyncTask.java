package android.mohamedalaa.com.vipreminder.fakeData;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/5/2018.
 *
 */
public class RunnableAsyncTask extends AsyncTask<Void , Void , Void> {

    private ArrayList<Runnable> runnableArrayList;

    RunnableAsyncTask(Runnable... runnables) {
        runnableArrayList = new ArrayList<>();
        runnableArrayList.addAll(Arrays.asList(runnables));
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (runnableArrayList != null && runnableArrayList.size() > 0){
            for (Runnable runnable : runnableArrayList){
                runnable.run();
            }
        }else {
            Timber.v("No runnables there");
        }

        Timber.v("Done With Background Operations");

        return null;
    }
}
