package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.util.Collection;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

public class EventListFragment extends NamedListFragment<EventBase> {

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
    protected String getHeaderText() {
        return getString(R.string.label_login_events);
    }

    @Override
    protected LoaderCallbacks<DataLoaderResult<Collection<EventBase>>> createLoaderCallbacks(ReadonlyDataManager manager) {
        return manager.createEventsLoader(this);
    }
}
