package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class ManagedRacesDataHandler extends DataHandler<Collection<ManagedRace>> {

	public ManagedRacesDataHandler(OnlineDataManager manager, LoadClient<Collection<ManagedRace>> client) {
		super(manager, client);
	}
	
	@Override
	public void onLoaded(Collection<ManagedRace> data) {
		super.onLoaded(data);
		manager.addRaces(data);
	}

}
