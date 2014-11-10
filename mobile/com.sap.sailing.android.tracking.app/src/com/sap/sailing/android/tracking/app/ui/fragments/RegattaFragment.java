package com.sap.sailing.android.tracking.app.ui.fragments;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class RegattaFragment extends BaseFragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_regatta, container, false);

        Button startTracking = (Button) view.findViewById(R.id.startTracking);
        startTracking.setOnClickListener(this);
        Button stopTracking = (Button) view.findViewById(R.id.stopTracking);
        stopTracking.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getActivity(), TrackingService.class);
        switch (view.getId()) {
        case R.id.startTracking:
            intent.setAction(getString(R.string.tracking_service_start));
            break;
            
        case R.id.stopTracking:
            intent.setAction(getString(R.string.tracking_service_stop));
            break;

        default:
            break;
        }
        getActivity().startService(intent);
    }
}
