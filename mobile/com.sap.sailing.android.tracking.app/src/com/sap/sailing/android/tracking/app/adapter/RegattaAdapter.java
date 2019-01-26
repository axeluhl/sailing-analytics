package com.sap.sailing.android.tracking.app.adapter;

import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.android.shared.ui.adapters.AbstractRegattaAdapter;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract;

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
        TextView regattaName = ViewHelper.get(view, R.id.regattaName);
        if (regattaName != null) {
            String regattaNameText = cursor
                    .getString(cursor.getColumnIndex(AnalyticsContract.Leaderboard.LEADERBOARD_DISPLAY_NAME));
            regattaName.setText(regattaNameText);
        }
        TextView regattaEvent = ViewHelper.get(view, R.id.regatta_event);
        if (regattaEvent != null) {
            String regattaEventText = "";
            regattaEventText += cursor.getString(cursor.getColumnIndex(AnalyticsContract.Event.EVENT_NAME));
            regattaEvent.setText(regattaEventText);
        }
        TextView detail = ViewHelper.get(view, R.id.regatta_detail);
        if (detail != null) {
            String detailText = "";
            int type = cursor.getInt(cursor.getColumnIndex(AnalyticsContract.Checkin.CHECKIN_TYPE));
            if (type == CheckinUrlInfo.TYPE_COMPETITOR) {
                detailText += cursor
                        .getString(cursor.getColumnIndex(AnalyticsContract.Competitor.COMPETITOR_DISPLAY_NAME));
            } else if (type == CheckinUrlInfo.TYPE_MARK) {
                detailText += cursor.getString(cursor.getColumnIndex(AnalyticsContract.Mark.MARK_NAME));
            } else if (type == CheckinUrlInfo.TYPE_BOAT) {
                detailText += cursor.getString(cursor.getColumnIndex(AnalyticsContract.Boat.BOAT_NAME));
                ViewHelper.setColors(detail,
                        cursor.getString(cursor.getColumnIndex(AnalyticsContract.Boat.BOAT_COLOR)));
            }
            detail.setText(detailText);
        }
    }
}
