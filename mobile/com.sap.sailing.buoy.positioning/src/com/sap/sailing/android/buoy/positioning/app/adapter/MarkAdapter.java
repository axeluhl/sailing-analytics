package com.sap.sailing.android.buoy.positioning.app.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;

import java.text.DecimalFormat;
import java.util.List;


public class MarkAdapter extends ResourceCursorAdapter {

    public MarkAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView markName = (TextView) view.findViewById(R.id.mark_name);
        TextView markSet = (TextView) view.findViewById(R.id.mark_set);
        String name = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Mark.MARK_NAME));
        String markID = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Mark.MARK_ID));
        List<MarkPingInfo> markPings = DatabaseHelper.getInstance().getMarkPings(context, markID);
        String setText = "";
        if(markPings.isEmpty()){
        	setText = context.getString(R.string.not_set);
        }
        else{
            DecimalFormat df = new DecimalFormat("#.##");
        	setText = context.getString(R.string.set);
            double accuracy = markPings.get(0).getAccuracy();
        	String accuracyString = context.getString(R.string.mark_list_accuracy);
            accuracyString = String.format(accuracyString,df.format(accuracy));
        	setText += " " +accuracyString;
        }
        markName.setText(name);
        markSet.setText(setText);
    }
}
