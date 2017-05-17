package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.shared.BoatClassDTOsAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.ConfigurationException;
import com.sap.sailing.gwt.ui.shared.PolarDiagramDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.Request1TurnerDTO;
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.Response1TurnerDTO;
import com.sap.sailing.gwt.ui.shared.ResponseTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorUISelectionDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternNotFoundException;

public interface SimulatorService extends RemoteService {

    Position[] getRaceLocations();

    WindLatticeDTO getWindLatice(WindLatticeGenParamsDTO params);

    WindFieldDTO getWindField(WindFieldGenParamsDTO params, WindPatternDisplay pattern) throws WindPatternNotFoundException;

    List<WindPatternDTO> getWindPatterns(char mode);

    WindPatternDisplay getWindPatternDisplay(WindPatternDTO pattern);

    SimulatorResultsDTO getSimulatorResults(char mode, char rcDirection, WindFieldGenParamsDTO params, WindPatternDisplay pattern, boolean withWindField,
            SimulatorUISelectionDTO selection) throws WindPatternNotFoundException, ConfigurationException;

    BoatClassDTOsAndNotificationMessage getBoatClasses() throws ConfigurationException;

    PolarDiagramDTOAndNotificationMessage getPolarDiagram(Double bearingStep, int boatClassIndex) throws ConfigurationException;

    ResponseTotalTimeDTO getTotalTime(RequestTotalTimeDTO requestData) throws ConfigurationException;

    Response1TurnerDTO get1Turner(Request1TurnerDTO requestData) throws ConfigurationException;

    List<String> getLegsNames(int selectedRaceIndex);

    List<String> getRacesNames();

    List<String> getCompetitorsNames(int selectedRaceIndex);
}
