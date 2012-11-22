package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.BoatClassDTOsAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.ConfigurationException;
import com.sap.sailing.gwt.ui.shared.PolarDiagramDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.ReceivePolarDiagramDataDTO;
import com.sap.sailing.gwt.ui.shared.RequestPolarDiagramDataDTO;
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

    SimulatorResultsDTOAndNotificationMessage getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay pattern, boolean withWindField, int boatClassIndex) throws WindPatternNotFoundException, ConfigurationException;

    BoatClassDTOsAndNotificationMessage getBoatClasses() throws ConfigurationException;

    PolarDiagramDTOAndNotificationMessage getPolarDiagramDTO(Double bearingStep, int boatClassIndex) throws ConfigurationException;

    ReceivePolarDiagramDataDTO getSpeedsFromPolarDiagram(RequestPolarDiagramDataDTO requestData) throws ConfigurationException;
}
