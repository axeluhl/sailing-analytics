package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

import java.util.List;

import com.sap.sailing.android.shared.util.ViewHolder;
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

public class StartProcedureAdapter extends BaseAdapter implements OnClickListener {

    private ImageView mChecked;
    private Context mContext;
    private List<StartProcedure> mList;
    private TextView mStartProcedure;
    
    public StartProcedureAdapter(Context context, List<StartProcedure> list) {
        mContext = context;
        mList = list;
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
            convertView = inflater.inflate(R.layout.race_schedule_procedure_change_row, parent, false);
        }
        
        mStartProcedure = ViewHolder.get(convertView, R.id.start_procedure);
        if (mStartProcedure != null) {
            mStartProcedure.setText(getItem(position).toString());
        }
        
        mChecked = (ImageView) ViewHolder.get(convertView, R.id.checked);
        if (mChecked != null) {
            mChecked.setTag(position);
            mChecked.setVisibility(View.INVISIBLE);
            if (getItem(position).isChecked()) {
                mChecked.setVisibility(View.VISIBLE);
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
            getItem(position).setChecked(true);
        }
        
        notifyDataSetChanged();
    }
}
