package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sap.sailing.domain.common.coursedesign.BoatClassType;
import com.sap.sailing.domain.common.coursedesign.CourseDesign;
import com.sap.sailing.domain.common.coursedesign.CourseLayout;
import com.sap.sailing.domain.common.coursedesign.NumberOfRounds;
import com.sap.sailing.domain.common.coursedesign.TargetTime;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.courseDesigner.CourseDesignComputer;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.ui.activities.WindActivity;

public class ClassicCourseDesignDialogFragment extends RaceDialogFragment {
    private MapView mMapView;
    private GoogleMap courseAreaMap;
    private Bundle mBundle;
    @SuppressWarnings("unused")
    private final static String TAG = ClassicCourseDesignDialogFragment.class.getName();

    private Button publishButton;
    private ImageButton windButton;
    private Spinner spinnerBoatClass;
    private Spinner spinnerCourseLayout;
    private Spinner spinnerNumberOfRounds;
    private Spinner spinnerTargetTime;

    private CourseDesignComputer courseDesignComputer;

    private ArrayAdapter<CourseLayout> courseLayoutAdapter;

    // TODO determine this by given race
    private BoatClassType selectedBoatClass = BoatClassType.boatClass470er;
    private CourseLayout selectedCourseLayout = (CourseLayout) BoatClassType.boatClass470er
            .getPossibleCourseLayoutsWithTargetTime().keySet().toArray().clone()[0];
    private NumberOfRounds selectedNumberOfRounds = NumberOfRounds.TWO;
    private TargetTime selectedTargetTime = TargetTime.thirty;

    public ClassicCourseDesignDialogFragment() {
        super();
        // handle map bug - https://code.google.com/p/gmaps-api-issues/issues/detail?id=4865
        this.setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_DimDisabledDialog);
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
                startActivityForResult(intent, 1);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

        });

        spinnerBoatClass = (Spinner) getView().findViewById(R.id.classic_course_designer_boat_class);
        setupBoatClassSpinner();
        spinnerCourseLayout = (Spinner) getView().findViewById(R.id.classic_course_designer_course_layout);
        setupCourseLayoutSpinner();
        spinnerNumberOfRounds = (Spinner) getView().findViewById(R.id.classic_course_designer_number_of_rounds);
        setupNumberOfRoundsSpinner();
        spinnerTargetTime = (Spinner) getView().findViewById(R.id.classic_course_designer_target_time);
        setupTargetTimeSpinner();

        DataStore ds = InMemoryDataStore.INSTANCE;
        courseDesignComputer= new CourseDesignComputer().setStartBoatPosition(ds.getLastWindPosition()).setWindDirection(ds.getLastWindDirection())
                .setWindSpeed(ds.getLastWindSpeed()).setBoatClass(selectedBoatClass)
                .setCourseLayout(selectedCourseLayout).setNumberOfRounds(selectedNumberOfRounds)
                .setTargetTime(selectedTargetTime);
        setUpMapIfNeeded(getView());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO make use of activity result instead of just handling the callback
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                recomputeCourseDesign();
            }
        }
    }// onActivityResult

    private CourseDesign recomputeCourseDesign() {
        try{
        courseDesignComputer.compute();
        }catch (IllegalStateException ise){
            Toast.makeText(
                    getActivity(),
                    ise.getMessage(), Toast.LENGTH_LONG).show();
        }
        DataStore ds = InMemoryDataStore.INSTANCE;
        Toast.makeText(
                getActivity(),
                "" + ds.getLastWindPosition() + ds.getLastWindDirection() + ds.getLastWindSpeed() + selectedBoatClass
                        + selectedCourseLayout + selectedNumberOfRounds + selectedTargetTime, Toast.LENGTH_LONG).show();
        return null;

    }

    private void setupBoatClassSpinner() {
        ArrayAdapter<BoatClassType> adapter = new ArrayAdapter<BoatClassType>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, BoatClassType.values());
        spinnerBoatClass.setAdapter(adapter);
        spinnerBoatClass.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedBoatClass = (BoatClassType) adapterView.getItemAtPosition(position);
                courseLayoutAdapter.clear();
                courseLayoutAdapter.addAll(selectedBoatClass.getPossibleCourseLayoutsWithTargetTime().keySet());
                courseLayoutAdapter.notifyDataSetChanged();
                if (selectedBoatClass.getPossibleCourseLayoutsWithTargetTime().keySet().contains(selectedCourseLayout)) {
                    recomputeCourseDesign();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerBoatClass.setSelection(adapter.getPosition(selectedBoatClass));
    }

    private void setupCourseLayoutSpinner() {
        courseLayoutAdapter = new ArrayAdapter<CourseLayout>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item);
        courseLayoutAdapter.addAll(selectedBoatClass.getPossibleCourseLayoutsWithTargetTime().keySet());
        spinnerCourseLayout.setAdapter(courseLayoutAdapter);
        spinnerCourseLayout.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedCourseLayout = (CourseLayout) adapterView.getItemAtPosition(position);
                recomputeCourseDesign();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerCourseLayout.setSelection(courseLayoutAdapter.getPosition(selectedCourseLayout));
    }

    private void setupNumberOfRoundsSpinner() {
        ArrayAdapter<NumberOfRounds> adapter = new ArrayAdapter<NumberOfRounds>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, NumberOfRounds.values());
        spinnerNumberOfRounds.setAdapter(adapter);
        spinnerNumberOfRounds.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedNumberOfRounds = (NumberOfRounds) adapterView.getItemAtPosition(position);
                recomputeCourseDesign();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerNumberOfRounds.setSelection(adapter.getPosition(selectedNumberOfRounds));
    }

    private void setupTargetTimeSpinner() {
        ArrayAdapter<TargetTime> adapter = new ArrayAdapter<TargetTime>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, TargetTime.values());
        spinnerTargetTime.setAdapter(adapter);
        spinnerTargetTime.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedTargetTime = (TargetTime) adapterView.getItemAtPosition(position);
                recomputeCourseDesign();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerNumberOfRounds.setSelection(adapter.getPosition(selectedTargetTime));
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
        if (ds.getLastWindPosition() != null && ds.getLastWindDirection() != null && ds.getLastWindSpeed() != null) {
            courseAreaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ds.getLastWindPosition(), 17.0f));

            Bitmap bmpOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.boat);
            Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getHeight(), bmpOriginal.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bmResult);
            tempCanvas.rotate(ds.getLastWindDirection(), bmpOriginal.getWidth() / 2, bmpOriginal.getHeight() / 2);
            tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);

            courseAreaMap.addMarker(new MarkerOptions()
                    .position(ds.getLastWindPosition())
                    .icon(BitmapDescriptorFactory.fromBitmap(bmResult))
                    .draggable(false)
                    .title(ds.getLastWindPosition() + ", " + ds.getLastWindSpeed() + "kn, " + ds.getLastWindDirection()
                            + "°"));
        }
        recomputeCourseDesign();

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
