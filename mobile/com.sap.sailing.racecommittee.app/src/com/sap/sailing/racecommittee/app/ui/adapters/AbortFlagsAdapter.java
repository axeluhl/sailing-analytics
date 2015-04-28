package com.sap.sailing.racecommittee.app.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;

import java.util.ArrayList;

public class AbortFlagsAdapter extends BaseFlagsAdapter {

    public class AbortFlag extends FlagItem {

        public AbortFlag(String line1, String line2, String fileName, Flags flag) {
            super(line1, line2, fileName, false, flag);
        }
    }

    private Context mContext;
    private ArrayList<AbortFlag> mFlags;
    private AbortFlagItemClick mListener;

    public AbortFlagsAdapter(Context context, AbortFlagItemClick listener, Flags flags) {
        mContext = context;
        mListener = listener;

        mFlags = new ArrayList<>();
        addFlag(flags.name().toLowerCase(), Flags.NONE);
        addFlag(flags.name().toLowerCase(), Flags.HOTEL);
        addFlag(flags.name().toLowerCase(), Flags.ALPHA);
    }

    private void addFlag(String primaryFlag, Flags otherFlag) {
        int flagNameId;
        int flagDescId;
        String flagName = null;
        String flagDesc = null;
        String secondFlag = "";

        if (otherFlag != Flags.NONE) {
            secondFlag = "_" + otherFlag.name().toLowerCase();
        }

        flagNameId = mContext.getResources().getIdentifier("string/flag_" + primaryFlag + secondFlag, null, mContext.getPackageName());
        if (flagNameId != 0) {
            flagName = mContext.getString(flagNameId);
        }
        flagDescId = mContext.getResources().getIdentifier("string/flag_" + primaryFlag + secondFlag + "_desc", null, mContext.getPackageName());
        if (flagDescId != 0) {
            flagDesc = mContext.getString(flagDescId);
        }
        if (!TextUtils.isEmpty(flagName) && !TextUtils.isEmpty(flagDesc)) {
            mFlags.add(new AbortFlag(flagName, flagDesc, "flag_" + primaryFlag + secondFlag + "_96dp", otherFlag));
        }
    }

    @Override
    public int getCount() {
        return mFlags.size();
    }

    @Override
    public AbortFlag getItem(int position) {
        return mFlags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.flag_list_item, parent, false);
        }

        final AbortFlag item = getItem(position);

        final ImageView flagImage = ViewHolder.get(convertView, R.id.flag);
        if (flagImage != null) {
            Drawable flagDrawable = null;
            flagImage.setVisibility(View.INVISIBLE);
            int flagResId = mContext.getResources().getIdentifier(item.file_name, "drawable", mContext.getPackageName());
            if (flagResId != 0) {
                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    flagDrawable = mContext.getDrawable(flagResId);
                } else {
                    flagDrawable = mContext.getResources().getDrawable(flagResId);
                }
            } else {
                flagDrawable = BitmapHelper.getAttrDrawable(mContext, item.file_name);
            }
            if (flagDrawable != null) {
                flagImage.setImageDrawable(flagDrawable);
                flagImage.setVisibility(View.VISIBLE);
            }
        }

        final TextView first_line = ViewHolder.get(convertView, R.id.first_line);
        if (first_line != null) {
            first_line.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(item.first_line)) {
                first_line.setVisibility(View.VISIBLE);
                first_line.setText(item.first_line);
            }
        }

        final TextView second_line = ViewHolder.get(convertView, R.id.second_line);
        if (second_line != null) {
            second_line.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(item.second_line)) {
                second_line.setVisibility(View.VISIBLE);
                second_line.setText(item.second_line);
            }
        }

        final Button confirm = ViewHolder.get(convertView, R.id.confirm);
        if (confirm != null && mListener != null) {
            confirm.setVisibility(View.GONE);
            if (item.touched) {
                confirm.setVisibility(View.VISIBLE);
            }

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(item.flag);
                }
            });
        }

        final RelativeLayout layout = ViewHolder.get(convertView, R.id.line);
        if (layout != null) {
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (AbortFlag flag : mFlags) {
                        flag.touched = flag.file_name.equals(item.file_name);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        return convertView;
    }

    public interface AbortFlagItemClick {
        void onClick(Flags flag);
    }
}
