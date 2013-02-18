package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public interface ReadonlyDataManager {
	
	public DataStore getDataStore();
	
	public void loadEvents(LoadClient<Collection<EventData>> client);

	public void loadCourseAreas(Serializable parentEventId, LoadClient<Collection<CourseArea>> client);
	
	public void loadRaces(Serializable courseAreaId, LoadClient<Collection<ManagedRace>> client);
	
}
