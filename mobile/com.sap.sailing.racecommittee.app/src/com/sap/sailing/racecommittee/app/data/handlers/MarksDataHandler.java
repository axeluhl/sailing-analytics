package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;

import java.util.Collection;

public class MarksDataHandler extends DataHandler<Collection<Mark>> {

    private final RaceGroup raceGroup;

    public MarksDataHandler(OnlineDataManager manager, RaceGroup raceGroup) {
        super(manager);
        this.raceGroup = raceGroup;
    }

    @Override
    public boolean hasCachedResults() {
        return !manager.getDataStore().getMarks(raceGroup).isEmpty();
    }

    @Override
    public Collection<Mark> getCachedResults() {
        return manager.getDataStore().getMarks(raceGroup);
    }

    @Override
    public void onResult(Collection<Mark> data, boolean isCached) {
        manager.addMarks(raceGroup, data);
    }

}
