package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;

public class MarksDataHandler extends DataHandler<Collection<Mark>> {

    public MarksDataHandler(OnlineDataManager manager) {
        super(manager);
    }

    @Override
    public boolean hasCachedResults() {
        return !manager.getDataStore().getMarks().isEmpty();
    }

    @Override
    public Collection<Mark> getCachedResults() {
        return manager.getDataStore().getMarks();
    }

    @Override
    public void onResult(Collection<Mark> data, boolean isCached) {
        manager.addMarks(data);
    }

}
