package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.text.TextUtils;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

import java.util.Collection;

public class EventListFragment extends NamedListFragment<EventBase> {

    public static EventListFragment newInstance(boolean forceLoad, @Nullable final String id) {
        final EventListFragment fragment = new EventListFragment();
        final Bundle args = new Bundle();
        args.putBoolean(AppConstants.ACTION_EXTRA_FORCED, forceLoad);
        if (!TextUtils.isEmpty(id)) {
            args.putString(AppConstants.EXTRA_EVENT_ID, id);
        }
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
            final String id = arguments.getString(AppConstants.EXTRA_EVENT_ID);
            for (EventBase event : data) {
                if (event.getId().toString().equals(id)) {
                    selectItem(event);
                    break;
                }
            }
        }
    }
}
