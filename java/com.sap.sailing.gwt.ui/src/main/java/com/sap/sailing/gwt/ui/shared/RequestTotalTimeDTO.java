package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RequestTotalTimeDTO implements IsSerializable {

    public int selectedBoatClassIndex = 0;
    public List<SimulatorWindDTO> allPoints = null;
    public List<PositionDTO> turnPoints = null;
    public boolean useRealAverageWindSpeed = false;
    public int stepDurationMilliseconds = 0;
    public boolean debugMode = false;
    public int selectedRaceIndex;
    public int selectedCompetitorIndex;
    public int selectedLegIndex;

    public RequestTotalTimeDTO() {
        this.selectedBoatClassIndex = 0;
        this.allPoints = new ArrayList<SimulatorWindDTO>();
        this.turnPoints = new ArrayList<PositionDTO>();
        this.useRealAverageWindSpeed = true;
        this.stepDurationMilliseconds = 2000;
        this.debugMode = false;
    }

    public RequestTotalTimeDTO(int selectedBoatClassIndex, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex,
            int stepDurationMilliseconds,
            List<SimulatorWindDTO> allPoints, List<PositionDTO> turnPoints, boolean useRealAverageWindSpeed, boolean debugMode) {
        this.selectedBoatClassIndex = selectedBoatClassIndex;
        this.allPoints = allPoints;
        this.turnPoints = turnPoints;
        this.useRealAverageWindSpeed = useRealAverageWindSpeed;
        this.stepDurationMilliseconds = stepDurationMilliseconds;

        this.selectedRaceIndex = selectedRaceIndex;
        this.selectedCompetitorIndex = selectedCompetitorIndex;
        this.selectedLegIndex = selectedLegIndex;

        this.debugMode = debugMode;
    }
}
