package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.util.Collection;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

public class EventListFragment extends NamedListFragment<EventBase> {

    private static final String PARAM_EVENT_ID = "EVENT_ID";

    public static EventListFragment newInstance(@Nullable final String eventId) {
        final EventListFragment fragment = new EventListFragment();
        final Bundle args = new Bundle();
        args.putString(PARAM_EVENT_ID, eventId);
        fragment.setArguments(args);
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

    @Override
    public void onLoadSucceeded(Collection<EventBase> data, boolean isCached) {
        super.onLoadSucceeded(data, isCached);
        final Bundle arguments = getArguments();
        if (arguments != null && mSelectedIndex == -1) {
            final String eventId = arguments.getString(PARAM_EVENT_ID);
            for (EventBase eventBase : data) {
                if (eventBase.getId().toString().equals(eventId)) {
                    selectEvent(eventBase);
                }
            }
        }
    }
}
