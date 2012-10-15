package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.BoatClassDTOsAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.ConfigurationException;
import com.sap.sailing.gwt.ui.shared.PolarDiagramDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternNotFoundException;

@RemoteServiceRelativePath("simulator")
public interface SimulatorService extends RemoteService {

    PositionDTO[] getRaceLocations();

    WindLatticeDTO getWindLatice(WindLatticeGenParamsDTO params);

    //PathDTO[] getPaths(WindFieldGenParamsDTO params, WindPatternDisplay pattern) throws WindPatternNotFoundException;

    List<WindPatternDTO> getWindPatterns();

    WindPatternDisplay getWindPatternDisplay(WindPatternDTO pattern);

    WindFieldDTO getWindField(WindFieldGenParamsDTO params, WindPatternDisplay pattern) throws WindPatternNotFoundException;
    
    //I00788 - Mihai Bogdan Eugen
    SimulatorResultsDTOAndNotificationMessage getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay pattern, boolean withWindField, int boatClassIndex) throws WindPatternNotFoundException, ConfigurationException;

    //I00788 - Mihai Bogdan Eugen
    BoatClassDTOsAndNotificationMessage getBoatClasses() throws ConfigurationException;

    //I00788 - Mihai Bogdan Eugen
    PolarDiagramDTOAndNotificationMessage getPolarDiagramDTO(Double bearingStep, int boatClassIndex) throws ConfigurationException;
}
