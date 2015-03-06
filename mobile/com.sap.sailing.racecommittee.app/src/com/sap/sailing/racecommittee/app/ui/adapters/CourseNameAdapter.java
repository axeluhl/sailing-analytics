package com.sap.sailing.racecommittee.app.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.CourseFragmentName;

import java.util.Collections;
import java.util.List;

/**
* Created by mars3142 on 04.03.15.
*/
public class CourseNameAdapter extends BaseAdapter implements View.OnClickListener {

    private Context mContext;
    private List<String> mCourses;
    private CourseItemClick mListener;

    public CourseNameAdapter(Context context, List<String> courses, CourseItemClick listener) {
        mContext = context;
        mCourses = courses;
        mListener = listener;

        Collections.sort(mCourses, new NaturalComparator());
    }

    @Override
    public int getCount() {
        return mCourses.size();
    }

    @Override
    public String getItem(int position) {
        return mCourses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.flag_list_item, parent, false);
        }

        TextView firstLine = ViewHolder.get(convertView, R.id.first_line);
        if (firstLine != null) {
            firstLine.setText(getItem(position));
        }

        TextView secondLine = ViewHolder.get(convertView, R.id.second_line);
        if (secondLine != null) {
            secondLine.setVisibility(View.GONE);
        }

        ImageView flag = ViewHolder.get(convertView, R.id.flag);
        if (flag != null) {
            flag.setImageDrawable(mContext.getResources().getDrawable(R.drawable.course_updown_48dp));
        }

        convertView.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            TextView textView = (TextView) view.findViewById(R.id.first_line);
            if (textView != null) {
                mListener.onClick(textView.getText().toString());
            }
        }
    }

    public interface CourseItemClick {
        void onClick(String course);
    }
}
