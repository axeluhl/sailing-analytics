package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;

public class StartModeAdapter extends BaseAdapter implements OnClickListener {

    private Context mContext;
    private ImageView mChecked;
    private StartModeClick mListener;

    private List<StartMode> mStartMode;

    public StartModeAdapter(Context context, List<StartMode> startMode, StartModeClick listener) {
        mContext = context;
        mStartMode = startMode;
        mListener = listener;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.race_schedule_mode_row, parent, false);
        }

        StartMode startMode = getItem(position);
        ImageView flag = ViewHolder.get(convertView, R.id.flag);
        if (flag != null) {
            flag.setImageDrawable(FlagsResources.getFlagDrawable(mContext, startMode.getFlagName(), 64));
        }

        TextView flagName = ViewHolder.get(convertView, R.id.flag_name);
        if (flagName != null) {
            flagName.setText(startMode.getFlagName());
            flagName.setTextColor(mContext.getResources().getColor(R.color.sap_light_gray));
            if (startMode.isChecked()) {
                flagName.setTextColor(mContext.getResources().getColor(R.color.white));
            }
        }

        mChecked = ViewHolder.get(convertView, R.id.checked);
        if (mChecked != null) {
            mChecked.setTag(position);
            mChecked.setVisibility(View.INVISIBLE);
            if (startMode.isChecked()) {
                mChecked.setVisibility(View.VISIBLE);
            }
        }

        convertView.setOnClickListener(this);
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
                    if (mListener != null) {
                        mListener.onClick(startMode);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface StartModeClick {
        void onClick(StartMode startMode);
    }
}
