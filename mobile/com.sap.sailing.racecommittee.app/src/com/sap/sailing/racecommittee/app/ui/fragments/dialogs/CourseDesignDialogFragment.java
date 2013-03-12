package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.domain.RoundingDirection;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseElementListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseListDataElement;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.MarkGridAdapter;

public class CourseDesignDialogFragment extends RaceDialogFragment {
    private final static String TAG = CourseDesignDialogFragment.class.getName();

    protected DialogFragmentButtonListener hostActivity;
    private List<Mark> aMarkList;
    private MarkGridAdapter gridAdapter;
    private List<CourseListDataElement> courseElements;
    private List<CourseListDataElement> previousCourseElements;
    private CourseElementListAdapter courseElementAdapter;
    private CourseElementListAdapter previousCourseElementAdapter;
    private ListView newCourseListView;
    private ListView previousCourseListView;
    
    private Button publishButton;
    private Button unpublishButton;
    private Button takePreviousButton;

    @Override
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);

        if (activity instanceof DialogFragmentButtonListener) {
            this.hostActivity = (DialogFragmentButtonListener) activity;
        } else {
            throw new IllegalStateException(
                    String.format(
                            "Instance of %s must be attached to instances of %s. Tried to attach to %s.",
                            ActivityDialogFragment.class.getName(),
                            DialogFragmentButtonListener.class.getName(),
                            activity.getClass().getName()));
        }
    };

    protected DialogFragmentButtonListener getHost() {
        return this.hostActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.race_choose_course_design_view, container);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setTitle(getActivity().getText(R.string.course_design_dialog_title));

        aMarkList  = new ArrayList<Mark>();
        courseElements = new ArrayList<CourseListDataElement>();
        previousCourseElements = new ArrayList<CourseListDataElement>();

        gridAdapter = new MarkGridAdapter(getActivity(), R.layout.welter_one_row_no_image, aMarkList);
        courseElementAdapter = new CourseElementListAdapter(getActivity(), R.layout.welter_one_row_three_columns, courseElements);
        previousCourseElementAdapter = new CourseElementListAdapter(getActivity(), R.layout.welter_one_row_three_columns, previousCourseElements);

        GridView gridView = (GridView) getView().findViewById(R.id.gridViewAssets);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                Mark mark = (Mark) gridAdapter.getItem(position);
                Log.i("CourseDesign", "Grid is clicked at position " + position + " for buoy " + mark.getName());
                onMarkClickedOnGrid(mark);
            }

        });

        newCourseListView = (ListView) getView().findViewById(R.id.listViewNewCourse);
        newCourseListView.setAdapter(courseElementAdapter);
        newCourseListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                createCourseElementModificationDialog(courseElements.get(position));
                return true;
            }
        });

        if (getRace().getCourseDesign() != null) {
            fillCourseElementsInList();
        }

        previousCourseListView = (ListView) getView().findViewById(R.id.listViewPreviousCourse);
        previousCourseListView.setAdapter(previousCourseElementAdapter);

        if(InMemoryDataStore.INSTANCE.getLastPublishedCourseDesign() != null){
            fillPreviousCourseElementsInList();
        }

        takePreviousButton = (Button) getView().findViewById(R.id.takePreviousCourseDesignButton);
        takePreviousButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                if (!previousCourseElements.isEmpty()) {
                    if (!courseElements.isEmpty())
                        createUsePreviousCourseDialog();
                    else
                        copyPreviousToNewCourseDesign();
                }
                else {
                    Toast.makeText(getActivity(), "No course available to copy", Toast.LENGTH_LONG).show();
                }
            }
        });


        publishButton = (Button) getView().findViewById(R.id.publishCourseDesignButton);
        publishButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                try {
                    CourseData courseData = convertCourseElementsToACourseData();
                    sendCourseDataAndDismiss(courseData);
                } catch (IllegalStateException ex) {
                    Toast.makeText(getActivity(), "A right buoy is missing for a gate. Please select the right buoy.", Toast.LENGTH_LONG).show();
                } catch (IllegalArgumentException ex) {
                    Toast.makeText(getActivity(), "The course design has to have at least one waypoint.", Toast.LENGTH_LONG).show();
                }
            }

        });

        unpublishButton = (Button) getView().findViewById(R.id.unpublishCourseDesignButton);
        unpublishButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                CourseData emptyCourse = null;
                sendCourseDataAndDismiss(emptyCourse);
            }

        });

    }

    private void fillPreviousCourseElementsInList() {
        CourseData previousCourseData = InMemoryDataStore.INSTANCE.getLastPublishedCourseDesign();
        if (previousCourseData != null) {
            previousCourseElements.clear();
            previousCourseElements.addAll(convertCourseDesignToCourseElements(previousCourseData));
            previousCourseElementAdapter.notifyDataSetChanged();
        }
    }

    private void fillCourseElementsInList() {
        courseElements.clear();
        courseElements.addAll(convertCourseDesignToCourseElements(getRace().getCourseDesign()));
        courseElementAdapter.notifyDataSetChanged();
    }

    protected List<CourseListDataElement> convertCourseDesignToCourseElements(CourseData courseData) {
        List<CourseListDataElement> elementList = new ArrayList<CourseListDataElement>();

        for (Waypoint waypoint : courseData.getWaypoints()) {
            ControlPoint controlPoint = waypoint.getControlPoint();
            
            if (controlPoint instanceof Mark) {
                CourseListDataElement element = new CourseListDataElement();
                element.setLeftMark((Mark)controlPoint);
                element.setRoundingDirection(getRoundingDirection(waypoint.getPassingSide()));
                elementList.add(element);
            } else if (controlPoint instanceof Gate) {
                Gate gate = (Gate) controlPoint;
                CourseListDataElement element = new CourseListDataElement();
                element.setLeftMark(gate.getLeft());
                element.setRightMark(gate.getRight());
                elementList.add(element);
            }
        }

        return elementList;
    }

    protected void sendCourseDataAndDismiss(CourseData courseDesign) {
        sendCourseData(courseDesign);
        dismiss();
    }

    protected void sendCourseData(CourseData courseDesign) {
//        this.race.changeCourseDesign(courseDesign);
        saveChangedCourseDesignInCache(courseDesign);
    }

    private void saveChangedCourseDesignInCache(CourseData courseDesign) {
        if (!Util.isEmpty(courseDesign.getWaypoints())) {
            InMemoryDataStore.INSTANCE.setLastPublishedCourseDesign(courseDesign);
        }
    }

    protected CourseData convertCourseElementsToACourseData() throws IllegalStateException, IllegalArgumentException {
        CourseData design = new CourseDataImpl("CourseTemplate");
        List<Waypoint> waypoints = new ArrayList<Waypoint>();

        for (CourseListDataElement courseElement : courseElements) {
            if (courseElement.getRoundingDirection().equals(RoundingDirection.Gate) && courseElement.getRightMark() != null) {
                String gateName = "Gate " + courseElement.getLeftMark().getName() + " / " + courseElement.getRightMark().getName();
                Gate gate = new GateImpl(courseElement.getLeftMark(), courseElement.getRightMark(), gateName);
                Waypoint waypoint = new WaypointImpl(gate);
                
                waypoints.add(waypoint);
            } else if (courseElement.getRoundingDirection().equals(RoundingDirection.Gate) && courseElement.getRightMark() == null) {
                throw new IllegalStateException("Gate has no right buoy");
            } else {
                Waypoint waypoint = new WaypointImpl(courseElement.getLeftMark(), getNauticalSide(courseElement.getRoundingDirection()));
                
                waypoints.add(waypoint);
            }

        }
        if (waypoints.isEmpty()) {
            throw new IllegalArgumentException("The course design to be published has no waypoints.");
        }

        int i = 0;
        for (Waypoint waypoint : waypoints) {
            design.addWaypoint(i++, waypoint);
        }

        return design;
    }

    private NauticalSide getNauticalSide(RoundingDirection roundingDirection) {
        NauticalSide side = null;
        if (roundingDirection.name().equalsIgnoreCase(NauticalSide.PORT.name())) {
            side = NauticalSide.PORT;
        } else if (roundingDirection.name().equalsIgnoreCase(NauticalSide.STARBOARD.name())){
            side = NauticalSide.STARBOARD;
        }
        return side;
    }
    
    private RoundingDirection getRoundingDirection(NauticalSide passingSide) {
        RoundingDirection direction = null;
        if (passingSide.name().equalsIgnoreCase(RoundingDirection.Port.name())) {
            direction = RoundingDirection.Port;
        } else if (passingSide.name().equalsIgnoreCase(RoundingDirection.Starboard.name())){
            direction = RoundingDirection.Starboard;
        } else {
            direction = RoundingDirection.Gate;
        }
        return direction;
    }

    private void createUsePreviousCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Use previous course design");
        builder.setMessage("Use previously published course design?");
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                copyPreviousToNewCourseDesign();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        builder.create();
        builder.show();
    }
    
    protected void copyPreviousToNewCourseDesign() {
        courseElements.clear();
        courseElements.addAll(previousCourseElements);
        courseElementAdapter.notifyDataSetChanged();
}

    protected void onMarkClickedOnGrid(Mark mark) {
        if (courseElements.isEmpty()) {
            addNewCourseElementToList(mark);
        } else {
            CourseListDataElement lastCourseElement = courseElements.get(courseElements.size() - 1);
            if (lastCourseElement.getRoundingDirection().equals(RoundingDirection.Gate) && lastCourseElement.getRightMark() == null) {
                lastCourseElement.setRightMark(mark);
                courseElementAdapter.notifyDataSetChanged();
            } else {
                addNewCourseElementToList(mark);
            }
        }
    }
    
    private void addNewCourseElementToList(Mark mark) {
        CourseListDataElement courseElement = new CourseListDataElement();
        courseElement.setLeftMark(mark);
        createRoundingDirectionDialog(courseElement);
    }

    private void createRoundingDirectionDialog(final CourseListDataElement courseElement) {
        List<String> directions = new ArrayList<String>();
        for (RoundingDirection direction : RoundingDirection.relevantValues()) {
            directions.add(direction.name());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_a_rounding_direction)
        .setItems(R.array.rounding_directions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                String[] directions = getActivity().getResources().getStringArray(R.array.rounding_directions);
                RoundingDirection pickedDirection = RoundingDirection.valueOf(directions[position]);
                onRoundingDirectionPicked(courseElement, pickedDirection);
            }
        });
        builder.create();
        builder.show();
    }

    protected void onRoundingDirectionPicked(CourseListDataElement courseElement, RoundingDirection pickedDirection) {
        courseElement.setRoundingDirection(pickedDirection);
        courseElements.add(courseElement);
        courseElementAdapter.notifyDataSetChanged();
    }
    
    private void createCourseElementModificationDialog(final CourseListDataElement courseElement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.change_course_element)
        .setItems(R.array.course_element_changers, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (position == 0) {
                    moveUpCourseElement(courseElement);
                } else if (position == 1) {
                    moveDownCourseElement(courseElement);
                } else if (position == 2) {
                    deleteCourseElement(courseElement);
                }
            }
        });
        builder.create();
        builder.show();
    }

    protected void deleteCourseElement(CourseListDataElement courseElement) {
        courseElements.remove(courseElement);
        courseElementAdapter.notifyDataSetChanged();
    }

    protected void moveUpCourseElement(CourseListDataElement courseElement) {
        int index = courseElements.indexOf(courseElement);
        if (index != 0) {
            Collections.swap(courseElements, index, index - 1);
            courseElementAdapter.notifyDataSetChanged();
        }
    }

    protected void moveDownCourseElement(CourseListDataElement courseElement) {
        int index = courseElements.indexOf(courseElement);
        if (index != courseElements.size() - 1) {
            Collections.swap(courseElements, index, index + 1);
            courseElementAdapter.notifyDataSetChanged();
        }
    }

    protected void onNegativeButton() {
        if (getHost() != null) {
            getHost().onDialogNegativeButton();
        } else {
            ExLog.w(TAG, "Dialog host was null.");
        }
    }

    protected void onPositiveButton() {
        if (getHost() != null) {
            getHost().onDialogPositiveButton();
        } else {
            ExLog.w(TAG, "Dialog host was null.");
        }
    }

}
