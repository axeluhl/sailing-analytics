package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import java.util.ArrayList;
import java.util.List;

import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.CourseListDataElementWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.BaseDraggableSwipeAdapter;
import com.sap.sailing.racecommittee.app.ui.utils.MarkImageHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CourseElementAdapter extends BaseDraggableSwipeAdapter<RecyclerView.ViewHolder> {

    private final static String TAG = CourseElementAdapter.class.getName();

    public final static int TOUCH_LEFT_AREA = 1;
    public final static int TOUCH_TYPE_AREA = 2;
    public final static int TOUCH_RIGHT_AREA = 3;

    private final static int ITEM_VIEW = 0;
    private final static int ADD_VIEW = 1;

    private Context mContext;
    private List<CourseListDataElementWithIdImpl> mElements;
    private MarkImageHelper mImageHelper;
    private ItemClick mItemClickListener;
    private EventListener mEventListener;

    private boolean mEditable;

    public CourseElementAdapter(Context context, ArrayList<CourseListDataElementWithIdImpl> elements, MarkImageHelper imageHelper, boolean editable) {
        mContext = context;
        mElements = elements;
        mImageHelper = imageHelper;
        mEditable = editable;

        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout;
        switch (viewType) {
            case ADD_VIEW:
                layout = LayoutInflater.from(mContext).inflate(R.layout.course_marks_waypoint_new_item, parent, false);
                return new AddItemHolder(layout);

            default:
                layout = LayoutInflater.from(mContext).inflate(R.layout.course_marks_waypoint_item, parent, false);
                return new ItemViewHolder(layout);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            CourseListDataElement element = mElements.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.dragHandle.setVisibility((mEditable) ? View.VISIBLE : View.GONE);
            itemHolder.leftText.setVisibility(View.GONE);
            itemHolder.leftImage.setVisibility(View.GONE);
            if (element.getLeftMark() != null) {
                itemHolder.leftText.setText(element.getLeftMark().getName());
                itemHolder.leftText.setVisibility(View.VISIBLE);
                itemHolder.leftImage.setImageDrawable(mImageHelper.resolveMarkImage(mContext, element.getLeftMark()));
                itemHolder.leftImage.setVisibility(View.VISIBLE);
            }
            itemHolder.roundingDirection.setVisibility(View.GONE);
            if (element.getPassingInstructions() != null) {
                itemHolder.roundingDirection.setText(getDisplayValueForRounding(element.getPassingInstructions()));
                itemHolder.roundingDirection.setVisibility(View.VISIBLE);
            }
            itemHolder.rightText.setVisibility(View.GONE);
            itemHolder.rightImage.setVisibility(View.GONE);
            itemHolder.addItem.setVisibility(View.GONE);
            if (element.getRightMark() != null) {
                itemHolder.rightText.setText(element.getRightMark().getName());
                itemHolder.rightText.setVisibility(View.VISIBLE);
                itemHolder.rightImage.setImageDrawable(mImageHelper.resolveMarkImage(mContext, element.getRightMark()));
                itemHolder.rightImage.setVisibility(View.VISIBLE);
            } else {
                if (element.getPassingInstructions() != null) {
                    if (PassingInstruction.Gate.equals(element.getPassingInstructions()) ||
                            PassingInstruction.Line.equals(element.getPassingInstructions())) {
                        itemHolder.addItem.setVisibility(View.VISIBLE);
                    }
                }
            }
            int dragState = itemHolder.getDragStateFlags();
            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) {
                int bgColor;
                if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                    bgColor = ThemeHelper.getColor(mContext, R.attr.sap_gray);
                } else {
                    bgColor = mContext.getResources().getColor(android.R.color.transparent);
                }
                itemHolder.container.setBackgroundColor(bgColor);
            }
        } else if (holder instanceof AddItemHolder) {
            AddItemHolder itemHolder = (AddItemHolder) holder;
            itemHolder.hintText.setVisibility((position == 0) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (mEditable) {
            if (mElements != null && mElements.size() != 0) {
                CourseListDataElementWithIdImpl element = mElements.get(mElements.size() - 1);
                if (element.getRightMark() == null) {
                    if (PassingInstruction.Gate.equals(element.getPassingInstructions()) ||
                            PassingInstruction.Line.equals(element.getPassingInstructions())) {
                        return mElements.size();
                    }
                }
                return mElements.size() + 1;
            }
            return 1;
        } else {
            return mElements.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mElements != null && mElements.size() != position) {
            return ITEM_VIEW;
        }
        return ADD_VIEW;
    }

    @Override
    public long getItemId(int position) {
        if (mElements != null && mElements.size() != position) {
            return mElements.get(position).getId();
        } else {
            return super.getItemId(position);
        }
    }

    public void setItemClickListener(ItemClick listener) {
        mItemClickListener = listener;
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
        if (PassingInstruction.Single_Unknown.equals(direction)) {
            return "U";
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
    public boolean onCheckCanStartDrag(RecyclerView.ViewHolder holder, int position, int x, int y) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            // x, y --- relative from the itemView's top-left
            View containerView = itemHolder.container;
            View dragHandleView = itemHolder.dragHandle;

            int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
            int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

            return hitTest(dragHandleView, x - offsetX, y - offsetY);
        } else {
            return false;
        }
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(RecyclerView.ViewHolder holder, int position) {
        if (mElements == null) {
            return null;
        }
        return new ItemDraggableRange(0, mElements.size() - 1);
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        CourseListDataElementWithIdImpl item = mElements.get(fromPosition);
        mElements.remove(item);
        mElements.add(toPosition, item);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int onGetSwipeReactionType(RecyclerView.ViewHolder holder, int position, int x, int y) {
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
        } else {
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
        }
    }

    @Override
    public void onSetSwipeBackground(RecyclerView.ViewHolder holder, int position, int type) {
        if (holder instanceof ItemViewHolder) {
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

            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.container.setBackgroundColor(ThemeHelper.getColor(mContext, R.attr.sap_gray_black_30));
            Drawable background = BitmapHelper.getAttrDrawable(mContext, bgRes);
            BitmapHelper.setBackground(itemHolder.itemView, background);
        }
    }

    @Override
    public int onSwipeItem(RecyclerView.ViewHolder holder, int position, int result) {
        if (holder instanceof ItemViewHolder) {
            switch (result) {
                case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;

                default:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
            }
        } else {
            return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(RecyclerView.ViewHolder holder, int position, int result, int reaction) {
        ExLog.i(mContext, TAG, "onPerformAfterSwipeReaction() called with: " + "holder = [" + holder + "], result = [" + result + "], reaction = [" + reaction + "]");
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

    public interface ItemClick {
        void onAddItemClick();

        void onItemEditClick(int type, CourseListDataElementWithIdImpl element);
    }

    public interface EventListener {
        void onItemRemoved(int position);
    }

    public class AddItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView addItem;
        private TextView hintText;

        public AddItemHolder(View itemView) {
            super(itemView);

            addItem = ViewHelper.get(itemView, R.id.add_item);
            if (addItem != null) {
                addItem.setOnClickListener(this);
            }
            hintText = ViewHelper.get(itemView, R.id.hint_text);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onAddItemClick();
            }
        }
    }

    public class ItemViewHolder extends AbstractDraggableSwipeableItemViewHolder implements View.OnClickListener {

        public ViewGroup container;
        public View dragHandle;
        public View leftColumn;
        public TextView leftText;
        public ImageView leftImage;
        public TextView roundingDirection;
        public View rightColumn;
        public TextView rightText;
        public ImageView rightImage;
        public ImageView addItem;

        public ItemViewHolder(View itemView) {
            super(itemView);

            container = ViewHelper.get(itemView, R.id.container);
            dragHandle = ViewHelper.get(itemView, R.id.drag_handle);
            leftColumn = ViewHelper.get(itemView, R.id.column_left);
            leftText = ViewHelper.get(itemView, R.id.column_left_text);
            leftImage = ViewHelper.get(itemView, R.id.column_left_image);
            roundingDirection = ViewHelper.get(itemView, R.id.rounding_direction);
            rightColumn = ViewHelper.get(itemView, R.id.column_right);
            rightText = ViewHelper.get(itemView, R.id.column_right_text);
            rightImage = ViewHelper.get(itemView, R.id.column_right_image);
            addItem = ViewHelper.get(itemView, R.id.add_item);

            if (mEditable) {
                if (leftColumn != null) {
                    leftColumn.setOnClickListener(this);
                }
                if (roundingDirection != null) {
                    roundingDirection.setOnClickListener(this);
                }
                if (rightColumn != null) {
                    rightColumn.setOnClickListener(this);
                }
                if (addItem != null) {
                    addItem.setOnClickListener(this);
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                switch (v.getId()) {
                    case R.id.column_left:
                        mItemClickListener.onItemEditClick(TOUCH_LEFT_AREA, mElements.get(getAdapterPosition()));
                        break;

                    case R.id.column_right:
                        if (addItem.getVisibility() == View.GONE) {
                            mItemClickListener.onItemEditClick(TOUCH_RIGHT_AREA, mElements.get(getAdapterPosition()));
                        }
                        break;

                    case R.id.rounding_direction:
                        mItemClickListener.onItemEditClick(TOUCH_TYPE_AREA, mElements.get(getAdapterPosition()));
                        break;

                    default:
                        mItemClickListener.onAddItemClick();
                }
            }
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }
    }
}
