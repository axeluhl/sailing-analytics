package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.os.Bundle;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItem;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.CourseAreaSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

public class CourseAreaListFragment extends NamedListFragment<CourseArea> {

    private Serializable parentEventId;

    public static CourseAreaListFragment newInstance(Serializable eventId) {
        CourseAreaListFragment fragment = new CourseAreaListFragment();
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.EventIdTag, eventId);
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

        throw new IllegalStateException(String
            .format("%s cannot be attached to a instance of %s", CourseAreaListFragment.class.getName(), activity.getClass().getName()));
    }

    @Override
    protected LoaderCallbacks<DataLoaderResult<Collection<CourseArea>>> createLoaderCallbacks(ReadonlyDataManager manager) {
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
    }

    @Override
    public DialogResultListener getListener() {
        return (DialogResultListener) getActivity();
    }
}
