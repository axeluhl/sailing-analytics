package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindFieldDTO implements IsSerializable {

    public double curSpeed;
    public double curBearing;
    
    public String windDataJSON;
    
    private List<SimulatorWindDTO> matrix;
    
    private WindLinesDTO windLinesDTO;

    public List<SimulatorWindDTO> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<SimulatorWindDTO> matrix) {
        this.matrix = matrix;
    }

    public WindLinesDTO getWindLinesDTO() {
        return windLinesDTO;
    }

    public void setWindLinesDTO(WindLinesDTO windLinesDTO) {
        this.windLinesDTO = windLinesDTO;
    }

}
