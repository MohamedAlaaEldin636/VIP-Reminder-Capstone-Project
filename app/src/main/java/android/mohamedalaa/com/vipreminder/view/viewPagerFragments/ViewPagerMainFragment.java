package android.mohamedalaa.com.vipreminder.view.viewPagerFragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.mohamedalaa.com.vipreminder.BaseApplication;
import android.mohamedalaa.com.vipreminder.DataRepository;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.customClasses.CustomGridLayoutManager;
import android.mohamedalaa.com.vipreminder.customClasses.DeleteReminderAsyncTask;
import android.mohamedalaa.com.vipreminder.customClasses.StringOrReminderEntity;
import android.mohamedalaa.com.vipreminder.customClasses.UpdateReminderAsyncTask;
import android.mohamedalaa.com.vipreminder.databinding.FragmentViewpagerMainBinding;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.ConstantsUtils;
import android.mohamedalaa.com.vipreminder.utils.ListUtils;
import android.mohamedalaa.com.vipreminder.view.AddReminderActivity;
import android.mohamedalaa.com.vipreminder.view.adapters.ReminderListRecyclerViewAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mohamed on 8/4/2018.
 *
 */
@SuppressWarnings("unchecked")
public class ViewPagerMainFragment extends Fragment implements
        ReminderListRecyclerViewAdapter.AdapterListener {

    // --- Constants

    private static final int RECYCLER_VIEW_COLUMNS = 1;

    private static final String NEW_INSTANCE_PAGE_TITLE = "NEW_INSTANCE_PAGE_TITLE";
    private static final String NEW_INSTANCE_REMINDER_LIST_MAP = "NEW_INSTANCE_REMINDER_LIST_MAP";

    /** SISK -> stands for Saved Instance State Key */
    private static final String SISK_STRING_OR_REMINDER_LIST = "SISK_STRING_OR_REMINDER_LIST";

    // --- Private Variables

    private FragmentViewpagerMainBinding binding;

    @Nullable private HashMap<String , List<ReminderEntity>> reminderListMap;
    private String pageTitle;

    private ReminderListRecyclerViewAdapter adapter;

    @Nullable private List<StringOrReminderEntity> currentStringOrReminderEntityList = null;

    private Toast toast;

    public ViewPagerMainFragment(){}

    public static ViewPagerMainFragment newInstance(
            @Nullable HashMap<String , List<ReminderEntity>> reminderListMap,
            String pageTitle){
        ViewPagerMainFragment fragment = new ViewPagerMainFragment();
        Bundle args = new Bundle();
        args.putSerializable(NEW_INSTANCE_REMINDER_LIST_MAP, reminderListMap);
        args.putString(NEW_INSTANCE_PAGE_TITLE, pageTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null){
            this.reminderListMap = (HashMap<String, List<ReminderEntity>>)
                    args.getSerializable(NEW_INSTANCE_REMINDER_LIST_MAP);
            this.pageTitle = args.getString(NEW_INSTANCE_PAGE_TITLE);
        }

        if (savedInstanceState != null){
            currentStringOrReminderEntityList = (List<StringOrReminderEntity>)
                    savedInstanceState.getSerializable(SISK_STRING_OR_REMINDER_LIST);

            // because it's impossible even if all collapsed to be zero size
            if (currentStringOrReminderEntityList != null
                    && currentStringOrReminderEntityList.size() == 0){
                currentStringOrReminderEntityList = null;
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (adapter != null){
            outState.putSerializable(SISK_STRING_OR_REMINDER_LIST,
                    (Serializable) adapter.getCurrentStringOrReminderEntityList());
        }

        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_viewpager_main, container, false);
        View view = binding.getRoot();

        // initial recycler views with title setups
        setupIncludedXmlViews();

        return view;
    }

    private void setupIncludedXmlViews() {
        if (getContext() == null){

            return;
        }

        if (pageTitle.equals(getString(R.string.upcoming))){
            putIncludeForUpcoming();

            binding.imageViewInEmptyView.setImageResource(R.drawable.ic_event_note_24px);
            binding.titleTextViewInEmptyView.setText(getString(R.string.no_tasks_found_here));
            binding.subTitleTextViewInEmptyView.setText(getString(R.string.add_tasks_from_the_below_button));

            binding.poweredByGoogleLinearLayout.setPadding(
                    0,
                    0,
                    (int) getResources().getDimension(R.dimen.powered_by_google_area_padding_end),
                    0);
            binding.fab.setVisibility(View.VISIBLE);
        }else if (pageTitle.equals(getString(R.string.favourite))){
            putDataForFavourite();

            binding.imageViewInEmptyView.setImageResource(R.drawable.ic_baseline_star_24px);
            binding.titleTextViewInEmptyView.setText(getString(R.string.no_favourite_found));
            binding.subTitleTextViewInEmptyView.setText(getString(R.string.favourite_subtitle));

            binding.poweredByGoogleLinearLayout.setPadding(0, 0, 0, 0);
            binding.fab.setVisibility(View.GONE);
        }else {
            // Then it is history
            putDataForHistory();

            binding.imageViewInEmptyView.setImageResource(R.drawable.ic_baseline_history_24px);
            binding.titleTextViewInEmptyView.setText(getString(R.string.no_history_found));
            binding.subTitleTextViewInEmptyView.setText(getString(R.string.history_subtitle));

            binding.poweredByGoogleLinearLayout.setPadding(0, 0, 0, 0);
            binding.fab.setVisibility(View.GONE);
        }

        binding.fab.setOnClickListener(view
                -> startActivity(new Intent(getActivity(), AddReminderActivity.class)));
    }

    /** {@link #setupIncludedXmlViews()} */
    
    private void putIncludeForUpcoming() {
        if (reminderListMap == null){
            binding.emptyViewLinearLayout.setVisibility(View.VISIBLE);

            return;
        }

        ArrayList<String> titlesInRecyclerView = new ArrayList<>();

        List<ReminderEntity> todayUpcomingList = reminderListMap.get(
                ConstantsUtils.MAP_TODAY_UPCOMING_LIST);
        List<ReminderEntity> tomorrowUpcomingList = reminderListMap.get(
                ConstantsUtils.MAP_TOMORROW_UPCOMING_LIST);
        List<ReminderEntity> upcomingUpcomingList = reminderListMap.get(
                ConstantsUtils.MAP_UPCOMING_UPCOMING_LIST);

        List<List<ReminderEntity>> lists = new ArrayList<>();
        if (!ListUtils.isNullOrEmpty(todayUpcomingList)){
            titlesInRecyclerView.add(getString(R.string.today));
            lists.add(todayUpcomingList);
        }
        if (!ListUtils.isNullOrEmpty(tomorrowUpcomingList)){
            titlesInRecyclerView.add(getString(R.string.tomorrow));
            lists.add(tomorrowUpcomingList);
        }
        if (!ListUtils.isNullOrEmpty(upcomingUpcomingList)){
            titlesInRecyclerView.add(getString(R.string.upcoming));
            lists.add(upcomingUpcomingList);
        }

        if (titlesInRecyclerView.size() == 0){
            binding.emptyViewLinearLayout.setVisibility(View.VISIBLE);
        }else {
            binding.emptyViewLinearLayout.setVisibility(View.GONE);

            // setup recycler view
            // Note -> used grid not linear in case i will want to make more than 1 column in tablet
            CustomGridLayoutManager customGridLayoutManager = new CustomGridLayoutManager(
                    getContext(),
                    RECYCLER_VIEW_COLUMNS,
                    LinearLayoutManager.VERTICAL,
                    false);
            binding.recyclerView.setLayoutManager(customGridLayoutManager);
            if (currentStringOrReminderEntityList == null){
                adapter = new ReminderListRecyclerViewAdapter(getContext(),
                        titlesInRecyclerView, lists, this);
            }else {
                adapter = new ReminderListRecyclerViewAdapter(getContext(),
                        titlesInRecyclerView, lists, currentStringOrReminderEntityList, this);
            }
            binding.recyclerView.setAdapter(adapter);
        }
    }

    private void putDataForFavourite() {
        if (reminderListMap == null){
            binding.emptyViewLinearLayout.setVisibility(View.VISIBLE);

            return;
        }

        ArrayList<String> titlesInRecyclerView = new ArrayList<>();

        List<ReminderEntity> todayFavouriteList = reminderListMap.get(
                ConstantsUtils.MAP_TODAY_FAVOURITE_LIST);
        List<ReminderEntity> tomorrowFavouriteList = reminderListMap.get(
                ConstantsUtils.MAP_TOMORROW_FAVOURITE_LIST);
        List<ReminderEntity> upcomingFavouriteList = reminderListMap.get(
                ConstantsUtils.MAP_UPCOMING_FAVOURITE_LIST);
        List<ReminderEntity> doneFavouriteList = reminderListMap.get(
                ConstantsUtils.MAP_DONE_FAVOURITE_LIST);
        List<ReminderEntity> overdueFavouriteList = reminderListMap.get(
                ConstantsUtils.MAP_OVERDUE_FAVOURITE_LIST);

        List<List<ReminderEntity>> lists = new ArrayList<>();
        if (!ListUtils.isNullOrEmpty(doneFavouriteList)){
            titlesInRecyclerView.add(getString(R.string.done));
            lists.add(doneFavouriteList);
        }
        if (!ListUtils.isNullOrEmpty(overdueFavouriteList)){
            titlesInRecyclerView.add(getString(R.string.overdue));
            lists.add(overdueFavouriteList);
        }
        if (!ListUtils.isNullOrEmpty(todayFavouriteList)){
            titlesInRecyclerView.add(getString(R.string.today));
            lists.add(todayFavouriteList);
        }
        if (!ListUtils.isNullOrEmpty(tomorrowFavouriteList)){
            titlesInRecyclerView.add(getString(R.string.tomorrow));
            lists.add(tomorrowFavouriteList);
        }
        if (!ListUtils.isNullOrEmpty(upcomingFavouriteList)){
            titlesInRecyclerView.add(getString(R.string.upcoming));
            lists.add(upcomingFavouriteList);
        }

        if (titlesInRecyclerView.size() == 0){
            binding.emptyViewLinearLayout.setVisibility(View.VISIBLE);
        }else {
            binding.emptyViewLinearLayout.setVisibility(View.GONE);

            // setup recycler view
            // Note -> used grid not linear in case i will want to make more than 1 column in tablet
            CustomGridLayoutManager customGridLayoutManager = new CustomGridLayoutManager(
                    getContext(),
                    RECYCLER_VIEW_COLUMNS,
                    LinearLayoutManager.VERTICAL,
                    false);
            binding.recyclerView.setLayoutManager(customGridLayoutManager);
            if (currentStringOrReminderEntityList == null){
                adapter = new ReminderListRecyclerViewAdapter(getContext(),
                        titlesInRecyclerView, lists, this);
            }else {
                adapter = new ReminderListRecyclerViewAdapter(getContext(),
                        titlesInRecyclerView, lists, currentStringOrReminderEntityList, this);
            }
            binding.recyclerView.setAdapter(adapter);
        }
    }

    private void putDataForHistory() {
        if (reminderListMap == null){
            binding.emptyViewLinearLayout.setVisibility(View.VISIBLE);

            return;
        }

        ArrayList<String> titlesInRecyclerView = new ArrayList<>();

        List<ReminderEntity> doneHistoryList = reminderListMap.get(
                ConstantsUtils.MAP_DONE_HISTORY_LIST);
        List<ReminderEntity> overdueHistoryList = reminderListMap.get(
                ConstantsUtils.MAP_OVERDUE_HISTORY_LIST);

        List<List<ReminderEntity>> lists = new ArrayList<>();
        if (!ListUtils.isNullOrEmpty(doneHistoryList)){
            titlesInRecyclerView.add(getString(R.string.done));
            lists.add(doneHistoryList);
        }
        if (!ListUtils.isNullOrEmpty(overdueHistoryList)){
            titlesInRecyclerView.add(getString(R.string.overdue));
            lists.add(overdueHistoryList);
        }

        if (titlesInRecyclerView.size() == 0){
            binding.emptyViewLinearLayout.setVisibility(View.VISIBLE);
        }else {
            binding.emptyViewLinearLayout.setVisibility(View.GONE);

            // setup recycler view
            // Note -> used grid not linear in case i will want to make more than 1 column in tablet
            CustomGridLayoutManager customGridLayoutManager = new CustomGridLayoutManager(
                    getContext(),
                    RECYCLER_VIEW_COLUMNS,
                    LinearLayoutManager.VERTICAL,
                    false);
            binding.recyclerView.setLayoutManager(customGridLayoutManager);
            if (currentStringOrReminderEntityList == null){
                adapter = new ReminderListRecyclerViewAdapter(getContext(),
                        titlesInRecyclerView, lists, this);
            }else {
                adapter = new ReminderListRecyclerViewAdapter(getContext(),
                        titlesInRecyclerView, lists, currentStringOrReminderEntityList, this);
            }
            binding.recyclerView.setAdapter(adapter);
        }
    }

    // ---- Public Methods

    public void swapData(HashMap<String , List<ReminderEntity>> reminderListMap){
        this.reminderListMap = reminderListMap;

        // Below line is added cuz if binding is null then the method below this if statement
        // might throw a NPE error, is System killed the app will in background.
        if (binding == null){
            return;
        }

        setupIncludedXmlViews();
    }

    // ---- Implement ReminderListRecyclerViewAdapter.AdapterListener Methods

    @Override
    public void updateReminderEntity(ReminderEntity reminderEntity) {
        if (getActivity() != null){
            DataRepository dataRepository = ((BaseApplication) getActivity().getApplication())
                    .getRepository();

            new UpdateReminderAsyncTask(getActivity().getBaseContext(), dataRepository, reminderEntity, getString(R.string.once),
                    getString(R.string.hourly), getString(R.string.daily), getString(R.string.weekly),
                    getString(R.string.monthly), getString(R.string.yearly))
                    .execute();
        }
    }

    @Override
    public void launchReminderToEdit(ReminderEntity reminderEntity) {
        Intent intent = new Intent(getActivity(), AddReminderActivity.class);
        intent.putExtra(AddReminderActivity.INTENT_KEY_REMINDER_ENTITY, reminderEntity);
        startActivity(intent);
    }

    @Override
    public void showToast(String msg) {
        if (toast != null){
            toast.cancel();
        }

        toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void deleteReminderEntities(ReminderEntity... reminderEntities) {
        if (getActivity() != null){
            DataRepository dataRepository = ((BaseApplication) getActivity().getApplication())
                    .getRepository();

            new DeleteReminderAsyncTask(getActivity().getBaseContext(), dataRepository)
                    .execute(reminderEntities);
        }
    }
}
