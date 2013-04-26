package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class CourseDataHandler extends DataHandler<CourseBase> {
    
    private ManagedRace race;

    public CourseDataHandler(OnlineDataManager manager, LoadClient<CourseBase> client, ManagedRace managedRace) {
        super(manager, client);
        race = managedRace;
    }

    @Override
    public void onLoaded(CourseBase data) {
        super.onLoaded(data);
        race.setCourseOnServer(data);
    }

}
