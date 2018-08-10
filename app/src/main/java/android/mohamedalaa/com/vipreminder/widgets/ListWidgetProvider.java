package android.mohamedalaa.com.vipreminder.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.mohamedalaa.com.vipreminder.utils.SharedPrefUtils;
import android.mohamedalaa.com.vipreminder.view.AddReminderActivity;
import android.widget.RemoteViews;
import android.mohamedalaa.com.vipreminder.R;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ListWidgetProviderConfigureActivity ListWidgetProviderConfigureActivity}
 */
public class ListWidgetProvider extends AppWidgetProvider {

    public static void updateAppWidget(Context context,
                                       AppWidgetManager appWidgetManager,
                                       int appWidgetId,
                                       String titleOfCategory){
        // Construct the RemoteViews object
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(), R.layout.list_widget_provider);

        // Set title of category
        remoteViews.setTextViewText(R.id.categoryTextView, titleOfCategory);

        // list view
        Intent adapterIntent = new Intent(context, WidgetServiceReminders.class);
        adapterIntent.setAction(String.valueOf(appWidgetId));
        remoteViews.setRemoteAdapter(R.id.listView, adapterIntent);

        remoteViews.setEmptyView(R.id.listView, R.id.emptyViewTextView);

        Intent intentEditReminder = new Intent(context, AddReminderActivity.class);
        PendingIntent pendingIntentEditReminder = PendingIntent.getActivity(
                context, 0, intentEditReminder, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.listView, pendingIntentEditReminder);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    public static void updateAllReminderListAppWidgetsAndNotifyChangeToListView(Context context,
                                                                                AppWidgetManager appWidgetManager,
                                                                                int[] appWidgetIds){
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView);
            updateAppWidget(context, appWidgetManager, appWidgetId,
                    SharedPrefUtils.getWidgetChosenCategory(context, appWidgetId));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Timber.v("Inside On Update of list widget.");

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId,
                    SharedPrefUtils.getWidgetChosenCategory(context, appWidgetId));
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

