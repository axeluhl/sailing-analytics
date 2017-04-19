package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.List;

import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ChildPositionItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.expandable.GroupPositionItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FinishListAdapter extends BaseDraggableSwipeAdapter<FinishListAdapter.ViewHolder> {

    private Context mContext;
    private List<CompetitorResultWithIdImpl> mCompetitor;
    private FinishEvents mListener;

    public FinishListAdapter(Context context, List<CompetitorResultWithIdImpl> competitor) {
        setHasStableIds(true);
        mContext = context;
        mCompetitor = competitor;
    }

    public void setListener(FinishEvents listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.race_tracking_list_draggable_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CompetitorResultWithIdImpl item = mCompetitor.get(position);

        if (item.getMaxPointsReason().equals(MaxPointsReason.NONE)) {
            holder.position.setText(String.valueOf(position + 1));
        } else {
            holder.position.setText(null);
        }
        holder.competitor.setText(item.getCompetitorDisplayName());

        int dragState = holder.getDragStateFlags();

        if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) {
            int bgColor;
            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                bgColor = ThemeHelper.getColor(mContext, R.attr.sap_gray);
            } else {
                bgColor = mContext.getResources().getColor(android.R.color.transparent);
            }
            holder.container.setBackgroundColor(bgColor);
        }
        holder.penalty.setText(item.getMaxPointsReason().name());
        holder.penalty.setVisibility(item.getMaxPointsReason().equals(MaxPointsReason.NONE) ? View.GONE : View.VISIBLE);

        holder.dragHandle.setVisibility(item.getMaxPointsReason().equals(MaxPointsReason.NONE) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public long getItemId(int position) {
        return mCompetitor.get(position).getId();
    }

    @Override
    public int getItemCount() {
        if (mCompetitor != null) {
            return mCompetitor.size();
        }
        return 0;
    }

    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        View containerView = holder.container;
        View dragHandleView = holder.dragHandle;

        int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return dragHandleView.getVisibility() == View.VISIBLE && hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder viewHolder, int position) {
        final int start = 0;
        final int end = getFirstPenalty() - 1;

        return new GroupPositionItemDraggableRange(start, end);
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        CompetitorResultWithIdImpl item = mCompetitor.get(fromPosition);
        mCompetitor.remove(item);
        mCompetitor.add(toPosition, item);
        if (mListener != null) {
            mListener.afterMoved();
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    private int getFirstPenalty() {
        int result = getItemCount();
        for (int i = 0; i < getItemCount(); i++) {
            if (!mCompetitor.get(i).getMaxPointsReason().equals(MaxPointsReason.NONE)) {
                result = i;
                break;
            }
        }
        return result;
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
        } else {
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
        }
    }

    @Override
    public void onSetSwipeBackground(ViewHolder viewHolder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.attr.swipe_idle;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.attr.swipe_left;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.attr.swipe_right;
                break;
        }

        viewHolder.container.setBackgroundColor(ThemeHelper.getColor(mContext, R.attr.sap_gray_black_30));
        Drawable background = BitmapHelper.getAttrDrawable(mContext, bgRes);
        BitmapHelper.setBackground(viewHolder.itemView, background);
    }

    @Override
    public int onSwipeItem(ViewHolder viewHolder, int position, int result) {
        switch (result) {
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;

            default:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(ViewHolder holder, int position, int result, int reaction) {
        switch (reaction) {
            case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM:
                if (mListener != null) {
                    CompetitorResultWithIdImpl competitor = mCompetitor.get(position);
                    mCompetitor.remove(competitor);
                    notifyItemRemoved(position);
                    mListener.onItemRemoved(competitor);
                }
                break;

            default:
                break;
        }
    }

    private void setDragRange() {

    }

    public interface FinishEvents {
        void afterMoved();

        void onItemRemoved(CompetitorResultWithIdImpl item);

        void onLongClick(CompetitorResultWithIdImpl item);

        void onEditItem(CompetitorResultWithIdImpl item, int position);
    }

    public class ViewHolder extends AbstractDraggableSwipeableItemViewHolder implements View.OnLongClickListener {
        View container;
        View dragHandle;
        View editItem;
        TextView position;
        TextView vesselId;
        TextView competitor;
        TextView penalty;

        public ViewHolder(View itemView) {
            super(itemView);

            container = ViewHelper.get(itemView, R.id.container);
            dragHandle = ViewHelper.get(itemView, R.id.drag_handle);
            editItem = ViewHelper.get(itemView, R.id.edit_item);
            if (editItem != null) {
                editItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onEditItem(mCompetitor.get(getAdapterPosition()), getAdapterPosition() + 1);
                        }
                    }
                });
            }
            vesselId = ViewHelper.get(itemView, R.id.vessel_id);
            competitor = ViewHelper.get(itemView, R.id.competitor);
            penalty = ViewHelper.get(itemView, R.id.item_penalty);
            position = ViewHelper.get(itemView, R.id.position);
            if (position != null) {
                position.setOnLongClickListener(this);
            }
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                mListener.onLongClick(mCompetitor.get(getAdapterPosition()));
            }
            return false;
        }
    }
}
