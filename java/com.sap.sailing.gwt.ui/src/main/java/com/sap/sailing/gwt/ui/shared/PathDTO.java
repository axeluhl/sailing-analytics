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
    
    public long getPathTime() {
    	if (matrix == null || matrix.size() == 0) {
    		return 0;
    	}
    	
    	return matrix.get(matrix.size()-1).timepoint - matrix.get(0).timepoint;
    }

}
