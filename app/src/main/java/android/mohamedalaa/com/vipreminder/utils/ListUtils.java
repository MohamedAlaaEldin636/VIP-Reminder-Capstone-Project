package android.mohamedalaa.com.vipreminder.utils;

import java.util.List;

/**
 * Created by Mohamed on 8/5/2018.
 *
 */
public class ListUtils {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isNullOrEmpty(List list){
        return list == null || list.size() == 0;
    }

}
