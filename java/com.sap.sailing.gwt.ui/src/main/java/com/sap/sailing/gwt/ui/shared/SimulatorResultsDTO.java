package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatorResultsDTO implements IsSerializable {

    public WindFieldDTO windField;
    public PathDTO[] paths;
    
    public SimulatorResultsDTO(){
        windField = null;
        paths = null;
    }
    
    public SimulatorResultsDTO(PathDTO[] paths, WindFieldDTO wf) {
        this.paths = paths;
        this.windField = wf;
    }
}
