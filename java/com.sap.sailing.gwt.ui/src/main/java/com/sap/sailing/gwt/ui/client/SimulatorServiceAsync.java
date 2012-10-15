package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.PolarDiagramDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;

public interface SimulatorServiceAsync {

    void getRaceLocations(AsyncCallback<PositionDTO[]> callback);

    void getWindLatice(WindLatticeGenParamsDTO params, AsyncCallback<WindLatticeDTO> callback);
    
    void getWindField(WindFieldGenParamsDTO params, WindPatternDisplay display, AsyncCallback<WindFieldDTO> callback);
    
    //void getPaths(WindFieldGenParamsDTO params, WindPatternDisplay display, AsyncCallback<PathDTO[]> callback);

    void getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay display, boolean withWindField,
            AsyncCallback<SimulatorResultsDTO> callback);

    void getWindPatterns(AsyncCallback<List<WindPatternDTO>> callback);

    void getWindPatternDisplay(WindPatternDTO pattern, AsyncCallback<WindPatternDisplay> callback);
    
    void getBoatClasses(AsyncCallback<BoatClassDTO[]> callback);

	void getPolarDiagram49DTO(Double bearingStep,int boatClass,
			AsyncCallback<PolarDiagramDTO> callback);

}
