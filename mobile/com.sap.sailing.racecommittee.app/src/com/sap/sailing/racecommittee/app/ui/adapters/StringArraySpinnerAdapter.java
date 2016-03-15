package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;

public class StringArraySpinnerAdapter implements SpinnerAdapter {

    private ArrayList<String> mArray;
    private int mSelectedItem = -1;

    public StringArraySpinnerAdapter(@NonNull String[] data) {
        mArray = new ArrayList<>(data.length);
        Collections.addAll(mArray, data);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        View layout = convertView;
        if (layout == null) {
            layout = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView mainText = ViewHelper.get(layout, android.R.id.text1);
        if (mainText != null) {
            mainText.setText(mArray.get(position));
            mainText.setTextColor(context.getResources().getColor(R.color.constant_black));
        }

        if (mSelectedItem == position) {
            layout.setBackgroundColor(context.getResources().getColor(R.color.light_sap_light_gray));
        } else {
            layout.setBackgroundColor(context.getResources().getColor(R.color.light_sap_gray_black_30));
        }

        layout.setClickable(false);

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
        return mArray.size();
    }

    @Override
    public String getItem(int position) {
        return mArray.get(position);
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
        Context context = parent.getContext();
        View layout = convertView;
        if (layout == null) {
            layout = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView mainText = ViewHelper.get(layout, android.R.id.text1);
        if (mainText != null) {
            mainText.setText(mArray.get(position));
            mainText.setTextColor(context.getResources().getColor(R.color.constant_black));
        }

        return layout;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return (mArray.size() == 0);
    }

    public int getPosition(String item) {
        for (int i = 0; i < mArray.size(); i++) {
            if (mArray.get(i).equals(item)) {
                return i;
            }
        }
        return 0;
    }

    public void setSelected(int selected) {
        mSelectedItem = selected;
    }
}
