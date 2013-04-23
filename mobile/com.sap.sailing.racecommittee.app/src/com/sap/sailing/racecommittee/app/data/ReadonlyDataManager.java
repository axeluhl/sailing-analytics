package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public interface ReadonlyDataManager {

    public DataStore getDataStore();

    public void loadEvents(LoadClient<Collection<EventBase>> client);

    public void loadCourseAreas(Serializable parentEventId, LoadClient<Collection<CourseArea>> client);

    public void loadRaces(Serializable courseAreaId, LoadClient<Collection<ManagedRace>> client);

    public void loadMarks(ManagedRace managedRace, LoadClient<Collection<Mark>> client);
    
    public void loadCourse(ManagedRace managedRace, LoadClient<CourseBase> client);
}
