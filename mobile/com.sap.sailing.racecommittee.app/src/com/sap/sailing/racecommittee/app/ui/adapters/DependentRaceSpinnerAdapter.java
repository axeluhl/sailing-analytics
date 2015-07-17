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
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public class DependentRaceSpinnerAdapter implements SpinnerAdapter {

    private static final int HEADER = 0;
    private static final int DATA = 1;

    private Context mContext;
    private int mLayout;
    private int mSelectedItem = -1;

    private ArrayList<RaceData> mData;

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
            mainText.setText(mData.get(position).getMainText());
        }

        TextView subTextView = ViewHolder.get(layout, android.R.id.text2);
        String subText = mData.get(position).getSubText();
        if (subTextView != null) {
            if (getItemViewType(position) == HEADER) {
                layout.setBackgroundColor(ThemeHelper.getColor(mContext, R.attr.sap_gray_black_20));
                if (subText != null) {
                    subTextView.setTextColor(ThemeHelper.getColor(mContext, R.attr.sap_light_gray));
                    subTextView.setText(subText);
                    subTextView.setVisibility(View.VISIBLE);
                } else {
                    subTextView.setVisibility(View.GONE);
                }
                layout.setClickable(true);
                if (mainText != null) {
                    mainText.setTextColor(ThemeHelper.getColor(mContext, R.attr.sap_light_gray));
                }
            } else {
                if (mSelectedItem == position) {
                    layout.setBackgroundColor(ThemeHelper.getColor(mContext, R.attr.sap_light_gray));
                } else {
                    layout.setBackgroundColor(ThemeHelper.getColor(mContext, R.attr.sap_gray_black_30));
                }
                subTextView.setVisibility(View.GONE);
                layout.setClickable(false);
                if (mainText != null) {
                    mainText.setTextColor(ThemeHelper.getColor(mContext, R.attr.white));
                }
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
        if (text != null && mData.get(position).getRace() != null) {
            String raceName = mData.get(position).getRace().getName();
            String additional = RaceHelper.getFleetSeries(mData.get(position).getRace());
            if (!TextUtils.isEmpty(additional)) {
                additional += " - ";
            }
            additional += RaceHelper.getRaceGroupName(mData.get(position).getRace());
            if (!TextUtils.isEmpty(additional)) {
                raceName += " (" + additional + ")";
            }
            text.setText(raceName);
        }

        return layout;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).getRace() == null) {
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

    public void add(RaceData data) {
        mData.add(data);
    }

    public void setSelected(int position) {
        mSelectedItem = position;
    }

    public ManagedRace getRaceAtPosition(int position) {
        return mData.get(position).getRace();
    }

    public static class RaceData {
        private String mText1;
        private String mText2;
        private ManagedRace mRace;

        public RaceData(String mainText, String subText, ManagedRace race) {
            mText1 = mainText;
            mText2 = subText;
            mRace = race;
        }

        public String getMainText() {
            return mText1;
        }

        public String getSubText() {
            return mText2;
        }

        public ManagedRace getRace() {
            return mRace;
        }
    }
}
