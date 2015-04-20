package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorsWithIdImpl;
import com.sap.sailing.racecommittee.app.utils.ColorHelper;
import com.sap.sse.common.Util;

import java.io.Serializable;
import java.util.ArrayList;

public class FinishListAdapter extends BaseDraggableSwipeAdapter<FinishListAdapter.ViewHolder> {

    private final static String TAG = FinishListAdapter.class.getName();

    private Context mContext;
    private ArrayList<CompetitorsWithIdImpl> mCompetitor;
    private FinishEvents mListener;

    public FinishListAdapter(Context context, ArrayList<CompetitorsWithIdImpl> competitor) {
        mContext = context;
        mCompetitor = competitor;

        setHasStableIds(true);
    }

    public void setListener(FinishEvents listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.race_positioning_draggable_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CompetitorsWithIdImpl item = mCompetitor.get(position);

        if (item.getReason().equals(MaxPointsReason.NONE)) {
            holder.position.setText(String.valueOf(position + 1));
        } else {
            holder.position.setText(item.getReason().name());
        };
        holder.competitor.setText(item.getText());

        int dragState = holder.getDragStateFlags();

        if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) {
            int bgColor;
            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                bgColor = ColorHelper.getThemedColor(mContext, R.attr.sap_gray);
            } else {
                bgColor = mContext.getResources().getColor(android.R.color.transparent);
            }
            holder.container.setBackgroundColor(bgColor);
        }
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
    public boolean onCheckCanStartDrag(ViewHolder holder, int x, int y) {
        ExLog.i(mContext, TAG, "onCheckCanStartDrag(" + x + ", " + y);

        // x, y --- relative from the itemView's top-left
        View containerView = holder.container;
        View dragHandleView = holder.dragHandle;

        int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder viewHolder) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        ExLog.i(mContext, TAG, "onMoveItem(" + fromPosition + ", " + toPosition + ")");

        if (fromPosition == toPosition) {
            return;
        }

        CompetitorsWithIdImpl item = mCompetitor.get(fromPosition);
        mCompetitor.remove(item);
        mCompetitor.add(toPosition, item);
        if (mListener != null) {
            mListener.afterMoved();
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int x, int y) {
        if (onCheckCanStartDrag(holder, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
        } else {
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
        }
    }

    @Override
    public void onSetSwipeBackground(ViewHolder viewHolder, int type) {
        // don't change background color
    }

    @Override
    public int onSwipeItem(ViewHolder viewHolder, int result) {
        switch (result) {
        case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
        case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
            return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;

        default:
            return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(ViewHolder holder, int result, int reaction) {

        switch (reaction) {
        case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM:
            int position = holder.getAdapterPosition();

            if (mListener != null) {
                mListener.onItemRemoved(mCompetitor.get(position));
                mCompetitor.remove(position);
                notifyItemRemoved(position);
            }
            break;

        default:
            break;
        }
    }

    public class ViewHolder extends BaseDraggableSwipeViewHolder implements View.OnLongClickListener {

        public View container;
        public View dragHandle;
        public TextView position;
        public TextView competitor;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(this);

            container = itemView.findViewById(R.id.container);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            position = (TextView) itemView.findViewById(R.id.position);
            competitor = (TextView) itemView.findViewById(R.id.competitor);
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

    public interface FinishEvents {
        void afterMoved();
        void onItemRemoved(CompetitorsWithIdImpl item);
        void onLongClick(CompetitorsWithIdImpl item);
    }
}
