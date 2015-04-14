package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.MarkImageHelper;

import java.util.ArrayList;
import java.util.List;

public class CourseMarkAdapter extends RecyclerView.Adapter<CourseMarkAdapter.ViewHolder> {

    private Context mContext;
    private List<Mark> mMarks;
    private MarkImageHelper mImageHelper;
    private MarkLongClick mListener;

    public CourseMarkAdapter(Context context, ArrayList<Mark> marks, MarkImageHelper imageHelper) {
        mContext = context;
        mMarks = marks;
        mImageHelper = imageHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.ess_course_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Mark mark = mMarks.get(position);

        int drawable = mImageHelper.resolveMarkImage(mark);
        holder.imageView.setImageResource(drawable);
        holder.textView.setText(mark.getName());
    }

    @Override
    public int getItemCount() {
        if (mMarks != null) {
            return mMarks.size();
        }
        return 0;
    }

    public void setListener(MarkLongClick listener) {
        mListener = listener;
    }

    public interface MarkLongClick {
        void onItemLongClick(Mark mark);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView textView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(this);
            textView = (TextView) itemView.findViewById(R.id.cell_text);
            imageView = (ImageView) itemView.findViewById(R.id.cell_image);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                mListener.onItemLongClick(mMarks.get(getAdapterPosition()));
                return true;
            }
            return false;
        }
    }
}
