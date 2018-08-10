package android.mohamedalaa.com.vipreminder.view;

import android.app.ActivityManager;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.customClasses.DeleteAllAsyncTask;
import android.mohamedalaa.com.vipreminder.customClasses.FilterReminderListAsyncTask;
import android.mohamedalaa.com.vipreminder.databinding.ActivityMainBinding;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.NetworkUtils;
import android.mohamedalaa.com.vipreminder.utils.UnitsConversionsUtils;
import android.mohamedalaa.com.vipreminder.view.adapters.CustomPagerAdapter;
import android.mohamedalaa.com.vipreminder.viewModel.ViewModelMainActivity;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        FilterReminderListAsyncTask.FilterReminderListAsyncTaskListener ,
        GoogleApiClient.ConnectionCallbacks ,
        GoogleApiClient.OnConnectionFailedListener {

    private ActivityMainBinding binding;

    private ViewModelMainActivity viewModel;

    private CustomPagerAdapter pagerAdapter;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // When we press on recent apps without below code we will see app name as black color
        // and it's background is primary, by performing the below code, the background
        // becomes primaryDark and title becomes white, and that's better since the whole app
        // toolbar titles are in white.
        // Note below api 21 it is auto colored so no need to do it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setTaskDescription(new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    null,
                    ContextCompat.getColor(this, R.color.lightBlueColorPrimaryDark)));
        }

        viewModel = ViewModelProviders.of(this).get(ViewModelMainActivity.class);

        // Connect to google play service via client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        // observe view model
        observeViewModel();

        // setup xml views
        setupXmlViews();

        // setup toolbar
        setupToolbar();
    }

    private void setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_main_activity);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.deleteAllAction){
                DataRepository dataRepository = ((BaseApplication) getApplication()).getRepository();
                new DeleteAllAsyncTask(getBaseContext(), dataRepository).execute();

                return true;
            }

            return false;
        });
    }

    private void observeViewModel() {
        viewModel.getReminderListFromDatabaseAsync().observe(this, reminderEntityList -> {
            binding.loadingFrameLayout.setVisibility(View.VISIBLE);

            if (reminderEntityList != null){
                // Make filtering
                Runnable runnable = () -> new FilterReminderListAsyncTask(
                        googleApiClient,
                        NetworkUtils.isCurrentlyOnline(getBaseContext()),
                        getString(R.string.no_internet_connection),
                        MainActivity.this,
                        reminderEntityList)
                        .execute();

                if (googleApiClient.isConnected()){
                    runnable.run();
                }else {
                    // We wait 250 milliSecond to wait for client if it won't be connected
                    // then start asyncTask filter anyway
                    new Handler().postDelayed(runnable, 250);
                }
            }
        });
    }

    private void setupXmlViews() {
        ArrayList<String> pagesTitles = new ArrayList<>();
        pagesTitles.add(getString(R.string.upcoming));
        pagesTitles.add(getString(R.string.favourite));
        pagesTitles.add(getString(R.string.history));
        pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), pagesTitles);
        binding.viewPager.setPageMargin(UnitsConversionsUtils.dpToPx(4, this));
        binding.viewPager.setPageMarginDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.lightBlueColorPrimary)));
        binding.viewPager.setAdapter(pagerAdapter);
        // In case orientation change occurred
        pagerAdapter.swapReminderListMap(viewModel.reminderListMap);

        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    // ---- Implement FilterReminderListAsyncTaskListener Methods

    @Override
    public void onFilterDone(HashMap<String, List<ReminderEntity>> reminderListMap) {
        binding.loadingFrameLayout.setVisibility(View.GONE);

        // In case orientation change will occur
        viewModel.reminderListMap = reminderListMap;

        pagerAdapter.swapReminderListMap(reminderListMap);
    }

    // ---- GoogleApiClient.ConnectionCallbacks , GoogleApiClient.OnConnectionFailedListener

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Timber.v("API Client Connection Successful!");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Timber.v("API Client Connection Suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Timber.v("API Client Connection Failed!");
    }
}
