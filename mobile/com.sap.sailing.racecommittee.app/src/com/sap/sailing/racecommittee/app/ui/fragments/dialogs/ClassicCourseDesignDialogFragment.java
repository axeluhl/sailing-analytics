package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sap.sailing.racecommittee.app.R;

public class ClassicCourseDesignDialogFragment extends RaceDialogFragment {
    private MapView mMapView;
    private GoogleMap mMap;
    private Bundle mBundle;

    public ClassicCourseDesignDialogFragment() {
        super();
        // handle map bug - https://code.google.com/p/gmaps-api-issues/issues/detail?id=4865
        this.setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_DimDisabledDialog);
    }

    @SuppressWarnings("unused")
    private final static String TAG = ClassicCourseDesignDialogFragment.class.getName();

    protected CourseDesignListener hostActivity;

    private Button publishButton;
    private Button unpublishButton;
    @SuppressWarnings("unused")
    private GoogleMap courseAreaMap;

    @Override
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);

        if (activity instanceof CourseDesignListener) {
            this.hostActivity = (CourseDesignListener) activity;
        } else {
            throw new IllegalStateException(String.format(
                    "Instance of %s must be attached to instances of %s. Tried to attach to %s.",
                    ActivityDialogFragment.class.getName(), CourseDesignListener.class.getName(), activity.getClass()
                            .getName()));
        }
    };

    protected CourseDesignListener getHost() {
        return this.hostActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.race_choose_classic_course_design_view, container);
        try {
            MapsInitializer.initialize(getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO handle this situation
        }

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(mBundle);
        setUpMapIfNeeded(view);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setTitle(getActivity().getText(R.string.course_design_dialog_title));

        publishButton = (Button) getView().findViewById(R.id.publishCourseDesignButton);
        publishButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
            }

        });

        unpublishButton = (Button) getView().findViewById(R.id.unpublishCourseDesignButton);
        unpublishButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void notifyTick() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
    }

    private void setUpMapIfNeeded(View inflatedView) {
        if (mMap == null) {
            mMap = ((MapView) inflatedView.findViewById(R.id.mapView)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

}
