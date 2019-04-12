package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.util.NaturalComparator;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

        String courseName = getItem(position);
        TextView firstLine = ViewHelper.get(convertView, R.id.first_line);
        if (firstLine != null) {
            firstLine.setText(courseName);
        }

        TextView secondLine = ViewHelper.get(convertView, R.id.second_line);
        if (secondLine != null) {
            secondLine.setVisibility(View.GONE);
        }

        ImageView flag = ViewHelper.get(convertView, R.id.flag);
        if (flag != null) {
            int resId;
            if (courseName.toLowerCase(Locale.US).startsWith("i")) {
                resId = R.attr.course_updown_48dp;
            } else {
                resId = R.attr.course_triangle_48dp;
            }
            Drawable drawable;
            drawable = BitmapHelper.getAttrDrawable(mContext, resId);
            if (drawable != null) {
                flag.setImageDrawable(drawable);
            }
            flag.setVisibility(View.GONE);
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
