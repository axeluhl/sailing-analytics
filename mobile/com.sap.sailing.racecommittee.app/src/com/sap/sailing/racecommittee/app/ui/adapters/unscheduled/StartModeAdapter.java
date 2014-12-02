package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

import com.sap.sailing.racecommittee.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StartModeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    
    public StartModeAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.race_schedule_mode_row, parent, false);
        }
        
        return convertView;
    }
}
