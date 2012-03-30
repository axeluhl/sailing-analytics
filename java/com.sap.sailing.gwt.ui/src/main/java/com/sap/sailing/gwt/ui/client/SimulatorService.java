package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.simulator.WindLatticeDTO;
import com.sap.sailing.gwt.ui.simulator.WindLatticeGenParamsDTO;

@RemoteServiceRelativePath("simulator")
public interface SimulatorService extends RemoteService {
	
	public PositionDTO[] getRaceLocations();
	
	public WindLatticeDTO getWindLatice(WindLatticeGenParamsDTO params);
}
