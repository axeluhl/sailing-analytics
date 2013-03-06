package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.BoatClassDTOsAndNotificationMessage;
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

public interface SimulatorServiceAsync {

    void getRaceLocations(AsyncCallback<PositionDTO[]> callback);

    void getWindLatice(WindLatticeGenParamsDTO params, AsyncCallback<WindLatticeDTO> callback);

    void getWindField(WindFieldGenParamsDTO params, WindPatternDisplay display, AsyncCallback<WindFieldDTO> callback);

    void getWindPatterns(AsyncCallback<List<WindPatternDTO>> callback);

    void getWindPatternDisplay(WindPatternDTO pattern, AsyncCallback<WindPatternDisplay> callback);

    void getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay display, boolean withWindField, int boatClassIndex,
            AsyncCallback<SimulatorResultsDTO> callback);

    void getBoatClasses(AsyncCallback<BoatClassDTOsAndNotificationMessage> callback);

    void getPolarDiagram(Double bearingStep, int boatClassIndex, AsyncCallback<PolarDiagramDTOAndNotificationMessage> callback);

    void getTotalTime_old(RequestTotalTimeDTO requestData, AsyncCallback<ResponseTotalTimeDTO> asyncCallback);

    void getTotalTime_new(RequestTotalTimeDTO requestData, AsyncCallback<ResponseTotalTimeDTO> asyncCallback);

    void get1Turner(final Request1TurnerDTO requestData, AsyncCallback<Response1TurnerDTO> asyncCallback);

    void getLegsNames(AsyncCallback<List<String>> asyncCallback);

    void getRacesNames(AsyncCallback<List<String>> asyncCallback);

    void getSimulatorResults(char mode, WindFieldGenParamsDTO params, WindPatternDisplay pattern, boolean withWindField, int boatClassIndex, int legIndex,
            int competitorIndex, AsyncCallback<SimulatorResultsDTO> callback);
}
