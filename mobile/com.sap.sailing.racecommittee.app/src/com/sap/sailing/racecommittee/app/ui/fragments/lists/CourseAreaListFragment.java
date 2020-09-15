package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItem;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.CourseAreaSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

public class CourseAreaListFragment extends NamedListFragment<CourseArea> {
    private static final String PARAM_SELECTED_ID = "SELECTED_ID";
    private Serializable parentEventId;

    public static CourseAreaListFragment newInstance(Serializable eventId, @Nullable final String selectedId) {
        CourseAreaListFragment fragment = new CourseAreaListFragment();
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.EventIdTag, eventId);
        args.putSerializable(PARAM_SELECTED_ID, selectedId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentEventId = getArguments().getSerializable(AppConstants.EventIdTag);
    }

    @Override
    protected ItemSelectedListener<CourseArea> attachListener(Activity activity) {
        if (activity instanceof CourseAreaSelectedListenerHost) {
            CourseAreaSelectedListenerHost listener = (CourseAreaSelectedListenerHost) activity;
            return listener.getCourseAreaSelectionListener();
        }

        throw new IllegalStateException(String.format("%s cannot be attached to a instance of %s",
                CourseAreaListFragment.class.getName(), activity.getClass().getName()));
    }

    @Override
    protected LoaderCallbacks<DataLoaderResult<Collection<CourseArea>>> createLoaderCallbacks(
            ReadonlyDataManager manager) {
        return manager.createCourseAreasLoader(parentEventId, this);
    }

    @Override
    public void onLoadSucceeded(Collection<CourseArea> data, boolean isCached) {
        super.onLoadSucceeded(data, isCached);

        for (CheckedItem item : checkedItems) {
            item.setDisabled(true);
        }

        List<String> courses = AppPreferences.on(getActivity()).getManagedCourseAreaNames();
        for (CheckedItem item : checkedItems) {
            for (String allowedCourse : courses) {
                if ("*".equals(allowedCourse) || allowedCourse.equals(item.getText())) {
                    item.setDisabled(false);
                }
            }
        }
        final Bundle arguments = getArguments();
        if (arguments != null && mSelectedIndex == -1) {
            final String eventId = arguments.getString(PARAM_SELECTED_ID);
            for (CourseArea area : data) {
                if (area.getId().toString().equals(eventId)) {
                    selectEvent(area);
                }
            }
        }
    }

    @Override
    public DialogResultListener getListener() {
        return (DialogResultListener) getActivity();
    }
}
