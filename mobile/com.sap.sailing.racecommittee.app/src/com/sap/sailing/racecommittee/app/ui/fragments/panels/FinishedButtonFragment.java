package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;

public class FinishedButtonFragment extends BasePanelFragment {

    public static FinishedButtonFragment newInstance(Bundle args) {
        FinishedButtonFragment fragment = new FinishedButtonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished_left, container, false);

        RelativeLayout record = ViewHolder.get(layout, R.id.record_button);
        if (record != null) {
            // TODO
            record.setOnClickListener(new NotYetImplemented());
        }

        RelativeLayout photo = ViewHolder.get(layout, R.id.photo_button);
        if (photo != null) {
            // TODO
            photo.setOnClickListener(new NotYetImplemented());
        }

        RelativeLayout list = ViewHolder.get(layout, R.id.list_button);
        if (list != null) {
            // TODO
            list.setOnClickListener(new NotYetImplemented());
        }

        return layout;
    }
}
