package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class CourseDataHandler extends DataHandler<CourseBase> {
    
    private ManagedRace race;

    public CourseDataHandler(OnlineDataManager manager, ManagedRace managedRace) {
        super(manager);
        race = managedRace;
    }

    @Override
    public void onResult(CourseBase data) {
        race.setCourseOnServer(data);
    }

}
