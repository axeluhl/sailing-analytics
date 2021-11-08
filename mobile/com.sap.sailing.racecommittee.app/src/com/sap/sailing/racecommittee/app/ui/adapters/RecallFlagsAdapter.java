package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.ArrayList;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RecallFlagsAdapter extends BaseFlagsAdapter {

    public class RecallFlag extends FlagItem {

        public RecallFlag(String line1, String line2, Flags flags) {
            super(line1, line2, flags.name(), false, flags);
        }
    }

    private Context mContext;
    private ArrayList<RecallFlag> mFlags;
    private RecallFlagItemClick mListener;

    public RecallFlagsAdapter(Context context, RacingProcedure procedure, RecallFlagItemClick listener) {
        mContext = context;
        mListener = listener;

        mFlags = new ArrayList<>();
        if (procedure.hasIndividualRecall()) {
            mFlags.add(new RecallFlag(context.getString(R.string.flag_xray), context.getString(R.string.flag_xray_desc),
                    Flags.XRAY));
        }
        mFlags.add(new RecallFlag(context.getString(R.string.flag_first_subst),
                context.getString(R.string.flag_first_subst_desc), Flags.FIRSTSUBSTITUTE));
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

        final RecallFlag item = getItem(position);

        final ImageView flagImage = ViewHelper.get(convertView, R.id.flag);
        if (flagImage != null) {
            flagImage.setImageDrawable(FlagsResources.getFlagDrawable(mContext, item.file_name, 96));
        }

        final TextView first_line = ViewHelper.get(convertView, R.id.first_line);
        if (first_line != null) {
            first_line.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(item.first_line)) {
                first_line.setVisibility(View.VISIBLE);
                first_line.setText(item.first_line);
            }
        }

        final TextView second_line = ViewHelper.get(convertView, R.id.second_line);
        if (second_line != null) {
            second_line.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(item.second_line)) {
                second_line.setVisibility(View.VISIBLE);
                second_line.setText(item.second_line);
            }
        }

        final Button confirm = ViewHelper.get(convertView, R.id.confirm);
        if (confirm != null && mListener != null) {
            confirm.setVisibility(View.GONE);
            if (item.touched) {
                confirm.setVisibility(View.VISIBLE);
            }

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(item);
                }
            });
        }

        final RelativeLayout layout = ViewHelper.get(convertView, R.id.line);
        if (layout != null) {
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (RecallFlag flag : mFlags) {
                        flag.touched = flag.file_name.equals(item.file_name);
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
