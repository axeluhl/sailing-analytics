package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.shared.util.EulaHelper;
import com.sap.sailing.android.ui.fragments.BaseFragment;

public class AboutFragment extends BaseFragment {

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        view.findViewById(R.id.licence_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        view.findViewById(R.id.eula_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EulaHelper.openEulaPage(getActivity());
            }
        });
        return view;
    }
}
