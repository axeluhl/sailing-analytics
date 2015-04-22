package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Loader;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.sap.sailing.domain.base.*;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.impl.CourseListDataElementWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseElementAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseElementAdapter.ElementLongClick;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseElementAdapter.EventListener;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseListDataElement;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseMarkAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseMarkAdapter.MarkLongClick;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sailing.racecommittee.app.ui.utils.ESSMarkImageHelper;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CourseFragmentMarks extends CourseFragment implements MarkLongClick, ElementLongClick, EventListener {

    private RecyclerViewDragDropManager mDragDropManager;
    private RecyclerViewSwipeManager mSwipeManager;
    private RecyclerViewTouchActionGuardManager mGuardManager;
    private ReadonlyDataManager mDataManager;
    private ArrayList<CourseListDataElementWithIdImpl> mHistory;
    private ArrayList<CourseListDataElementWithIdImpl> mElements;
    private ArrayList<Mark> mMarks;
    private RecyclerView mHistoryCourse;
    private RecyclerView mCurrentCourse;
    private RecyclerView mMarkGrid;
    private CourseElementAdapter mHistoryAdapter;
    private RecyclerView.Adapter mCourseAdapter;
    private CourseMarkAdapter mMarkAdapter;
    private int mId;

    public CourseFragmentMarks() {
        super();

        mId = 0;
        mHistory = new ArrayList<>();
        mElements = new ArrayList<>();
        mMarks = new ArrayList<>();
    }

    public static CourseFragmentMarks newInstance(int startMode) {
        CourseFragmentMarks fragment = new CourseFragmentMarks();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_course_marks, container, false);

        mHistoryCourse = (RecyclerView) view.findViewById(R.id.previous_course);
        if (mHistoryCourse != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mHistoryCourse.setLayoutManager(layoutManager);

            mHistoryAdapter = new CourseElementAdapter(getActivity(), mHistory, ESSMarkImageHelper.getInstance(),
                false);
            mHistoryCourse.setAdapter(mHistoryAdapter);
        }

        mCurrentCourse = (RecyclerView) view.findViewById(R.id.new_course);
        if (mCurrentCourse != null) {
            mGuardManager = new RecyclerViewTouchActionGuardManager();
            mGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
            mGuardManager.setEnabled(true);

            mDragDropManager = new RecyclerViewDragDropManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mDragDropManager.setDraggingItemShadowDrawable(
                    (NinePatchDrawable) getActivity().getDrawable(R.drawable.material_shadow_z3));
            } else {
                mDragDropManager.setDraggingItemShadowDrawable(
                    (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));
            }

            mSwipeManager = new RecyclerViewSwipeManager();

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

            CourseElementAdapter adapter = new CourseElementAdapter(getActivity(), mElements,
                ESSMarkImageHelper.getInstance(), true);
            adapter.setListener(this);
            adapter.setEventListener(this);

            mCourseAdapter = mDragDropManager.createWrappedAdapter(adapter);
            mCourseAdapter = mSwipeManager.createWrappedAdapter(mCourseAdapter);
            mCurrentCourse.setLayoutManager(layoutManager);
            mCurrentCourse.setAdapter(mCourseAdapter);
            mCurrentCourse.setItemAnimator(new SwipeDismissItemAnimator());

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mCurrentCourse.addItemDecoration(new ItemShadowDecorator(
                    (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
            }

            mGuardManager.attachRecyclerView(mCurrentCourse);
            mSwipeManager.attachRecyclerView(mCurrentCourse);
            mDragDropManager.attachRecyclerView(mCurrentCourse);
        }

        mMarkGrid = (RecyclerView) view.findViewById(R.id.assets);
        if (mMarkGrid != null) {
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
            mMarkGrid.setLayoutManager(layoutManager);

            mMarkAdapter = new CourseMarkAdapter(getActivity(), mMarks, ESSMarkImageHelper.getInstance());
            mMarkAdapter.setListener(this);
            mMarkGrid.setAdapter(mMarkAdapter);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDataManager = DataManager.create(getActivity());

        if (getRace().getCourseDesign() != null) {
            fillCourseElement();
        }

        loadMarks();
        fillPreviousCourseElementsWithLastPublishedCourseDesign();

        if (getView() != null) {
            Button takePreviousButton = (Button) getView().findViewById(R.id.takeHistoryCourse);
            takePreviousButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!mHistory.isEmpty()) {
                        if (!mElements.isEmpty()) {
                            createUsePreviousCourseDialog();
                        } else {
                            copyPreviousToNewCourseDesign();
                        }
                    } else {
                        String toastText = getString(R.string.error_no_course_to_copy);
                        Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
                    }
                }
            });

            Button publishButton = (Button) getView().findViewById(R.id.publishCourse);
            publishButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    try {
                        CourseBase courseData = convertCourseElementsToACourseData();
                        sendCourseDataAndDismiss(courseData);
                    } catch (IllegalStateException ex) {
                        if (ex.getMessage().equals("Missing second mark")) {
                            String toastText = getString(R.string.error_missing_second_mark);
                            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
                        } else if (ex.getMessage().equals("Each waypoints needs passing instructions")) {
                            String toastText = getString(R.string.error_missing_passing_instructions);
                            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
                        }

                    } catch (IllegalArgumentException ex) {
                        String toastText = getString(R.string.error_no_way_point);
                        Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
                    }
                }
            });

            Button unpublishButton = (Button) getView().findViewById(R.id.unpublishCourse);
            unpublishButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    CourseBase emptyCourse = new CourseDataImpl("Unpublished course design");
                    sendCourseDataAndDismiss(emptyCourse);
                }
            });
        }
    }

    @Override
    public void onPause() {
        mDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mDragDropManager != null) {
            mDragDropManager.release();
            mDragDropManager = null;
        }

        if (mSwipeManager != null) {
            mSwipeManager.release();
            mSwipeManager = null;
        }

        if (mGuardManager != null) {
            mGuardManager.release();
            mGuardManager = null;
        }

        if (mHistoryCourse != null) {
            mHistoryCourse.setAdapter(null);
            mHistoryCourse = null;
        }

        if (mCurrentCourse != null) {
            mCurrentCourse.setItemAnimator(null);
            mCurrentCourse.setAdapter(null);
            mCurrentCourse = null;
        }

        if (mMarkGrid != null) {
            mMarkGrid.setAdapter(null);
            mMarkGrid = null;
        }

        if (mCourseAdapter != null) {
            WrapperAdapterUtils.releaseAll(mCourseAdapter);
            mCourseAdapter = null;
        }

        super.onDestroy();
    }

    private void fillCourseElement() {
        mElements.clear();
        mElements.addAll(convertCourseDesignToCourseElements(getRace().getCourseDesign()));
        mCourseAdapter.notifyDataSetChanged();
    }

    private void loadMarks() {
        Loader<?> marksLoader = getLoaderManager()
            .restartLoader(0, null, mDataManager.createMarksLoader(getRace(), new LoadClient<Collection<Mark>>() {
                @Override
                public void onLoadFailed(Exception reason) {
                    String toastText = getString(R.string.marks_w_placeholder);
                    Toast.makeText(getActivity(), String.format(toastText, reason.toString()), Toast.LENGTH_LONG)
                        .show();
                }

                @Override
                public void onLoadSucceeded(Collection<Mark> data, boolean isCached) {
                    onLoadMarksSucceeded(data);
                }
            }));
        marksLoader.forceLoad();
    }

    private void onLoadMarksSucceeded(Collection<Mark> data) {
        mMarks.clear();
        mMarks.addAll(data);
        Collections.sort(mMarks, new NaturalNamedComparator());
        mMarkAdapter.notifyDataSetChanged();
    }

    protected List<CourseListDataElementWithIdImpl> convertCourseDesignToCourseElements(CourseBase courseData) {
        List<CourseListDataElementWithIdImpl> elementList = new ArrayList<>();

        for (Waypoint waypoint : courseData.getWaypoints()) {
            ControlPoint controlPoint = waypoint.getControlPoint();

            if (controlPoint instanceof Mark) {
                CourseListDataElementWithIdImpl element = new CourseListDataElementWithIdImpl();
                element.setId(mId);
                element.setLeftMark((Mark) controlPoint);
                element.setPassingInstructions(waypoint.getPassingInstructions());
                elementList.add(element);
            } else if (controlPoint instanceof ControlPointWithTwoMarks) {
                ControlPointWithTwoMarks controlPointTwoMarks = (ControlPointWithTwoMarks) controlPoint;
                CourseListDataElementWithIdImpl element = new CourseListDataElementWithIdImpl();
                element.setId(mId);
                element.setLeftMark(controlPointTwoMarks.getLeft());
                element.setRightMark(controlPointTwoMarks.getRight());
                element.setPassingInstructions(waypoint.getPassingInstructions());
                elementList.add(element);
            }
            mId++;
        }

        return elementList;
    }

    @Override
    public void onItemLongClick(CourseListDataElementWithIdImpl element) {
        createPassingInstructionDialog(element);
        mCourseAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemLongClick(Mark mark) {
        if (mElements.isEmpty()) {
            addNewCourseElementToList(mark);
        } else {
            CourseListDataElement twoMarksCourseElement = getFirstTwoMarksCourseElementWithoutRightMark();
            if (twoMarksCourseElement != null) {
                twoMarksCourseElement.setRightMark(mark);
                mCourseAdapter.notifyDataSetChanged();
            } else {
                addNewCourseElementToList(mark);
            }
        }
    }

    private CourseListDataElement getFirstTwoMarksCourseElementWithoutRightMark() {
        for (CourseListDataElement courseElement : mElements) {
            if ((courseElement.getPassingInstructions().equals(PassingInstruction.Gate) || courseElement
                .getPassingInstructions().equals(PassingInstruction.Line) || courseElement.getPassingInstructions()
                .equals(PassingInstruction.Offset)) && courseElement.getRightMark() == null) {
                return courseElement;
            }
        }
        return null;
    }

    private void addNewCourseElementToList(Mark mark) {
        CourseListDataElementWithIdImpl courseElement = new CourseListDataElementWithIdImpl();
        courseElement.setId(mId);
        courseElement.setLeftMark(mark);
        createPassingInstructionDialog(courseElement);
        mId++;
    }

    private void createPassingInstructionDialog(final CourseListDataElementWithIdImpl courseElement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_a_rounding_direction)
            .setItems(R.array.rounding_directions, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int position) {
                    PassingInstruction pickedDirection = PassingInstruction.relevantValues()[position];
                    onPassingInstructionPicked(courseElement, pickedDirection);
                }
            });
        builder.create().show();
    }

    protected void onPassingInstructionPicked(CourseListDataElementWithIdImpl courseElement,
        PassingInstruction pickedDirection) {
        courseElement.setPassingInstructions(pickedDirection);
        if (!mElements.contains(courseElement)) {
            mElements.add(courseElement);
        }
        mCourseAdapter.notifyDataSetChanged();
    }

    private void fillPreviousCourseElementsWithLastPublishedCourseDesign() {
        CourseBase lastPublishedCourseDesign = InMemoryDataStore.INSTANCE.getLastPublishedCourseDesign();
        if (lastPublishedCourseDesign != null) {
            fillPreviousCourseElementsInList(lastPublishedCourseDesign);
        }
    }

    private void fillPreviousCourseElementsInList(CourseBase previousCourseData) {
        if (previousCourseData != null) {
            mHistory.clear();
            mHistory.addAll(convertCourseDesignToCourseElements(previousCourseData));
            mHistoryAdapter.notifyDataSetChanged();
        }
    }

    private void createUsePreviousCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.use_previous_course_dialog_title));
        builder.setMessage(R.string.use_previous_course_dialog_message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                copyPreviousToNewCourseDesign();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        builder.create().show();
    }

    protected void copyPreviousToNewCourseDesign() {
        mElements.clear();
        mElements.addAll(mHistory);
        mCourseAdapter.notifyDataSetChanged();
    }

    protected CourseBase convertCourseElementsToACourseData() throws IllegalStateException, IllegalArgumentException {
        // TODO find a proper name for the highly flexible ESS courses to be shown on the regatta overview page
        CourseBase design = new CourseDataImpl("Course");
        List<Waypoint> waypoints = new ArrayList<>();

        for (CourseListDataElement courseElement : mElements) {
            if ((courseElement.getPassingInstructions().equals(PassingInstruction.Gate) || courseElement
                .getPassingInstructions().equals(PassingInstruction.Line) || courseElement.getPassingInstructions()
                .equals(PassingInstruction.Offset))) {
                if (courseElement.getRightMark() != null) {
                    String cpwtmName =
                        "ControlPointWithTwoMarks " + courseElement.getLeftMark().getName() + " / " + courseElement
                            .getRightMark().getName();
                    ControlPointWithTwoMarks cpwtm = new ControlPointWithTwoMarksImpl(courseElement.getLeftMark(),
                        courseElement.getRightMark(), cpwtmName);
                    Waypoint waypoint = new WaypointImpl(cpwtm, courseElement.getPassingInstructions());

                    waypoints.add(waypoint);
                } else {
                    throw new IllegalStateException("Missing second mark");
                }
            } else if (courseElement.getPassingInstructions().equals(PassingInstruction.None)) {
                throw new IllegalStateException("Each waypoints needs passing instructions");
            } else {
                Waypoint waypoint = new WaypointImpl(courseElement.getLeftMark(),
                    courseElement.getPassingInstructions());

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

    private void saveChangedCourseDesignInCache(CourseBase courseDesign) {
        if (!Util.isEmpty(courseDesign.getWaypoints())) {
            InMemoryDataStore.INSTANCE.setLastPublishedCourseDesign(courseDesign);
        }
    }

    protected void sendCourseDataAndDismiss(CourseBase courseDesign) {
        getRaceState().setCourseDesign(MillisecondsTimePoint.now(), courseDesign);
        saveChangedCourseDesignInCache(courseDesign);
        switch (getArguments().getInt(START_MODE, 0)) {
        case 1:
            sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
            break;

        default:
            openMainScheduleFragment();
            break;
        }
    }

    @Override
    public void onItemRemoved(int position) {
        // do nothing - later maybe show "undo" or something
    }
}
