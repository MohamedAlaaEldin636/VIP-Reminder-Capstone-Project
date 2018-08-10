package android.mohamedalaa.com.vipreminder.utils;

import java.util.Calendar;

/**
 * Created by Mohamed on 8/4/2018.
 *
 */
public class CalenderUtils {

    /**
     * if returns 0 -> then provided time is today
     * if returns 1 -> then provided time is yesterday
     * and so on.
     */
    public static int getCurrentDayMinusProvidedOne(long timeInMilli){
        Calendar providedTime = Calendar.getInstance();
        providedTime.setTimeInMillis(timeInMilli);

        Calendar now = Calendar.getInstance();

        return (now.get(Calendar.DATE) - providedTime.get(Calendar.DATE));
    }

}
