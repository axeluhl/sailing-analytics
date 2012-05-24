package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PathDTO extends NamedDTO implements IsSerializable {

    private List<WindDTO> matrix;
    
    public PathDTO() {
        
    }
    
    public PathDTO(String name) {
        super(name);
    }
    
    public List<WindDTO> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<WindDTO> matrix) {
        this.matrix = matrix;
    }

}
