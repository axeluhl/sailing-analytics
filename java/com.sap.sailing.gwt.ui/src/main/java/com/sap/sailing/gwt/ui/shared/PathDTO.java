package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PathDTO implements IsSerializable {

    private List<WindDTO> matrix;

    public List<WindDTO> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<WindDTO> matrix) {
        this.matrix = matrix;
    }

}
