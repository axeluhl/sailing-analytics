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
            return R.drawable.ap_flag;

        case BLACK:
            return R.drawable.black_flag;

        case BRAVO:
            return R.drawable.bravo;

        case BLUE:
            return R.drawable.blue_flag;

        case CLASS:
            return R.drawable.generic_class;

        case ESSONE:
            return R.drawable.one_min_flag;

        case ESSTHREE:
            return R.drawable.three_min_flag;

        case ESSTWO:
            return R.drawable.two_min_flag;

        case FIRSTSUBSTITUTE:
            return R.drawable.first_substitute_flag;

        case FOXTROTT:
            return R.drawable.foxtrott_flag;

        case GOLF:
            return R.drawable.golf_flag;

        case HOTEL:
            return R.drawable.hotel_flag;

        case INDIA:
            return R.drawable.india_flag;

        case JURY:
            return R.drawable.jury_flag;

        case NOVEMBER:
            return R.drawable.november_flag;

        case PAPA:
            return R.drawable.papa_flag;

        case XRAY:
            return R.drawable.xray_flag;

        case ZULU:
            return R.drawable.zulu_flag;

        default:
            return R.drawable.alpha_flag;
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
            mFlag.setImageDrawable(mContext.getResources().getDrawable(getResId(mStartMode.getFlag())));
        }

        mFlagName = ViewHolder.get(convertView, R.id.flag_name);
        if (mFlagName != null) {
            mFlagName.setText(mStartMode.getFlag());
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
