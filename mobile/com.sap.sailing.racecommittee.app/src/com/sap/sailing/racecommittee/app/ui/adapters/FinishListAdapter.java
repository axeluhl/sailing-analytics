package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.List;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.dragandswipelist.BaseDraggableSwipeAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.dragandswipelist.BaseDraggableSwipeViewHolder;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.TrackingListFragment;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FinishListAdapter extends BaseDraggableSwipeAdapter<FinishListAdapter.ItemViewHolder> {

    private final String TAG = FinishListAdapter.class.getName();

    private final Context mContext;
    private final CompetitorResultsList<CompetitorResultWithIdImpl> mItems;
    private final boolean mCanBoatsOfCompetitorsChangePerRace;
    private TrackingListFragment mParent;

    public FinishListAdapter(Context context, List<CompetitorResultWithIdImpl> competitors,
            boolean canBoatsOfCompetitorsChangePerRace, TrackingListFragment parent) {
        super(context, competitors, parent);
        setHasStableIds(true);
        mContext = context;
        mItems = (CompetitorResultsList<CompetitorResultWithIdImpl>) competitors;
        mCanBoatsOfCompetitorsChangePerRace = canBoatsOfCompetitorsChangePerRace;
        mParent = parent;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // NinePatchDrawable drawable = (NinePatchDrawable) ContextCompat.getDrawable(getActivity(),
        // R.drawable.material_shadow_z3);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.race_tracking_list_draggable_item, parent,
                false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        return itemViewHolder;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should update the contents of
     * the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method again if the position of the item
     * changes in the data set unless the item itself is invalidated or the new position cannot be determined. For this
     * reason, you should only use the <code>position</code> parameter while acquiring the related data item inside this
     * method and should not keep a copy of it. If you need the position of an item later on (e.g. in a click listener),
     * use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)} instead if Adapter can handle efficient
     * partial bind.
     *
     * @param holder
     *            The ViewHolder which should be updated to represent the contents of the item at the given position in
     *            the data set.
     * @param position
     *            The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        CompetitorResultWithIdImpl item = mItems.get(position);
        if (item.getOneBasedRank() != 0) {
            holder.position.setText(String.valueOf(item.getOneBasedRank()));
        } else {
            holder.position.setText(null);
        }
        holder.vesselId.setVisibility(View.GONE);
        if (mCanBoatsOfCompetitorsChangePerRace && item.getBoat() != null) {
            holder.vesselId.setVisibility(View.VISIBLE);
            holder.vesselId.setText(item.getBoat().getSailID());
            if (item.getBoat().getColor() != null) {
                ViewHelper.setColors(holder.vesselId, item.getBoat().getColor().getAsHtml());
            }
        }
        holder.competitor.setText(item.getCompetitorDisplayName());
        Drawable warning;
        switch (item.getMergeState()) {
        case ERROR:
            warning = ContextCompat.getDrawable(mContext, R.drawable.ic_warning_red);
            break;
        case WARNING:
            warning = ContextCompat.getDrawable(mContext, R.drawable.ic_warning_yellow);
            break;
        default:
            warning = null;
            break;
        }
        holder.warning.setImageDrawable(warning);
        holder.penalty.setText(item.getMaxPointsReason().name());
        holder.penalty.setVisibility(item.getMaxPointsReason().equals(MaxPointsReason.NONE) ? View.GONE : View.VISIBLE);
        int bgId = R.attr.sap_gray_black_30;
        if (mItems.get(position).getOneBasedRank() == 0) {
            bgId = R.attr.sap_gray_black_20;
        }
        holder.container.setBackgroundColor(ThemeHelper.getColor(mContext, bgId));
        holder.dragHandle.setVisibility(item.getOneBasedRank() != 0 ? View.VISIBLE : View.INVISIBLE);
        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mDragListener.onStartDrag(holder);
                }
                return false;
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // Note: fromPosition may be greater than toPosition if item is moved towards the top of the list
        boolean wasMoved = super.onItemMove(fromPosition, toPosition);
        if (wasMoved) {
            mParent.onItemMove(fromPosition, toPosition);
        }
        return wasMoved;
    }

    @Override
    public void onItemRemove(int position) {
        CompetitorResultWithIdImpl item = mItems.get(position);
        super.onItemRemove(position);
        mParent.onItemRemove(position, item);
    }

    private static Pair<Integer, Integer> getItemDraggableRange(
            CompetitorResultsList<CompetitorResultWithIdImpl> items) {
        final int start = 0;
        final int end = items.getFirstRankZeroPosition() - 1;
        return new Pair<Integer, Integer>(start, end);
    }

    public int getFirstRankZeroPosition() {
        return mItems.getFirstRankZeroPosition();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements BaseDraggableSwipeViewHolder {
        View container;
        View dragHandle;
        View editItem;
        TextView position;
        TextView vesselId;
        TextView competitor;
        ImageView warning;
        TextView penalty;

        public ItemViewHolder(View itemView) {
            super(itemView);

            container = ViewHelper.get(itemView, R.id.container);
            dragHandle = ViewHelper.get(itemView, R.id.drag_handle);
            editItem = ViewHelper.get(itemView, R.id.edit_item);
            if (editItem != null) {
                editItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CompetitorResultWithIdImpl item = mItems.get(getAdapterPosition());
                        mParent.onItemEdit(item);
                    }
                });
            }
            vesselId = ViewHelper.get(itemView, R.id.vessel_id);
            competitor = ViewHelper.get(itemView, R.id.competitor);
            warning = ViewHelper.get(itemView, R.id.warning_sign);
            penalty = ViewHelper.get(itemView, R.id.item_penalty);
            position = ViewHelper.get(itemView, R.id.position);
            if (position != null) {
                position.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        CompetitorResultWithIdImpl item = mItems.get(getAdapterPosition());
                        mParent.onLongClick(item);
                        return true;
                    }
                });
            }
        }

        /**
         * Called when the {@link ItemTouchHelper} first registers an item as being moved or swiped. Implementations
         * should update the item view to indicate it's active state.
         */
        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
            CompetitorResultWithIdImpl item = mItems.get(getAdapterPosition());
            if (item.getOneBasedRank() == 0) {
                container.setBackgroundColor(ThemeHelper.getColor(mContext, R.attr.sap_gray_black_30));
            }
        }

        /**
         * Called when the {@link ItemTouchHelper} has completed the move or swipe, and the active item state should be
         * cleared.
         */
        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
            container.setBackgroundColor(0);
        }

        @Override
        public boolean isDragAllowed() {
            Pair<Integer, Integer> range = getItemDraggableRange(mItems);
            return range.first <= getAdapterPosition() && getAdapterPosition() <= range.second;
        }
    }
}
