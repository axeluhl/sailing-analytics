package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.util.Collection;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

import android.app.Activity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

public class EventListFragment extends NamedListFragment<EventBase> {

    public static EventListFragment newInstance() {
        EventListFragment fragment = new EventListFragment();
        return fragment;
    }

    @Override
    protected ItemSelectedListener<EventBase> attachListener(Activity activity) {
        if (activity instanceof EventSelectedListenerHost) {
            EventSelectedListenerHost listener = (EventSelectedListenerHost) activity;
            return listener.getEventSelectionListener();
        }

        throw new IllegalStateException(String.format("%s cannot be attached to a instance of %s",
                EventListFragment.class.getName(), activity.getClass().getName()));
    }

    @Override
    protected LoaderCallbacks<DataLoaderResult<Collection<EventBase>>> createLoaderCallbacks(
            ReadonlyDataManager manager) {
        return manager.createEventsLoader(this);
    }

    @Override
    public DialogResultListener getListener() {
        return (DialogResultListener) getActivity();
    }
}
