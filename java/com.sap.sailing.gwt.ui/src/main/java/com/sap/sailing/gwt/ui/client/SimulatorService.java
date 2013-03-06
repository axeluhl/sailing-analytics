package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.BoatClassDTOsAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.ConfigurationException;
import com.sap.sailing.gwt.ui.shared.PolarDiagramDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.Request1TurnerDTO;
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.Response1TurnerDTO;
import com.sap.sailing.gwt.ui.shared.ResponseTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
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

    WindFieldDTO getWindField(WindFieldGenParamsDTO params, WindPatternDisplay pattern) throws WindPatternNotFoundException;

    List<WindPatternDTO> getWindPatterns();

    WindPatternDisplay getWindPatternDisplay(WindPatternDTO pattern);

    // SimulatorResultsDTO getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay pattern,
    // boolean withWindField, int boatClassIndex)
    // throws WindPatternNotFoundException, ConfigurationException;

    SimulatorResultsDTO getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay pattern, boolean withWindField,
            int selectedBoatClassIndex, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) throws WindPatternNotFoundException,
            ConfigurationException;

    BoatClassDTOsAndNotificationMessage getBoatClasses() throws ConfigurationException;

    PolarDiagramDTOAndNotificationMessage getPolarDiagram(Double bearingStep, int boatClassIndex) throws ConfigurationException;

    ResponseTotalTimeDTO getTotalTime_old(RequestTotalTimeDTO requestData) throws ConfigurationException;

    ResponseTotalTimeDTO getTotalTime_new(RequestTotalTimeDTO requestData) throws ConfigurationException;

    Response1TurnerDTO get1Turner(Request1TurnerDTO requestData) throws ConfigurationException;

    List<String> getLegsNames(int selectedRaceIndex);

    List<String> getRacesNames();

    List<String> getCompetitorsNames(int selectedRaceIndex);
}
