package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;

public class FinishedSubmitFragment extends BasePanelFragment {

    public static FinishedSubmitFragment newInstance(Bundle args) {
        FinishedSubmitFragment fragment = new FinishedSubmitFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished_right, container, false);

        Button protest = ViewHolder.get(layout, R.id.protest_button);
        if (protest != null) {
            protest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AppConstants.INTENT_ACTION_SHOW_PROTEST);
                    String extra = getRace().getRaceGroup().getDisplayName();
                    if (TextUtils.isEmpty(extra)) {
                        extra = getRace().getRaceGroup().getName();
                    }
                    if (!getRace().getSeries().getName().equals(AppConstants.DEFAULT)) {
                        extra += " - " + getRace().getSeries().getName();
                    }
                    if (!getRace().getFleet().getName().equals(AppConstants.DEFAULT)) {
                        extra += " - " + getRace().getFleet().getName();
                    }
                    intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, extra);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                }
            });
        }

        return layout;
    }
}
