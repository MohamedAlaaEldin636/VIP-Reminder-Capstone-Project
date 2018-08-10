package android.mohamedalaa.com.vipreminder.viewModel;

import android.app.Application;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableFloat;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.customClasses.CustomTextWatcher;
import android.mohamedalaa.com.vipreminder.customClasses.RepeatPickerDialog;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Mohamed on 8/5/2018.
 *
 */
public class ViewModelAddReminderActivity extends AndroidViewModel implements
        DatePickerDialog.OnDateSetListener ,
        TimePickerDialog.OnTimeSetListener ,
        RepeatPickerDialog.OnRepeatSetListener {

    public ObservableField<String> observableDate = new ObservableField<>();
    public ObservableField<String> observableTime = new ObservableField<>();
    public ObservableField<String> observablePlace = new ObservableField<>();
    public ObservableBoolean observableConditionDateAndTime = new ObservableBoolean();
    public ObservableField<String> observableRepeat = new ObservableField<>();
    public ObservableField<String> observableLabel = new ObservableField<>();
    public ObservableField<String> observableLongDescription = new ObservableField<>();

    public ObservableBoolean observableDaysLinearLayoutVisibility = new ObservableBoolean();

    public ObservableFloat observableConditionAlpha = new ObservableFloat();
    public ObservableBoolean observableSwitchEnabled = new ObservableBoolean();

    private HostConnectionListener listener;

    private long currentTimeInMillis;

    /** Null means it is create new reminder, else we are editing a reminder */
    public ReminderEntity originalReminder = null;

    /**
     * Only used if place name doesn't equal context.getString(R.string.nothing_selected)
     */
    public ObservableField<String> placeIdObservable = new ObservableField<>("");

    public ViewModelAddReminderActivity(@NonNull Application application) {
        super(application);

        observeOwnObservers();
    }

    private void observeOwnObservers() {
        placeIdObservable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                String stringPlaceId = placeIdObservable.get();

                if (StringUtils.isNullOrEmpty(stringPlaceId)){
                    observableConditionAlpha.set(0.5f);
                    observableSwitchEnabled.set(false);
                }else {
                    observableConditionAlpha.set(1f);
                    observableSwitchEnabled.set(true);
                }
            }
        });
    }

    public void setupInitialObservableValues(HostConnectionListener listener){
        this.listener = listener;

        // ---- Check if this is orientation change
        if (! StringUtils.isNullOrEmpty(observableDate.get())){
            return;
        }

        forceSetupInitialObservableValues();
    }

    public void setupInitialObservableValues(HostConnectionListener listener, ReminderEntity originalReminder) {
        this.listener = listener;
        this.originalReminder = originalReminder;

        // ---- Check if this is orientation change
        if (! StringUtils.isNullOrEmpty(observableDate.get())){
            return;
        }

        forceSetupInitialObservableValues(originalReminder);
    }

        // ---- Xml Direct Variables

    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener
            = ((buttonView, isChecked) -> observableConditionDateAndTime.set(isChecked));

    public CustomTextWatcher customTextWatcherLabel = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);

            observableLabel.set(s.toString());
        }
    };

    public CustomTextWatcher customTextWatcherLongDescription = new CustomTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);

            observableLongDescription.set(s.toString());
        }
    };

    public View.OnClickListener daysOnClickListener = view -> {
        if (view.getAlpha() == 1f){
            view.setAlpha(0.5f);
        }else {
            view.setAlpha(1f);
        }
    };

    // ---- Xml Direct Methods

    public void pickDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeInMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (listener != null){
            DatePickerDialog dialog = new DatePickerDialog(
                    listener.getContext(),this, year, month, day);
            dialog.show();
        }
    }

    public void pickTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeInMillis);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (listener != null){
            TimePickerDialog dialog = new TimePickerDialog(
                    listener.getContext(), this, hourOfDay, minute, false);
            dialog.show();
        }
    }

    public void pickPlace(){
        if (listener != null){
            if (ActivityCompat.checkSelfPermission(listener.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                listener.requestAccessFineLocationPermission();
            }else {
                // Permission is granted
                listener.showPlacePickerDialog();
            }
        }
    }

    public void toggleConditionOfDateAndTime(){
        if (observableSwitchEnabled.get()){
            observableConditionDateAndTime.set(
                    ! observableConditionDateAndTime.get());
        }
    }

    public void pickRepeat(){
        if (listener != null){
            RepeatPickerDialog dialog = new RepeatPickerDialog(
                    listener.getContext(), this, observableRepeat.get());
            dialog.show();
        }
    }

    // ---- Implemented Methods

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeInMillis);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(year, month, dayOfMonth, hourOfDay, minute);
        currentTimeInMillis = calendar.getTimeInMillis();

        observableDate.set(new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                .format(new Date(currentTimeInMillis)));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeInMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, dayOfMonth, hourOfDay, minute);
        currentTimeInMillis = calendar.getTimeInMillis();

        observableTime.set(new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(currentTimeInMillis)));
    }

    @Override
    public void onRepeatSet(String stringRepeat) {
        if (stringRepeat.equals(getApplication().getApplicationContext().getString(R.string.once))){
            observableDaysLinearLayoutVisibility.set(false);
        }else {
            observableDaysLinearLayoutVisibility.set(true);
        }

        observableRepeat.set(stringRepeat);
    }

    public void onPlaceSet(String placeName, String placeId){
        observablePlace.set(placeName);

        this.placeIdObservable.set(placeId);
    }

    // ----- Interface for this viewModel to connect to it's host (activity/fragment)

    public interface HostConnectionListener {
        Context getContext();

        void requestAccessFineLocationPermission();

        void showPlacePickerDialog();

        LinearLayout getDaysLinearLayout();
    }

    // ---- Public Methods

    public long getCurrentTimeInMillis() {
        return currentTimeInMillis;
    }

    public String getDaysOfRepeat(LinearLayout linearLayout){
        if (observableRepeat.get().equals(
                getApplication().getApplicationContext().getString(R.string.once))){
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i=0; i<linearLayout.getChildCount(); i++){
            View view = linearLayout.getChildAt(i);

            if (view instanceof TextView){
                if (view.getAlpha() == 1f){
                    builder.append(((TextView) view).getText().toString());
                    builder.append(",");
                }
            }
        }

        String days = builder.toString();

        if (! StringUtils.isNullOrEmpty(days)){
            // get rid of last comma ","
            days = days.substring(0, days.length() - 1);
        }

        return days;
    }

    public void resetAll(){
        if (originalReminder == null){
            forceSetupInitialObservableValues();
        }else {
            forceSetupInitialObservableValues(originalReminder);
        }
    }

    // ---- Private Methods

    private void forceSetupInitialObservableValues(){
        currentTimeInMillis = System.currentTimeMillis();

        Context context = getApplication().getApplicationContext();

        String[] dateAndTime = new SimpleDateFormat("dd MMM, yyyy-hh:mm a", Locale.getDefault())
                .format(new Date(currentTimeInMillis)).split("-");
        String stringDate = dateAndTime[0];
        String stringTime = dateAndTime[1];

        observableDate.set(stringDate);

        observableTime.set(stringTime);

        String place = context.getString(R.string.nothing_selected);
        observablePlace.set(place);

        observableConditionDateAndTime.set(false);

        observableRepeat.set(context.getString(R.string.once));

        observableLabel.set("");

        observableLongDescription.set("");

        observableDaysLinearLayoutVisibility.set(false);

        observableConditionAlpha.set(0.5f);
        observableSwitchEnabled.set(false);
    }

    private void forceSetupInitialObservableValues(ReminderEntity reminderEntity){
        currentTimeInMillis = reminderEntity.getTime();

        Context context = getApplication().getApplicationContext();

        String[] dateAndTime = new SimpleDateFormat("dd MMM, yyyy-hh:mm a", Locale.getDefault())
                .format(new Date(currentTimeInMillis)).split("-");
        String stringDate = dateAndTime[0];
        String stringTime = dateAndTime[1];

        observableDate.set(stringDate);

        observableTime.set(stringTime);

        String placeId = reminderEntity.getPlaceId();
        placeIdObservable.set(placeId);
        String place = StringUtils.isNullOrEmpty(placeId)
                ? context.getString(R.string.nothing_selected)
                : reminderEntity.getPlaceName();
        observablePlace.set(place);

        observableConditionDateAndTime.set(reminderEntity.isDateAndTimeCondition());

        observableRepeat.set(reminderEntity.getRepeatMode());

        String repeatDays = reminderEntity.getRepeatDays();
        if (StringUtils.isNullOrEmpty(repeatDays)){
            observableDaysLinearLayoutVisibility.set(false);
        }else {
            observableDaysLinearLayoutVisibility.set(true);

            setDaysOfRepeat(repeatDays, listener.getDaysLinearLayout());
        }

        observableLabel.set(reminderEntity.getLabel());

        observableLongDescription.set(reminderEntity.getLongDescription());

        boolean enable = ! StringUtils.isNullOrEmpty(placeId);
        observableConditionAlpha.set(enable ? 1f : 0.5f);
        observableSwitchEnabled.set(enable);
    }

    private void setDaysOfRepeat(String repeatDays, LinearLayout linearLayout){
        if (StringUtils.isNullOrEmpty(repeatDays)){
            return;
        }

        for (int i=0; i<linearLayout.getChildCount(); i++){
            View view = linearLayout.getChildAt(i);

            if (view instanceof TextView){
                String text = ((TextView) view).getText().toString();
                if (repeatDays.contains(text)){
                    view.setAlpha(1f);
                }else {
                    view.setAlpha(0.5f);
                }
            }
        }
    }

}
