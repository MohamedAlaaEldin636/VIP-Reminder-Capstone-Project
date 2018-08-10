package android.mohamedalaa.com.vipreminder.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.mohamedalaa.com.vipreminder.databinding.ListWidgetProviderConfigureBinding;
import android.mohamedalaa.com.vipreminder.utils.SharedPrefUtils;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.mohamedalaa.com.vipreminder.R;
import android.widget.TextView;

/**
 * The configuration screen for the {@link ListWidgetProvider ListWidgetProvider} AppWidget.
 */
public class ListWidgetProviderConfigureActivity extends AppCompatActivity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private View.OnClickListener categoryTextViewOnClickListener = view -> {
        if (view instanceof TextView){
            String category = ((TextView) view).getText().toString();

            Context context = getBaseContext().getApplicationContext();

            SharedPrefUtils.setWidgetChosenCategory(context, appWidgetId, category);

            // Push widget update to surface with newly set prefix
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ListWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId, category);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView);
            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public ListWidgetProviderConfigureActivity() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        ListWidgetProviderConfigureBinding binding = DataBindingUtil.setContentView(
                this, R.layout.list_widget_provider_configure);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();

            return;
        }

        // all text views
        binding.allTextView.setOnClickListener(categoryTextViewOnClickListener);
        binding.todayTextView.setOnClickListener(categoryTextViewOnClickListener);
        binding.tomorrowTextView.setOnClickListener(categoryTextViewOnClickListener);
        binding.upcomingTextView.setOnClickListener(categoryTextViewOnClickListener);
        binding.doneTextView.setOnClickListener(categoryTextViewOnClickListener);
        binding.overdueTextView.setOnClickListener(categoryTextViewOnClickListener);

        // cancel button
        binding.cancelButton.setOnClickListener(view -> finish());
    }
}

