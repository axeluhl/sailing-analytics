package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public class DependentRaceSpinnerAdapter implements SpinnerAdapter {

    private static final int HEADER = 0;
    private static final int DATA = 1;

    private Context mContext;
    private int mLayout;
    private int mSelectedItem = -1;

    private ArrayList<String> mData;

    public DependentRaceSpinnerAdapter(Context context, @LayoutRes int layout) {
        mContext = context;
        mLayout = layout;
        mData = new ArrayList<>();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View layout = convertView;
        if (layout == null) {
            layout = LayoutInflater.from(mContext).inflate(mLayout, parent, false);
        }

        TextView mainText = ViewHolder.get(layout, android.R.id.text1);
        if (mainText != null) {
            mainText.setText(mData.get(position));
        }

        if (mSelectedItem == position) {
            layout.setBackgroundColor(ThemeHelper.getColor(mContext, R.attr.sap_light_gray));
        } else {
            layout.setBackgroundColor(ThemeHelper.getColor(mContext, R.attr.sap_gray_black_30));
        }
        layout.setClickable(false);
        if (mainText != null) {
            mainText.setTextColor(ThemeHelper.getColor(mContext, R.attr.white));
        }

        return layout;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layout = convertView;
        if (layout == null) {
            layout = LayoutInflater.from(mContext).inflate(mLayout, parent, false);
        }

        TextView text = ViewHolder.get(layout, android.R.id.text1);
        if (text != null && position < mData.size() && !TextUtils.isEmpty(mData.get(position))) {
            String raceName = mData.get(position);
            text.setText(raceName);
        }

        return layout;
    }

    @Override
    public int getItemViewType(int position) {
        return DATA;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return (mData.size() == 0);
    }

    public void add(String data) {
        mData.add(data);
    }

    public void addUnique(String data) {
        if (!mData.contains(data)) {
            mData.add(data);
        }
    }

    public void setSelected(int position) {
        mSelectedItem = position;
    }
}
