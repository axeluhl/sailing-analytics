package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment;

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
                    RaceListFragment.showProtest(getActivity(), getRace());
                }
            });
        }

        return layout;
    }
}
