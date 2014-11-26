package com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.racelog.analyzing.impl.LastWindFixFinder;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.domain.coursedesign.BoatClassType;
import com.sap.sailing.racecommittee.app.domain.coursedesign.CourseDesign;
import com.sap.sailing.racecommittee.app.domain.coursedesign.CourseDesignComputer;
import com.sap.sailing.racecommittee.app.domain.coursedesign.CourseLayouts;
import com.sap.sailing.racecommittee.app.domain.coursedesign.NumberOfRounds;
import com.sap.sailing.racecommittee.app.domain.coursedesign.PositionedMark;
import com.sap.sailing.racecommittee.app.domain.coursedesign.TargetTime;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.WindFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sse.common.Util;

public class ByMapCourseDesignDialog extends RaceDialogFragment {

    private static int WIND_ACTIVITY_REQUEST_CODE = 7331;

    private MapView mMapView;
    private GoogleMap courseAreaMap;
    private Bundle mBundle;
    @SuppressWarnings("unused")
    private final static String TAG = ByMapCourseDesignDialog.class.getName();

    private Button publishButton;
    private Button unpublishButton;
    private ImageButton windButton;
    private Spinner spinnerBoatClass;
    private Spinner spinnerCourseLayout;
    private Spinner spinnerNumberOfRounds;
    private Spinner spinnerTargetTime;

    private CourseDesignComputer courseDesignComputer;
    private TargetTime selectedTargetTime;

    private ArrayAdapter<CourseLayouts> courseLayoutAdapter;
    private ArrayAdapter<TargetTime> targetTimeAdapter;

    private WindFragment windFragment;
    
