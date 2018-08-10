package android.mohamedalaa.com.vipreminder.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Mohamed on 8/9/2018.
 *
 */
public class SharedPrefUtils {

    /**
     * Doing below approach to ensure we have the same widget with different lists,
     *      not all widget with the same data.
     *
     * Same sharedPref key generation methodology is done in corresponding get method.
     */
    synchronized public static void setWidgetChosenCategory(Context context,
                                                            int appWidgetId,
                                                            String category){
        SharedPreferences sharedPref = context.getSharedPreferences(
                SharedPrefConstants.SH_PREF_FILE_WIDGET_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String keySpecificToAppWidgetId
                = SharedPrefConstants.SH_PREF_KEY_CHOSEN_CATEGORY + "_" + appWidgetId;

        editor.putString(keySpecificToAppWidgetId,
                category);

        editor.apply();
    }

    synchronized public static String getWidgetChosenCategory(Context context, int appWidgetId){
        SharedPreferences sharedPref = context.getSharedPreferences(
                SharedPrefConstants.SH_PREF_FILE_WIDGET_NAME, Context.MODE_PRIVATE);

        String keySpecificToAppWidgetId
                = SharedPrefConstants.SH_PREF_KEY_CHOSEN_CATEGORY + "_" + appWidgetId;

        return sharedPref.getString(keySpecificToAppWidgetId,
                SharedPrefConstants.SH_PREF_VALUE_CHOSEN_CATEGORY);
    }

    private class SharedPrefConstants {

        /**  File Name */
        static final String SH_PREF_FILE_WIDGET_NAME = "SH_PREF_FILE_WIDGET_NAME";

        /** File Keys */
        static final String SH_PREF_KEY_CHOSEN_CATEGORY = "SH_PREF_KEY_CHOSEN_CATEGORY";

        /** File Default Values */
        static final String SH_PREF_VALUE_CHOSEN_CATEGORY = "";

    }

}
