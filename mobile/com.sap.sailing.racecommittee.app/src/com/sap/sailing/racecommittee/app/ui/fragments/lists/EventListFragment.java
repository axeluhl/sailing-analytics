package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.EventsDataHandler;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoader;
import com.sap.sailing.racecommittee.app.data.parsers.DataParser;
import com.sap.sailing.racecommittee.app.data.parsers.EventsDataParser;
import com.sap.sailing.racecommittee.app.domain.impl.DomainFactoryImpl;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;

public class EventListFragment extends NamedListFragment<EventBase> implements LoaderCallbacks<Collection<EventBase>> {
    
    @Override
    public void onResume() {
        super.onResume();
        loadItems();
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
    protected String getHeaderText() {
        return getString(R.string.label_login_events);
    }

    @Override
    protected void loadItems(ReadonlyDataManager manager) {
        /*manager.loadEvents(this);*/
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public Loader<Collection<EventBase>> onCreateLoader(int id, Bundle args) {
        DataParser<Collection<EventBase>> parser = new EventsDataParser(new EventBaseJsonDeserializer(
                new VenueJsonDeserializer(new CourseAreaJsonDeserializer(DomainFactoryImpl.INSTANCE))));
        try {
            return new DataLoader<Collection<EventBase>>(
                    getActivity(), 
                    URI.create(AppPreferences.getServerBaseURL(getActivity()) + "/sailingserver/events"), 
                    parser, 
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Collection<EventBase>> arg0, Collection<EventBase> arg1) {
        if (arg1 == null)
            ExLog.w("BLUB", "NULL");
        else
            super.onLoadSucceded(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Collection<EventBase>> arg0) {
        super.onLoadSucceded(Collections.<EventBase>emptyList());
    }

}
