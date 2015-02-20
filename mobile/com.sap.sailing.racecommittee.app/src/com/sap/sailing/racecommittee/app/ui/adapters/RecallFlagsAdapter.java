package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;

import java.util.ArrayList;

public class RecallFlagsAdapter extends BaseAdapter{

    public class RecallFlag {
        public String first_line;
        public String second_line;
        public String flag;
        public Boolean touched = false;

        public RecallFlag(String line1, String line2, String flag) {
            this.first_line = line1;
            this.second_line = line2;
            this.flag = flag;
        }

    }

    private Context mContext;
    private ArrayList<RecallFlag> mFlags;
    private RecallFlagItemClick mListener;

    public RecallFlagsAdapter(Context context) {
        mContext = context;
        mFlags = new ArrayList<>();
        mFlags.add(new RecallFlag("Xray", "Individual Recall", "XRAY"));
        mFlags.add(new RecallFlag("1st Sub", "General Recall", "FIRSTSUBSTITUTE"));

        if (context instanceof RecallFlagItemClick) {
            mListener = (RecallFlagItemClick) context;
        }
    }

    @Override
    public int getCount() {
        return mFlags.size();
    }

    @Override
    public RecallFlag getItem(int position) {
        return mFlags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.flag_list_item, parent, false);
        }

        final RecallFlag flag = getItem(position);

        final ImageView flagImage = ViewHolder.get(convertView, R.id.flag);
        if (flagImage != null) {
            flagImage.setImageDrawable(FlagsResources.getFlagDrawable(mContext, flag.flag, 48));
        }

        final TextView first_line = ViewHolder.get(convertView, R.id.first_line);
        if (first_line != null) {
            first_line.setText(flag.first_line);
        }

        final TextView second_line = ViewHolder.get(convertView, R.id.second_line);
        if (second_line != null) {
            second_line.setText(flag.second_line);
        }

        final Button confirm = ViewHolder.get(convertView, R.id.confirm);
        if (confirm != null) {
            confirm.setVisibility(View.GONE);
            if (flag.touched) {
                confirm.setVisibility(View.VISIBLE);
            }

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(flag);
                    }
                }
            });
        }

        final RelativeLayout layout = ViewHolder.get(convertView, R.id.line);
        if (layout != null) {
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (RecallFlag item : mFlags) {
                        item.touched = item.flag.equals(flag.flag);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        return convertView;
    }

    public interface RecallFlagItemClick {
        void onClick(RecallFlag flag);
    }
}
