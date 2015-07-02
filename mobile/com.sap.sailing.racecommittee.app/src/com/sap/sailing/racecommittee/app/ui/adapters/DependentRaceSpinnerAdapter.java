package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

import java.util.ArrayList;

public class DependentRaceSpinnerAdapter implements SpinnerAdapter {

    private static final int HEADER = 0;
    private static final int DATA = 1;

    private Context mContext;
    private int mLayout;

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

        TextView subText = ViewHolder.get(layout, android.R.id.text2);
        if (subText != null) {
            if (getItemViewType(position) == HEADER) {
                subText.setText("SubText");
                subText.setVisibility(View.VISIBLE);
                layout.setClickable(true);
            } else {
                subText.setVisibility(View.GONE);
                layout.setClickable(false);
            }
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
    public Object getItem(int position) {
        return position;
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
        if (text != null) {
            text.setText("- " + mData.get(position) + " -");
        }

        return layout;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 10 == 0) {
            return HEADER;
        }
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
}
