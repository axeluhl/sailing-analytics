package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        return layout;
    }
}
