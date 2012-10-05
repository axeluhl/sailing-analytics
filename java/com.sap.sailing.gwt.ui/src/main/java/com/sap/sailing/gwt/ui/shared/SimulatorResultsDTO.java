package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatorResultsDTO implements IsSerializable {

    public RaceMapDataDTO raceCourse;
    public WindFieldDTO windField;
    public PathDTO[] paths;
    
    public SimulatorResultsDTO(){
        raceCourse = null;
        windField = null;
        paths = null;
    }
    
    public SimulatorResultsDTO(RaceMapDataDTO raceCourse, PathDTO[] paths, WindFieldDTO wf) {
        this.raceCourse = raceCourse;
        this.paths = paths;
        this.windField = wf;
    }
}
