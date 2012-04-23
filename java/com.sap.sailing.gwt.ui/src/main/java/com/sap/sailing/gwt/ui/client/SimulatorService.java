package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;

@RemoteServiceRelativePath("simulator")
public interface SimulatorService extends RemoteService {
	
	public PositionDTO[] getRaceLocations();
	
	public WindLatticeDTO getWindLatice(WindLatticeGenParamsDTO params);
	
	public WindFieldDTO getWindField(WindFieldGenParamsDTO params);
}
