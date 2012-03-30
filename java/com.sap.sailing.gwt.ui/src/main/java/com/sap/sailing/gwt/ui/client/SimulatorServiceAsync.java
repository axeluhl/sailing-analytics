package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.simulator.WindLatticeDTO;
import com.sap.sailing.gwt.ui.simulator.WindLatticeGenParamsDTO;

public interface SimulatorServiceAsync {

	void getRaceLocations(AsyncCallback<PositionDTO[]> callback);

	void getWindLatice(WindLatticeGenParamsDTO params,
			AsyncCallback<WindLatticeDTO> callback);

}
