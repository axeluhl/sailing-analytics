package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindFieldDTO implements IsSerializable {

    private List<WindDTO> matrix;
    
    private WindLinesDTO windLinesDTO;

    public List<WindDTO> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<WindDTO> matrix) {
        this.matrix = matrix;
    }

    public WindLinesDTO getWindLinesDTO() {
        return windLinesDTO;
    }

    public void setWindLinesDTO(WindLinesDTO windLinesDTO) {
        this.windLinesDTO = windLinesDTO;
    }

}
