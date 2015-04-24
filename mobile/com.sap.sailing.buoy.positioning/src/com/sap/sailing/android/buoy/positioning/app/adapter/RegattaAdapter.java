package com.sap.sailing.android.buoy.positioning.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract;
import com.sap.sailing.android.shared.ui.adapters.AbstractRegattaAdapter;
import com.sap.sailing.android.shared.ui.customviews.OpenSansTextView;

public class RegattaAdapter extends AbstractRegattaAdapter {

    public RegattaAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        OpenSansTextView name = (OpenSansTextView) view.findViewById(R.id.regattaName);
        if (name != null) {
            String text = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Leaderboard.LEADERBOARD_NAME));
            name.setText(text);
            // String digestString =
            // cursor.getString(cursor.getColumnIndex(AnalyticsContract.Leaderboard.LEADERBOARD_CHECKIN_DIGEST));
            // System.out.println("BENNET: " + digestString);
        }
    }
}
