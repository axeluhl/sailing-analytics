package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public interface ReadonlyDataManager {
	public DataStore getDataStore();
	
	public void loadEvents(LoadClient<Collection<Event>> client);

	public void loadCourseAreas(Serializable parentEventId, LoadClient<Collection<CourseArea>> client);
	
	public void loadRaces(Serializable courseAreaId, LoadClient<Collection<ManagedRace>> client);

	public void loadRaceLogs(
			Collection<ManagedRace> data, 
			LoadClient<Map<ManagedRace, Collection<RaceLogEvent>>> client);
	
}
