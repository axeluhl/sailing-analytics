package com.sap.sailing.android.tracking.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import com.sap.sailing.android.shared.ui.adapters.AbstractRegattaAdapter;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract;

public class RegattaAdapter extends AbstractRegattaAdapter {

    public RegattaAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.regattaName);
        if (name != null) {
            String text = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Event.EVENT_NAME));
            // text += " (" +
            // cursor.getString(cursor.getColumnIndex(AnalyticsContract.Competitor.COMPETITOR_DISPLAY_NAME)) + ")";
            name.setText(text);
        }
    }
}
