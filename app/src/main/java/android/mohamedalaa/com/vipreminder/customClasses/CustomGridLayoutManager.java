package android.mohamedalaa.com.vipreminder.customClasses;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import timber.log.Timber;

/**
 * Created by Mohamed on 7/31/2018.
 *
 * ==> Usage
 * 1- used along with recyclerView.stopScroll to avoid error when scrolling then go to another
 *      viewPager while scrolling (Also note it is known bug but i don't remember link of issue).
 */
public class CustomGridLayoutManager extends GridLayoutManager {

    public CustomGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public CustomGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Timber.v(".onLayoutChildren() error msg = " + e.getMessage());
        }
    }
}
