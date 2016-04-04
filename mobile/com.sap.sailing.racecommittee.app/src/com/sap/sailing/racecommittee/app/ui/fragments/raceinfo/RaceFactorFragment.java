package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;

public class RaceFactorFragment extends BaseFragment {

    public static RaceFactorFragment newInstance(@START_MODE_VALUES int startMode) {
        RaceFactorFragment fragment = new RaceFactorFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View layout = LayoutInflater.from(getActivity()).inflate(R.layout.race_schedule_procedure_pathfinder, container, false);

        HeaderLayout header = ViewHelper.get(layout, R.id.header);
        if (header != null) {
            header.setHeaderOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goHome();
                }
            });
        }

        return layout;
    }
}
