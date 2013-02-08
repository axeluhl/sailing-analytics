package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class RaceLogsDataHandler extends DataHandler<Map<ManagedRace, Collection<RaceLogEvent>>> {

	public RaceLogsDataHandler(DataManager manager, LoadClient<Map<ManagedRace, Collection<RaceLogEvent>>> client) {
		super(manager, client);
	}
	
	@Override
	public void onLoaded(Map<ManagedRace, Collection<RaceLogEvent>> data) {
		super.onLoaded(data);
		System.out.println(data.size());
	}

}
