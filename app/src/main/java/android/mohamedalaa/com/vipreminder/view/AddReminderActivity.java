package android.mohamedalaa.com.vipreminder.view;

import android.app.ActivityManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.customClasses.InsertReminderAsyncTask;
import android.mohamedalaa.com.vipreminder.customClasses.UpdateReminderAsyncTask;
import android.mohamedalaa.com.vipreminder.databinding.ActivityAddReminderBinding;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.mohamedalaa.com.vipreminder.viewModel.ViewModelAddReminderActivity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/5/2018.
 *
 */
public class AddReminderActivity extends AppCompatActivity implements
        ViewModelAddReminderActivity.HostConnectionListener {

    // --- Constants

    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 2847;
    private static final int PLACE_PICKER_REQUEST = 576;

    public static final String INTENT_KEY_REMINDER_ENTITY = "INTENT_KEY_REMINDER_ENTITY";

    public static final String INTENT_KEY_CAME_FROM_WIDGET = "INTENT_KEY_CAME_FROM_WIDGET";

    // --- Private Variables

    private ActivityAddReminderBinding binding;

    private ViewModelAddReminderActivity viewModel;

    private Toast toast;

    private boolean shouldCreateParentStack = false;

    private View.OnClickListener upWithParentClickListener = view -> {
        Intent upIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(upIntent)
                .startActivities();
    };

    private View.OnClickListener upWithoutParent = view -> {
        Intent upIntent = new Intent(this, MainActivity.class);
        NavUtils.navigateUpTo(this, upIntent);
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_reminder);

        // Check if came from widget
        if (getIntent() != null && getIntent().hasExtra(INTENT_KEY_CAME_FROM_WIDGET)){
            shouldCreateParentStack = getIntent().getBooleanExtra(
                    INTENT_KEY_CAME_FROM_WIDGET, false);
        }

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

        // Get extra if exists
        Intent intent = getIntent();
        ReminderEntity reminderEntity = null;
        if (intent != null && intent.hasExtra(INTENT_KEY_REMINDER_ENTITY)){
            reminderEntity = (ReminderEntity) intent.getSerializableExtra(INTENT_KEY_REMINDER_ENTITY);
        }

        viewModel = ViewModelProviders.of(this).get(ViewModelAddReminderActivity.class);
        if (reminderEntity == null){
            viewModel.setupInitialObservableValues(this);
        }else {
            viewModel.setupInitialObservableValues(this, reminderEntity);
            binding.toolbar.setTitle(getString(R.string.edit_reminder));
        }
        binding.setViewModel(viewModel);

        // setup xml clicks
        setupClicks();
    }

    private void setupClicks() {
        // Since this activity will only be launched from main activity so, up button
        // can be the same as onBackPressed,
        // ( which means no need to add the parent code in manifest as well )
        binding.toolbar.setNavigationOnClickListener(shouldCreateParentStack
                ? upWithParentClickListener : upWithoutParent);
        binding.toolbar.inflateMenu(R.menu.menu_add_reminder_activity);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.resetAllAction){
                viewModel.resetAll();

                return true;
            }

            return false;
        });

        // Validate then perform appropriate action
        binding.fab.setOnClickListener(view -> {
            long time = viewModel.getCurrentTimeInMillis();
            String placeId = viewModel.placeIdObservable.get();
            boolean conditionDateAndTime = viewModel.observableConditionDateAndTime.get();
            String label = viewModel.observableLabel.get();

            // Validation operation
            // 1- time passed without place restriction
            // 2- no label found
            if (System.currentTimeMillis() > time){
                if (StringUtils.isNullOrEmpty(placeId)){
                    showToast(getString(R.string.please_set_reminder_for_upcoming_time));
                    return;
                }else if (conditionDateAndTime){
                    showToast(getString(R.string.please_set_reminder_for_upcoming_time));
                    return;
                }
                // else it's ok if time has passed current time since we will depend in this
                // situation on place only
            }
            if (StringUtils.isNullOrEmpty(label)){
                showToast(getString(R.string.please_set_label_to_the_reminder));
                return;
            }

            // Note since we will save to database we put empty in placeName, latitude and longitude
            ReminderEntity reminderEntity = new ReminderEntity(
                    time,
                    "",
                    placeId,
                    "",
                    "",
                    conditionDateAndTime,
                    label,
                    viewModel.observableLongDescription.get(),
                    viewModel.observableRepeat.get(),
                    viewModel.getDaysOfRepeat(binding.daysLinearLayout),
                    false,
                    false,
                    "workRequestUUID");

            DataRepository dataRepository = ((BaseApplication) getApplication()).getRepository();
            if (viewModel.originalReminder != null){
                reminderEntity.setId(viewModel.originalReminder.getId());
                reminderEntity.setFavourite(viewModel.originalReminder.isFavourite());
                reminderEntity.setDone(viewModel.originalReminder.isDone());
                reminderEntity.setWorkRequestUUID(viewModel.originalReminder.getWorkRequestUUID());

                new UpdateReminderAsyncTask(getBaseContext(), dataRepository, reminderEntity, getString(R.string.once),
                        getString(R.string.hourly), getString(R.string.daily), getString(R.string.weekly),
                        getString(R.string.monthly), getString(R.string.yearly)).execute();
            }else {
                new InsertReminderAsyncTask(getBaseContext(), dataRepository, reminderEntity, getString(R.string.once),
                        getString(R.string.hourly), getString(R.string.daily), getString(R.string.weekly),
                        getString(R.string.monthly), getString(R.string.yearly)).execute();
            }

            showToast(getString(R.string.saved_successfully));

            finish();
        });
    }

    // ---- Respond to onRequestPermissionsResult

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_FINE_LOCATION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                viewModel.pickPlace();
            }else {
                // Permission isn't granted, show snackBar.
                Snackbar.make(binding.rootCoordinatorLayout, R.string.on_permission_access_fine_location_denied, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.on_permission_access_fine_location_denied_action, view
                                -> requestAccessFineLocationPermission())
                        .setActionTextColor(Color.GREEN)
                        .show();
            }
        }
    }

    // ---- Respond to onActivityResult

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place == null) {
                Timber.v("No place selected");
                return;
            }

            // Extract the place information from the API
            // Note we can only save the id, due to terms of use of the API
            CharSequence charSequencePlaceName = place.getName();
            CharSequence charSequencePlaceAddress = place.getAddress();
            String placeName = charSequencePlaceName == null ? "" : charSequencePlaceName.toString();
            String placeAddress = charSequencePlaceAddress == null ? "" : charSequencePlaceAddress.toString();
            String placeID = place.getId();

            String toBeShownName;
            if (! StringUtils.isNullOrEmpty(placeName)){
                toBeShownName = placeName;
            }else if (! StringUtils.isNullOrEmpty(placeAddress)){
                toBeShownName = placeAddress;
            }else {
                toBeShownName = getString(R.string.unknown_place_name);
            }

            viewModel.onPlaceSet(toBeShownName, placeID);
        }
    }

    // ---- ViewModelAddReminderActivity.HostConnectionListener Implementation

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void requestAccessFineLocationPermission() {
        ActivityCompat.requestPermissions(AddReminderActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    @Override
    public void showPlacePickerDialog() {
        try {
            // Start a new Activity for the Place Picker API, this will trigger onActivityResult()
            // method when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent intent = builder.build(this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Timber.v(String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (GooglePlayServicesNotAvailableException e) {
            Timber.v(String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Timber.v(String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }

    @Override
    public LinearLayout getDaysLinearLayout() {
        return binding.daysLinearLayout;
    }

    // ---- Private Methods

    private void showToast(String msg){
        if (toast != null){
            toast.cancel();
        }

        toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

}
