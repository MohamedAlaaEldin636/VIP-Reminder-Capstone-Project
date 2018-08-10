package android.mohamedalaa.com.vipreminder.view.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.customClasses.StringOrReminderEntity;
import android.mohamedalaa.com.vipreminder.databinding.ItemReminderBinding;
import android.mohamedalaa.com.vipreminder.model.database.ReminderEntity;
import android.mohamedalaa.com.vipreminder.utils.StringUtils;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Mohamed on 8/5/2018.
 *
 */
public class ReminderListRecyclerViewAdapter extends
        RecyclerView.Adapter<ReminderListRecyclerViewAdapter.CustomViewHolder> {

    private Context context;
    private List<StringOrReminderEntity> allStringOrReminderEntityList;
    private AdapterListener listener;

    private List<StringOrReminderEntity> currentStringOrReminderEntityList;

    public ReminderListRecyclerViewAdapter(Context context,
                                           ArrayList<String> titles,
                                           List<List<ReminderEntity>> lists,
                                           AdapterListener listener) {
        this.context = context;

        // Note tittles size must equal lists size, and in same order
        allStringOrReminderEntityList = new ArrayList<>();
        for (int i=0; i<titles.size(); i++){
            StringOrReminderEntity item1 = new StringOrReminderEntity(
                    titles.get(i), null);

            allStringOrReminderEntityList.add(item1);

            List<ReminderEntity> reminderEntityList = lists.get(i);
            for (ReminderEntity entity : reminderEntityList){
                StringOrReminderEntity item2 = new StringOrReminderEntity(
                        null, entity);

                allStringOrReminderEntityList.add(item2);
            }
        }

        currentStringOrReminderEntityList = new ArrayList<>(allStringOrReminderEntityList);

        this.listener = listener;
    }

    public ReminderListRecyclerViewAdapter(Context context,
                                           ArrayList<String> titles,
                                           List<List<ReminderEntity>> lists,
                                           @NonNull List<StringOrReminderEntity> currentStringOrReminderEntityList,
                                           AdapterListener listener) {
        this.context = context;

        // Note tittles size must equal lists size, and in same order
        allStringOrReminderEntityList = new ArrayList<>();
        for (int i=0; i<titles.size(); i++){
            StringOrReminderEntity item1 = new StringOrReminderEntity(
                    titles.get(i), null);

            allStringOrReminderEntityList.add(item1);

            List<ReminderEntity> reminderEntityList = lists.get(i);
            for (ReminderEntity entity : reminderEntityList){
                StringOrReminderEntity item2 = new StringOrReminderEntity(
                        null, entity);

                allStringOrReminderEntityList.add(item2);
            }
        }

        this.currentStringOrReminderEntityList = currentStringOrReminderEntityList;

        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        ItemReminderBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.item_reminder, parent, false);

        return new CustomViewHolder(binding);
    }

    /**
     * RedundantIfStatement as it could be simplifies but i prefer not to do so, since
     * I want the comments inside each if statement block to be read either by me or anyone else
     * in order to make better understanding.
     */
    @SuppressWarnings("RedundantIfStatement")
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        StringOrReminderEntity item = currentStringOrReminderEntityList.get(position);

        if (StringUtils.isNullOrEmpty(item.getTitle())){
            // it is card view item
            holder.binding.rootTitleLinearLayout.setVisibility(View.GONE);
            holder.binding.rootCardViewLayout.setVisibility(View.VISIBLE);

            ReminderEntity reminderEntity = item.getReminderEntity();

            holder.binding.labelTextView.setText(reminderEntity.getLabel());

            String[] dateAndTime = formatDate(reminderEntity.getTime());
            holder.binding.timeTextView.setText(dateAndTime[1]);
            holder.binding.dateTextView.setText(dateAndTime[0]);

            holder.binding.repeatTextView.setText(reminderEntity.getRepeatMode());

            String placeId = reminderEntity.getPlaceId();
            if (StringUtils.isNullOrEmpty(placeId)){
                holder.binding.placeLinearLayout.setVisibility(View.GONE);
            }else {
                holder.binding.placeLinearLayout.setVisibility(View.VISIBLE);

                String placeName = reminderEntity.getPlaceName();
                if (StringUtils.isNullOrEmpty(placeName)){
                    placeName = reminderEntity.getLatitude() + "," + reminderEntity.getLongitude();
                }
                holder.binding.placeTextView.setText(placeName);
            }

            @DrawableRes int favIcon = reminderEntity.isFavourite()
                    ? R.drawable.ic_star_pink : R.drawable.ic_baseline_star_24px;
            holder.binding.favImageView.setImageResource(favIcon);

            holder.binding.moreVertImageView.setOnClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.inflate(R.menu.popup_menu_item_reminder);

                if (reminderEntity.isDone()){
                    popupMenu.getMenu().findItem(R.id.doneAction).setVisible(false);
                }else {
                    popupMenu.getMenu().findItem(R.id.undoneAction).setVisible(false);
                }

                popupMenu.setOnMenuItemClickListener(
                        new CustomOnMenuItemClickListener(reminderEntity));

                popupMenu.show();
            });

            holder.binding.favImageView.setOnClickListener(view -> {
                reminderEntity.setFavourite(! reminderEntity.isFavourite());

                listener.updateReminderEntity(reminderEntity);
            });

            holder.binding.rootCardViewLayout.setOnClickListener(view
                    -> listener.launchReminderToEdit(reminderEntity));
        }else {
            String title = item.getTitle();
            holder.binding.titleTextView.setText(title);

            boolean isExpanded;

            holder.binding.rootTitleLinearLayout.setVisibility(View.VISIBLE);
            holder.binding.rootCardViewLayout.setVisibility(View.GONE);

            // Note imageView might be collapsed or expanded and that can be easily defined
            // by next item in currentStringOrReminderEntityList
            if (position == currentStringOrReminderEntityList.size() - 1){
                // Last item in recyclerView is title then surely
                // it is collapsed
                isExpanded = false;
            }else if (StringUtils.isNullOrEmpty(
                    currentStringOrReminderEntityList.get(position + 1).getTitle())){
                // next item isn't title then
                // it is expanded
                isExpanded = true;
            }else {
                // next item is title then
                // it is collapsed
                isExpanded = false;
            }

            @DrawableRes int arrowSrc = isExpanded
                    ? R.drawable.ic_arrow_left_24px : R.drawable.ic_arrow_drop_down_24px;
            holder.binding.arrowImageView.setImageResource(arrowSrc);

            final int currentIndex = position;
            holder.binding.rootTitleLinearLayout.setOnClickListener(view -> {
                boolean isCurrentlyExpanded
                        = currentIndex != currentStringOrReminderEntityList.size() - 1
                        && StringUtils.isNullOrEmpty(
                                currentStringOrReminderEntityList.get(currentIndex + 1).getTitle());

                if (isCurrentlyExpanded){
                    // at least 1, or it is collapsed
                    int deleteCounter = 1;
                    for (int i = currentIndex + 2; i < currentStringOrReminderEntityList.size(); i++){
                        StringOrReminderEntity tempItem = currentStringOrReminderEntityList.get(i);
                        if (StringUtils.isNullOrEmpty(tempItem.getTitle())){
                            deleteCounter++;
                        }else {
                            break;
                        }
                    }

                    // delete from list
                    for (int i=1; i<=deleteCounter; i++){
                        currentStringOrReminderEntityList.remove(currentIndex + 1);
                    }

                    // notify data set changed
                    notifyDataSetChanged();
                }else {
                    String currentTitle = currentStringOrReminderEntityList.get(currentIndex).getTitle();

                    List<StringOrReminderEntity> toBeAddedList = getListFromAllByTitle(currentTitle);

                    // add collapsed list
                    for (int i=0; i<toBeAddedList.size(); i++){
                        currentStringOrReminderEntityList.add(currentIndex + 1 + i,
                                toBeAddedList.get(i));
                    }

                    // notify data set changed
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return currentStringOrReminderEntityList == null
                ? 0
                : currentStringOrReminderEntityList.size();
    }

    // ----- View Holder Class

    class CustomViewHolder extends RecyclerView.ViewHolder {

        final ItemReminderBinding binding;

        CustomViewHolder(ItemReminderBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }

    // ---- Private Helper Methods

    /**
     * index 0 -> have date as 08 Jul, 2018
     * index 1 -> have time as 06:35 PM
     */
    private String[] formatDate(long timeInMillis){
        return new SimpleDateFormat("dd MMM, yyyy-hh:mm a", Locale.getDefault())
                .format(new Date(timeInMillis)).split("-");
    }

    private List<StringOrReminderEntity> getListFromAllByTitle(String currentTitle) {
        int indexOfFirstCardItem = allStringOrReminderEntityList.size();
        for (int i=0; i<allStringOrReminderEntityList.size(); i++){
            StringOrReminderEntity item = allStringOrReminderEntityList.get(i);

            String title = item.getTitle();
            if (! StringUtils.isNullOrEmpty(title)){
                if (title.equals(currentTitle)){
                    indexOfFirstCardItem = i + 1;
                    break;
                }
            }
        }

        List<StringOrReminderEntity> list = new ArrayList<>();
        for (int i = indexOfFirstCardItem; i<allStringOrReminderEntityList.size(); i++){
            StringOrReminderEntity item = allStringOrReminderEntityList.get(i);

            if (StringUtils.isNullOrEmpty(item.getTitle())){
                list.add(item);
            }else {
                break;
            }
        }

        return list;
    }

    // ---- Public Methods

    public List<StringOrReminderEntity> getCurrentStringOrReminderEntityList() {
        return currentStringOrReminderEntityList;
    }

    // ---- Interface to connect to host activity/fragment

    public interface AdapterListener {
        void updateReminderEntity(ReminderEntity reminderEntity);

        void launchReminderToEdit(ReminderEntity reminderEntity);

        void showToast(String msg);

        void deleteReminderEntities(ReminderEntity... reminderEntities);
    }

    // ----- Custom inner helper class

    private class CustomOnMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private ReminderEntity reminderEntity;

        CustomOnMenuItemClickListener(ReminderEntity reminderEntity) {
            this.reminderEntity = reminderEntity;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.doneAction:
                case R.id.undoneAction:
                    /*
                     Flow
                     -> If it depends on time only or time and place
                            where both conditions must be met or not
                     Then make done on the next occurrence which means for ex. if reminder will fire
                     Today and tomorrow after the done then it should fire tomorrow only, if once
                     then mark as done as it will not be fired again.
                     -> In case Depends on Place Only
                     consider it as once even if repeat is anything else.

                     Note
                     the above flow is achieved inside the Update asyncTask only not in the insert.
                     */
                    reminderEntity.setDone(! reminderEntity.isDone());
                    listener.updateReminderEntity(reminderEntity);
                    return true;
                case R.id.editAction:
                    listener.launchReminderToEdit(reminderEntity);
                    return true;
                case R.id.copyAction:
                    String reminderLabelText = reminderEntity.getLabel();

                    ClipboardManager clipboard = (ClipboardManager) context
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null){
                        ClipData clip = ClipData.newPlainText(context
                                .getString(R.string.vip_reminder_label), reminderLabelText);
                        clipboard.setPrimaryClip(clip);
                    }

                    listener.showToast(context.getString(R.string.copied_to_clipboard));

                    return true;
                case R.id.shareAction:
                    Intent intent = new Intent();
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, reminderEntity.getLabel());

                    if (intent.resolveActivity(context.getPackageManager()) != null){
                        context.startActivity(Intent.createChooser(
                                intent, context.getString(R.string.vip_reminder_label)));
                    }

                    return true;
                case R.id.deleteAction:
                    listener.deleteReminderEntities(reminderEntity);
                    return true;
            }

            return false;
        }
    }


}
