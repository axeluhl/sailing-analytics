package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.CourseListDataElementWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.BaseDraggableSwipeAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.BaseDraggableSwipeViewHolder;
import com.sap.sailing.racecommittee.app.ui.utils.MarkImageHelper;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

public class CourseElementAdapter extends BaseDraggableSwipeAdapter<CourseElementAdapter.ViewHolder> {

    private final static String TAG = CourseElementAdapter.class.getName();

    private Context mContext;
    private List<CourseListDataElementWithIdImpl> mElements;
    private MarkImageHelper mImageHelper;
    private ElementLongClick mListener;
    private EventListener mEventListener;

    private boolean mEditable;

    public CourseElementAdapter(Context context, ArrayList<CourseListDataElementWithIdImpl> elements,
        MarkImageHelper imageHelper, boolean editable) {
        mContext = context;
        mElements = elements;
        mImageHelper = imageHelper;
        mEditable = editable;

        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.ess_course_waypoint_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        CourseListDataElement element = mElements.get(position);

        holder.dragHandle.setVisibility((mEditable) ? View.VISIBLE : View.GONE);

        holder.leftText.setVisibility(View.GONE);
        holder.leftImage.setVisibility(View.GONE);
        if (element.getLeftMark() != null) {
            holder.leftText.setText(element.getLeftMark().getName());
            holder.leftText.setVisibility(View.VISIBLE);

            int resId = mImageHelper.resolveMarkImage(element.getLeftMark());
            holder.leftImage.setImageResource(resId);
            holder.leftImage.setVisibility(View.VISIBLE);
        }

        holder.roundingDirection.setVisibility(View.GONE);
        if (element.getPassingInstructions() != null) {
            holder.roundingDirection.setText(getDisplayValueForRounding(element.getPassingInstructions()));
            holder.roundingDirection.setVisibility(View.VISIBLE);
        }

        holder.rightText.setVisibility(View.GONE);
        holder.rightImage.setVisibility(View.GONE);
        if (element.getRightMark() != null) {
            holder.rightText.setText(element.getRightMark().getName());
            holder.rightText.setVisibility(View.VISIBLE);

            int resId = mImageHelper.resolveMarkImage(element.getRightMark());
            holder.rightImage.setImageResource(resId);
            holder.rightImage.setVisibility(View.VISIBLE);
        }

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
    }

    @Override
    public int getItemCount() {
        if (mElements != null) {
            return mElements.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return mElements.get(position).getId();
    }

    public void setListener(ElementLongClick listener) {
        mListener = listener;
    }

    public void setEventListener(EventListener listener) {
        mEventListener = listener;
    }

    protected String getDisplayValueForRounding(PassingInstruction direction) {
        if (PassingInstruction.Gate.equals(direction)) {
            return "G";
        }
        if (PassingInstruction.Port.equals(direction)) {
            return "P";
        }
        if (PassingInstruction.Starboard.equals(direction)) {
            return "S";
        }
        if (PassingInstruction.Line.equals(direction)) {
            return "L";
        }
        if (PassingInstruction.Offset.equals(direction)) {
            return "O";
        }
        return "";
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
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        ExLog.i(mContext, TAG, "onMoveItem(" + fromPosition + ", " + toPosition + ")");

        if (fromPosition == toPosition) {
            return;
        }

        CourseListDataElementWithIdImpl item = mElements.get(fromPosition);
        mElements.remove(item);
        mElements.add(toPosition, item);

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onSetSwipeBackground(ViewHolder viewHolder, int type) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewHolder.itemView.setBackground(background);
        } else {
            viewHolder.itemView.setBackgroundDrawable(background);
        }
    }

    @Override
    public int onSwipeItem(ViewHolder holder, int result) {
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
        int position = holder.getAdapterPosition();
        CourseListDataElementWithIdImpl element = mElements.get(position);

        switch (reaction) {
        case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM:
            mElements.remove(element);
            notifyItemRemoved(position);

            if (mEventListener != null) {
                mEventListener.onItemRemoved(position);
            }
            break;

        default:
            break;
        }
    }

    public interface ElementLongClick {
        void onItemLongClick(CourseListDataElementWithIdImpl element);
    }

    public interface EventListener {
        void onItemRemoved(int position);
    }

    protected class ViewHolder extends BaseDraggableSwipeViewHolder implements View.OnLongClickListener {

        public ViewGroup container;
        public ImageView dragHandle;
        public TextView leftText;
        public ImageView leftImage;
        public TextView roundingDirection;
        public TextView rightText;
        public ImageView rightImage;

        public ViewHolder(View itemView) {
            super(itemView);

            if (mEditable) {
                itemView.setOnLongClickListener(this);
            }

            container = (ViewGroup) itemView.findViewById(R.id.container);
            dragHandle = (ImageView) itemView.findViewById(R.id.drag_handle);
            leftText = (TextView) itemView.findViewById(R.id.column_left_text);
            leftImage = (ImageView) itemView.findViewById(R.id.column_left_image);
            roundingDirection = (TextView) itemView.findViewById(R.id.rounding_direction);
            rightText = (TextView) itemView.findViewById(R.id.column_right_text);
            rightImage = (ImageView) itemView.findViewById(R.id.column_right_image);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                mListener.onItemLongClick(mElements.get(getAdapterPosition()));
                return true;
            }

            return false;
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }
    }
}