    public ByMapCourseDesignDialog() {
        super();
        // handle bug "Dark overlay of MapFragment in Activity with Dialog theme" -
        // https://code.google.com/p/gmaps-api-issues/issues/detail?id=4865
        this.setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_DimDisabledDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.race_choose_classic_course_design_view, container);
        MapsInitializer.initialize(getActivity());

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(mBundle);
        return view;

    }

    protected void sendCourseDataAndDismiss(CourseBase courseDesign) {
        getRaceState().setCourseDesign(MillisecondsTimePoint.now(), courseDesign);
        saveChangedCourseDesignInCache(courseDesign);
        dismiss();
    }

    private void saveChangedCourseDesignInCache(CourseBase courseDesign) {
        if (!Util.isEmpty(courseDesign.getWaypoints())) {
            InMemoryDataStore.INSTANCE.setLastPublishedCourseDesign(courseDesign);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setTitle(getActivity().getText(R.string.course_design_dialog_title));
        setUpMapIfNeeded(getView());
        publishButton = (Button) getView().findViewById(R.id.publishCourseDesignButton);
        publishButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                CourseBase courseBase = new CourseDataImpl(getSelectedCourseLayout().getShortName() + " "
                        + getSelectedNumberOfRounds().getNumberOfRounds());
                sendCourseDataAndDismiss(courseBase);
            }

        });

        unpublishButton = (Button) getView().findViewById(R.id.unpublishCourseDesignButton);
        unpublishButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                CourseBase emptyCourse = new CourseDataImpl("Unknown");
                sendCourseDataAndDismiss(emptyCourse);
            }

        });

        windButton = (ImageButton) getView().findViewById(R.id.windButton);
        /*windButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	((RacingActivity)getActivity()).loadWindFragment();
            	//TODO: catch result WIND_ACTIVITY_REQUEST_CODE !
            }

        });
		*/
        courseDesignComputer = new CourseDesignComputer();

        spinnerBoatClass = (Spinner) getView().findViewById(R.id.classic_course_designer_boat_class);
        setupBoatClassSpinner();
        spinnerCourseLayout = (Spinner) getView().findViewById(R.id.classic_course_designer_course_layout);
        setupCourseLayoutSpinner();
        spinnerNumberOfRounds = (Spinner) getView().findViewById(R.id.classic_course_designer_number_of_rounds);
        setupNumberOfRoundsSpinner();
        spinnerTargetTime = (Spinner) getView().findViewById(R.id.classic_course_designer_target_time);
        setupTargetTimeSpinner();

        LastWindFixFinder lastWindFixFinder = new LastWindFixFinder(getRace().getRaceLog());
        Wind lastWind = lastWindFixFinder.analyze();

        if (lastWind != null) {
            courseDesignComputer.setWindSpeed(lastWind.getKnots());
            courseDesignComputer.setWindDirection(lastWind.getBearing());
            courseDesignComputer.setStartBoatPosition(lastWind.getPosition());
            recomputeCourseDesign();
        } else {
            Toast.makeText(getActivity(), "Set the wind information, please!", Toast.LENGTH_LONG).show();
        }
    }


    private void onWindEntered(Wind windFix) {
        getRaceState().setWindFix(MillisecondsTimePoint.now(), windFix);
        courseDesignComputer.setStartBoatPosition(windFix.getPosition());
        courseDesignComputer.setWindDirection(windFix.getBearing());
        courseDesignComputer.setWindSpeed(windFix.getKnots());
        recomputeCourseDesign();
    }

    private void recomputeCourseDesign() {
        try {
            drawMap(courseDesignComputer.compute());
        } catch (IllegalStateException ise) {
            Toast.makeText(getActivity(), ise.getMessage(), Toast.LENGTH_LONG).show();
            clearMap();
        } catch (IllegalArgumentException iae) {
            Toast.makeText(getActivity(), iae.getMessage(), Toast.LENGTH_LONG).show();
            clearMap();
        }
    }

    private void clearMap() {
        courseAreaMap.clear();
    }

    private void setupBoatClassSpinner() {
        ArrayAdapter<BoatClassType> adapter = new ArrayAdapter<BoatClassType>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, BoatClassType.values());
        spinnerBoatClass.setAdapter(adapter);
        spinnerBoatClass.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                setSelectedBoatClass((BoatClassType) adapterView.getItemAtPosition(position));

                // update possible course layouts
                courseLayoutAdapter.clear();
                courseLayoutAdapter.addAll(getSelectedBoatClass().getPossibleCourseLayoutsWithTargetTime().keySet());
                courseLayoutAdapter.notifyDataSetChanged();
                if (!getSelectedBoatClass().getPossibleCourseLayoutsWithTargetTime().keySet()
                        .contains(getSelectedCourseLayout())) {
                    setSelectedCourseLayout((CourseLayouts) getSelectedBoatClass()
                            .getPossibleCourseLayoutsWithTargetTime().keySet().toArray().clone()[0]);
                    spinnerCourseLayout.setSelection(courseLayoutAdapter.getPosition(getSelectedCourseLayout()));
                }

                recomputeCourseDesign();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerBoatClass.setSelection(adapter.getPosition(getSelectedBoatClass()));
    }

    private void setupCourseLayoutSpinner() {
        courseLayoutAdapter = new ArrayAdapter<CourseLayouts>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item);
        courseLayoutAdapter.addAll(getSelectedBoatClass().getPossibleCourseLayoutsWithTargetTime().keySet());
        spinnerCourseLayout.setAdapter(courseLayoutAdapter);
        spinnerCourseLayout.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                setSelectedCourseLayout((CourseLayouts) adapterView.getItemAtPosition(position));
                // update target time
                if (getSelectedBoatClass().getPossibleCourseLayoutsWithTargetTime().keySet()
                        .contains(getSelectedCourseLayout())) {
                setSelectedTargetTime((TargetTime) getSelectedBoatClass().getPossibleCourseLayoutsWithTargetTime().get(
                        getSelectedCourseLayout()));
                }
                spinnerTargetTime.setSelection(targetTimeAdapter.getPosition(getSelectedTargetTime()));
                recomputeCourseDesign();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerCourseLayout.setSelection(courseLayoutAdapter.getPosition(getSelectedCourseLayout()));
    }

    private void setupNumberOfRoundsSpinner() {
        ArrayAdapter<NumberOfRounds> adapter = new ArrayAdapter<NumberOfRounds>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, NumberOfRounds.values());
        spinnerNumberOfRounds.setAdapter(adapter);
        spinnerNumberOfRounds.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                setSelectedNumberOfRounds((NumberOfRounds) adapterView.getItemAtPosition(position));
                recomputeCourseDesign();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerNumberOfRounds.setSelection(adapter.getPosition(getSelectedNumberOfRounds()));
    }

    private void setupTargetTimeSpinner() {
        targetTimeAdapter = new ArrayAdapter<TargetTime>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                TargetTime.values());
        spinnerTargetTime.setAdapter(targetTimeAdapter);
        spinnerTargetTime.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                setSelectedTargetTime((TargetTime) adapterView.getItemAtPosition(position));
                courseDesignComputer.setTargetTime(getSelectedTargetTime());
                recomputeCourseDesign();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerTargetTime.setSelection(targetTimeAdapter.getPosition(getSelectedTargetTime()));
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
        if (courseAreaMap == null) {
            courseAreaMap = ((MapView) inflatedView.findViewById(R.id.mapView)).getMap();
            // something really bad happened
            if (courseAreaMap == null) {
                CourseBase emptyCourse = new CourseDataImpl("Unknown");
                sendCourseDataAndDismiss(emptyCourse);
            } else {
                courseAreaMap.getUiSettings().setRotateGesturesEnabled(false);
            }
        }
    }

    private void drawMap(CourseDesign courseDesign) {
        courseAreaMap.clear();
        courseAreaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                position2LatLng(courseDesign.getStartBoatPosition()), 14.0f));

        Bitmap bmpOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.boat);
        Bitmap bmResult = Bitmap
                .createBitmap(bmpOriginal.getHeight(), bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bmResult);
        tempCanvas.rotate((float) courseDesign.getWindDirection().getDegrees(), bmpOriginal.getWidth() / 2,
                bmpOriginal.getHeight() / 2);
        tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);

        courseAreaMap.addMarker(
                new MarkerOptions().position(position2LatLng(courseDesign.getStartBoatPosition()))
                        .icon(BitmapDescriptorFactory.fromBitmap(bmResult)).draggable(false)
                        .title(courseDesign.getCourseDesignDescription())).showInfoWindow();
        LatLng pinEndPosition = new LatLng(courseDesign.getPinEnd().getPosition().getLatDeg(), courseDesign.getPinEnd()
                .getPosition().getLngDeg());
        courseAreaMap.addMarker(new MarkerOptions().position(pinEndPosition)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.buoy_red_big)).draggable(false)
                .title(courseDesign.getPinEnd().getName()));

        for (PositionedMark mark : courseDesign.getCourseDesignSpecificMarks()) {
            LatLng markPosition = new LatLng(mark.getPosition().getLatDeg(), mark.getPosition().getLngDeg());
            courseAreaMap.addMarker(new MarkerOptions().position(markPosition).icon(getImageForMark(mark))
                    .draggable(false).title(makeMarkDescription(mark, courseDesign.getReferencePoint())));
        }

    }

    private String makeMarkDescription(PositionedMark mark, Position referencePoint) {
        DecimalFormat distanceFormat = new DecimalFormat("0.00");
        DecimalFormat bearingFormat = new DecimalFormat("0.00");
        StringBuilder description = new StringBuilder();
        description.append("mark: ");
        description.append(mark.getName());
        description.append(", bearing: ");
        description.append(bearingFormat.format(mark.getBearingFrom(referencePoint).getDegrees()));
        description.append("o, distance: ");
        description.append(distanceFormat.format(mark.getDistanceFromPosition(referencePoint).getNauticalMiles()));
        description.append("nm");
        return description.toString();
    }

    private BitmapDescriptor getImageForMark(PositionedMark mark) {
        if (MarkType.BUOY.equals(mark.getType())) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_black_big);
        } else if (MarkType.FINISHBOAT.equals(mark.getType())) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_black_finish_big);
        } else
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_black_big);
    }

    private LatLng position2LatLng(Position p) {
        return new LatLng(p.getLatDeg(), p.getLngDeg());
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

    public BoatClassType getSelectedBoatClass() {
        AppPreferences preferences = AppPreferences.on(getActivity());
        if (preferences.getBoatClass() != null) {
            return preferences.getBoatClass();
        } else {
            setSelectedBoatClass(BoatClassType.boatClass470erMen);
            return preferences.getBoatClass();
        }
    }

    public CourseLayouts getSelectedCourseLayout() {
        AppPreferences preferences = AppPreferences.on(getActivity());
        if (preferences.getCourseLayout() != null) {
            return preferences.getCourseLayout();
        } else {
            setSelectedCourseLayout((CourseLayouts) getSelectedBoatClass().getPossibleCourseLayoutsWithTargetTime()
                    .keySet().toArray().clone()[0]);
            return preferences.getCourseLayout();
        }
    }

    public NumberOfRounds getSelectedNumberOfRounds() {
        AppPreferences preferences = AppPreferences.on(getActivity());
        if (preferences.getNumberOfRounds() != null) {
            return preferences.getNumberOfRounds();
        } else {
            setSelectedNumberOfRounds(NumberOfRounds.TWO);
            return preferences.getNumberOfRounds();
        }
    }

    public TargetTime getSelectedTargetTime() {
        return selectedTargetTime;
        /*if (AppPreferences.getTargetTime(getActivity()) != null) {
            return AppPreferences.getTargetTime(getActivity());
        } else {
            setSelectedTargetTime(getSelectedBoatClass().getPossibleCourseLayoutsWithTargetTime().get(
                    getSelectedCourseLayout()));
            return AppPreferences.getTargetTime(getActivity());
        }*/
    }

    public void setSelectedBoatClass(BoatClassType selectedBoatClass) {
        AppPreferences.on(getActivity()).setBoatClass(selectedBoatClass);
        courseDesignComputer.setBoatClass(getSelectedBoatClass());
    }

    public void setSelectedCourseLayout(CourseLayouts selectedCourseLayout) {
        AppPreferences.on(getActivity()).setCourseLayout(selectedCourseLayout);
        courseDesignComputer.setCourseLayout(getSelectedCourseLayout());
    }

    public void setSelectedNumberOfRounds(NumberOfRounds selectedNumberOfRounds) {
        AppPreferences.on(getActivity()).setNumberOfRounds(selectedNumberOfRounds);
        courseDesignComputer.setNumberOfRounds(getSelectedNumberOfRounds());
    }

    public void setSelectedTargetTime(TargetTime selectedTargetTime) {
        this.selectedTargetTime  = selectedTargetTime;
        /*AppPreferences.setTargetTime(getActivity(), selectedTargetTime);
        courseDesignComputer.setTargetTime(getSelectedTargetTime());*/
    }

}
