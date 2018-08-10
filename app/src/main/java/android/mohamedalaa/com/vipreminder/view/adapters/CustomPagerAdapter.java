package android.mohamedalaa.com.vipreminder.view.adapters;

import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.view.viewPagerFragments.ViewPagerMainFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mohamed on 8/4/2018.
 *
 * Notes
 * 1- I know tab pages are fixed and can be added as constants from code but i prefer from
 *      strings.xml, so that in future, it's easy to make it for several languages.
 */
public class CustomPagerAdapter extends FragmentPagerAdapter {

    // --- Constants

    // --- Private Variables

    private ArrayList<String> pagesTitles;

    private HashMap<String , List<ReminderEntity>> reminderListMap;

    public CustomPagerAdapter(FragmentManager fm, ArrayList<String> pagesTitles) {
        super(fm);

        this.pagesTitles = pagesTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return ViewPagerMainFragment.newInstance(
                reminderListMap,
                pagesTitles.get(position));
    }

    @Override
    public int getCount() {
        return pagesTitles == null ? 0 : pagesTitles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pagesTitles == null ? "" : pagesTitles.get(position);
    }

    public void swapReminderListMap(HashMap<String , List<ReminderEntity>> reminderListMap){
        this.reminderListMap = reminderListMap;

        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // object instanceof ViewPagerMainFragment -> true
        return POSITION_NONE;
    }

    /**
     * Better flow to reach and refresh only 1 fragment not all of 'em is
     * in below method add tag to the fragment and after notify data set changed
     * in getItemPosition() method check the tag and update the item,
     * but since when the source data in database change -> all 3 tabs will change accordingly
     * or at least 2 then i should make update to all of them, so to do so
     * I need to call notify dataSetChanged() and always return POSITION_NONE, then in
     * below method refresh data in fragment, by the method i created -> swapData().
     */
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewPagerMainFragment createdFragment = (ViewPagerMainFragment)
                super.instantiateItem(container, position);

        createdFragment.swapData(reminderListMap);

        return createdFragment;
    }
}
