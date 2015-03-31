package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;

public class FinishedSubmitFragment extends BasePanelFragment {

    public static FinishedSubmitFragment newInstance(Bundle args) {
        FinishedSubmitFragment fragment = new FinishedSubmitFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished_right, container, false);

        Button protest = ViewHolder.get(layout, R.id.protest_button);
        if (protest != null) {
            // TODO
            protest.setOnClickListener(new NotYetImplemented());
        }

        Button submit = ViewHolder.get(layout, R.id.submit_button);
        if (submit != null) {
            // TODO
            submit.setOnClickListener(new NotYetImplemented());
        }

        return layout;
    }
}
