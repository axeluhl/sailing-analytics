package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class CourseBaseHandler extends DataHandler<CourseBase> {

    private ManagedRace race;

    public CourseBaseHandler(OnlineDataManager manager, ManagedRace managedRace) {
        super(manager);
        race = managedRace;
    }

    @Override
    public boolean hasCachedResults() {
        return race.getCourseOnServer() != null;
    }

    @Override
    public CourseBase getCachedResults() {
        return race.getCourseOnServer();
    }

    @Override
    public void onResult(CourseBase data, boolean isCached) {
        race.setCourseOnServer(data);
    }

}
