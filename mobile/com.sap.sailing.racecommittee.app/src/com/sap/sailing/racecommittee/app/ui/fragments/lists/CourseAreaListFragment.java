package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.os.Bundle;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.adapters.CourseAreaArrayAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.NamedArrayAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DialogListenerHost;
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
    protected NamedArrayAdapter<CourseArea> createAdapter(Context context, ArrayList<CourseArea> items) {
        return new CourseAreaArrayAdapter(context, items);
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
    protected LoaderCallbacks<DataLoaderResult<Collection<CourseArea>>> createLoaderCallbacks(ReadonlyDataManager manager) {
        return manager.createCourseAreasLoader(parentEventId, this);
    }

    @Override
    public DialogResultListener getListener() {
        return (DialogResultListener) getActivity();
    }
}
