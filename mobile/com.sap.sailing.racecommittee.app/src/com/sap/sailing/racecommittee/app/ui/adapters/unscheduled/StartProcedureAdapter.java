package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

import java.util.List;

import android.text.TextUtils;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public class StartProcedureAdapter extends BaseAdapter implements OnClickListener {

    private Context mContext;
    private RacingProcedureTypeClick mListener;
    private List<StartProcedure> mList;

    public StartProcedureAdapter(Context context, List<StartProcedure> list, RacingProcedureTypeClick listener) {
        mContext = context;
        mList = list;
        mListener = listener;
    }
    
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public StartProcedure getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.race_schedule_procedure_row, parent, false);
        }

        TextView startProcedure = ViewHolder.get(convertView, R.id.start_procedure);
        if (startProcedure != null) {
            startProcedure.setText(getItem(position).toString());
            startProcedure.setTextColor(ThemeHelper.getColor(mContext, R.attr.sap_light_gray));
            if (getItem(position).isChecked()) {
                startProcedure.setTextColor(ThemeHelper.getColor(mContext, R.attr.white));
            }
        }

        ImageView checked = ViewHolder.get(convertView, R.id.checked);
        if (checked != null) {
            checked.setTag(position);
            checked.setVisibility(View.INVISIBLE);
            if (getItem(position).isChecked()) {
                checked.setVisibility(View.VISIBLE);
            }
        }

        ImageView more = ViewHolder.get(convertView, R.id.set_path_finder);
        if (more != null) {
            String className = getItem(position).getClassName();
            if (!TextUtils.isEmpty(className)) {
                more.setVisibility(View.VISIBLE);
            } else {
                more.setVisibility(View.INVISIBLE);
            }
        }

        convertView.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View view) {
        for (StartProcedure procedure : mList) {
            procedure.setChecked(false);
        }
        
        Integer position = (Integer) ViewHolder.get(view, R.id.checked).getTag();
        if (position != null) {
            StartProcedure procedure = getItem(position);
            procedure.setChecked(true);
            if (mListener != null) {
                mListener.onClick(procedure.getProcedureType(), procedure.getClassName());
            }
        }

        notifyDataSetChanged();
    }

    public interface RacingProcedureTypeClick {
        void onClick(RacingProcedureType procedureType, String className);
    }
}
