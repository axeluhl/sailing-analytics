package com.sap.sailing.android.buoy.positioning.app.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract;


public class MarkAdapter extends ResourceCursorAdapter {

    public MarkAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView markName = (TextView) view.findViewById(R.id.mark_name);
        TextView markSet = (TextView) view.findViewById(R.id.mark_set);
        String name = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Mark.MARK_NAME));
        // String setText = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Mark.MARK_NAME));

        markName.setText(name);
    }
}
