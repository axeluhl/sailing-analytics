package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;

import java.util.ArrayList;

public class MoreFlagsAdapter extends BaseFlagsAdapter {

    public class MoreFlag extends FlagItem {

        public MoreFlag(String line1, String line2, String flag) {
            super(line1, line2, flag);
        }
    }

    private Context mContext;
    private ArrayList<MoreFlag> mFlags;
    private MoreFlagItemClick mListener;

    public MoreFlagsAdapter(Context context) {
        mContext = context;
        mFlags = new ArrayList<>();
        mFlags.add(new MoreFlag(context.getString(R.string.flag_blue), context.getString(R.string.flag_blue_desc), "flag_blue_96dp"));

        if (context instanceof MoreFlagItemClick) {
            mListener = (MoreFlagItemClick) context;
        }
    }

    @Override
    public int getCount() {
        return mFlags.size();
    }

    @Override
    public MoreFlag getItem(int position) {
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

        final MoreFlag flag = getItem(position);

        final ImageView flagImage = ViewHolder.get(convertView, R.id.flag);
        if (flagImage != null) {
            flagImage.setVisibility(View.INVISIBLE);
            int flagResId = mContext.getResources().getIdentifier(flag.flag, "drawable", mContext.getPackageName());
            if (flagResId != 0) {
                Drawable flagDrawable = mContext.getResources().getDrawable(flagResId);
                flagImage.setImageDrawable(flagDrawable);
                flagImage.setVisibility(View.VISIBLE);
            }
        }

        final TextView first_line = ViewHolder.get(convertView, R.id.first_line);
        if (first_line != null) {
            first_line.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(flag.first_line)) {
                first_line.setVisibility(View.VISIBLE);
                first_line.setText(flag.first_line);
            }
        }

        final TextView second_line = ViewHolder.get(convertView, R.id.second_line);
        if (second_line != null) {
            second_line.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(flag.second_line)) {
                second_line.setVisibility(View.VISIBLE);
                second_line.setText(flag.second_line);
            }
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
                    for (MoreFlag item : mFlags) {
                        item.touched = item.flag.equals(flag.flag);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        return convertView;
    }

    public interface MoreFlagItemClick {
        void onClick(MoreFlag flag);
    }
}
