package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.ui.activities.WindActivity;

public class ClassicCourseDesignDialogFragment extends RaceDialogFragment {
    private MapView mMapView;
    private GoogleMap courseAreaMap;
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
    private ImageButton windButton;

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

        windButton = (ImageButton) getView().findViewById(R.id.windButton);
        windButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), WindActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        setUpMap();
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
        if (courseAreaMap == null) {
            courseAreaMap = ((MapView) inflatedView.findViewById(R.id.mapView)).getMap();
            if (courseAreaMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        courseAreaMap.clear();
        DataStore ds = InMemoryDataStore.INSTANCE;
        if (ds.getLastLatitude() != null && ds.getLastLongitude() != null && ds.getLastWindDirection() != null && ds.getLastWindSpeed() != null) {
            courseAreaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(ds.getLastLatitude(), ds.getLastLongitude()), 13.0f));
        
            Bitmap bmpOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.boat);
            Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getHeight(), bmpOriginal.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bmResult);
            tempCanvas.rotate(ds.getLastWindDirection(), bmpOriginal.getWidth() / 2, bmpOriginal.getHeight() / 2);
            tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);

            courseAreaMap.addMarker(new MarkerOptions()
                    .position(new LatLng(ds.getLastLatitude(), ds.getLastLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmResult)).draggable(false)
                    .title(ds.getLastLatitude()+"/"+ds.getLastLongitude()+", "+ds.getLastWindSpeed()+"kn, "+ds.getLastWindDirection()+"°"));
        }

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
