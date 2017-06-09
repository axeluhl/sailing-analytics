package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;

import java.util.Collection;

public class EventsDataHandler extends DataHandler<Collection<EventBase>> {

    public EventsDataHandler(OnlineDataManager manager) {
        super(manager);
    }

    @Override
    public void onResult(Collection<EventBase> data, boolean isCached) {
        manager.addEvents(data);
    }

    @Override
    public boolean hasCachedResults() {
        return !manager.getDataStore().getEvents().isEmpty();
    }

    @Override
    public Collection<EventBase> getCachedResults() {
        return manager.getDataStore().getEvents();
    }

    @Override
    public void clearCache() {
        manager.getDataStore().getEvents().clear();
    }
}
