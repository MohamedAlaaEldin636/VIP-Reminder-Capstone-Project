package android.mohamedalaa.com.vipreminder.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Mohamed on 4/6/2018.
 *
 */

public class UnitsConversionsUtils {

    public static int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

}
