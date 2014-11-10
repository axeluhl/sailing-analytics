package com.sap.sailing.android.tracking.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

import com.sap.sailing.android.shared.provider.AnalyticsContract;
import com.sap.sailing.android.tracking.app.R;

public class RegattaAdapter extends ResourceCursorAdapter {

    public RegattaAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
       TextView name = (TextView) view.findViewById(R.id.regattaName);
       if (name != null) {
           String text = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Event.EVENT_TITLE));
           text += " (" + cursor.getString(cursor.getColumnIndex(AnalyticsContract.Competitor.COMPETITOR_NAME)) + ")";
           name.setText(text);
       }
    }
}
