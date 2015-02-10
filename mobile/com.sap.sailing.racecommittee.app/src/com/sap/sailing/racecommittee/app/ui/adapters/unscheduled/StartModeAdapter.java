package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled.StartModeFragment;

public class StartModeAdapter extends BaseAdapter implements OnClickListener {

    private ImageView mChecked;
    private Context mContext;
    private ImageView mFlag;

    private TextView mFlagName;
    private LayoutInflater mInflater;
    private List<StartMode> mStartMode;

    public StartModeAdapter(Context context, List<StartMode> startMode) {
        mContext = context;
        mInflater = (LayoutInflater) (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        mStartMode = startMode;
    }

    @Override
    public int getCount() {
        return mStartMode.size();
    }

    @Override
    public StartMode getItem(int position) {
        return mStartMode.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mStartMode.indexOf(getItem(position));
    }

    private int getResId(String res) {
        switch (Flags.valueOf(res)) {
        case AP:
            return R.drawable.flag_ap_64dp;

        case BLACK:
            return R.drawable.flag_black_64dp;

        case BRAVO:
            return R.drawable.flag_bravo_64dp;

        case BLUE:
            return R.drawable.flag_blue_64dp;

        case CLASS:
            return R.drawable.flag_class_64dp;

        case ESSONE:
            return R.drawable.one_min_flag;

        case ESSTHREE:
            return R.drawable.three_min_flag;

        case ESSTWO:
            return R.drawable.two_min_flag;

        case FIRSTSUBSTITUTE:
            return R.drawable.flag_first_substitute_64dp;

        case FOXTROTT:
            return R.drawable.flag_foxtrott_64dp;

        case GOLF:
            return R.drawable.flag_golf_64dp;

        case HOTEL:
            return R.drawable.flag_hotel_64dp;

        case INDIA:
            return R.drawable.flag_india_64dp;

        case JURY:
            return R.drawable.jury_flag;

        case NOVEMBER:
            return R.drawable.flag_november_64dp;

        case PAPA:
            return R.drawable.flag_papa_64dp;

        case XRAY:
            return R.drawable.flag_xray_64dp;

        case ZULU:
            return R.drawable.flag_zulu_64dp;

        default:
            return R.drawable.flag_alpha_64dp;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.race_schedule_mode_row, parent, false);
        }

        convertView.setOnClickListener(this);
        StartMode mStartMode = getItem(position);

        mFlag = ViewHolder.get(convertView, R.id.flag);
        if (mFlag != null) {
            mFlag.setImageDrawable(mContext.getResources().getDrawable(getResId(mStartMode.getFlagName())));
        }

        mFlagName = ViewHolder.get(convertView, R.id.flag_name);
        if (mFlagName != null) {
            mFlagName.setText(mStartMode.getFlagName());
            mFlagName.setTextColor(mContext.getResources().getColor(R.color.grey_light));
            if (mStartMode.isChecked()) {
                mFlagName.setTextColor(mContext.getResources().getColor(R.color.white));
            }
        }

        mChecked = ViewHolder.get(convertView, R.id.checked);
        if (mChecked != null) {
            mChecked.setTag(position);
            mChecked.setVisibility(View.INVISIBLE);
            if (mStartMode.isChecked()) {
                mChecked.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    @Override
    public void onClick(View view) {
        for (StartMode startMode : mStartMode) {
            startMode.setChecked(false);
        }
        mChecked = ViewHolder.get(view, R.id.checked);
        if (mChecked != null) {
            Integer position = (Integer) mChecked.getTag();
            if (position != null) {
                StartMode startMode = getItem(position);
                if (startMode != null) {
                    startMode.setChecked(true);
                }
            }
        }
        notifyDataSetChanged();
    }
}
