package com.sap.sailing.android.buoy.positioning.app.adapter;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract;
import com.sap.sailing.android.shared.ui.adapters.AbstractRegattaAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

public class RegattaAdapter extends AbstractRegattaAdapter {

    public RegattaAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.regattaName);
        if (name != null) {
            String text = cursor
                    .getString(cursor.getColumnIndex(AnalyticsContract.Leaderboard.LEADERBOARD_DISPLAY_NAME));
            name.setText(text);
        }
    }
}
