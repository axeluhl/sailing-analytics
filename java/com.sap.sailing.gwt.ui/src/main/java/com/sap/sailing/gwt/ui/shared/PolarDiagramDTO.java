package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PolarDiagramDTO implements IsSerializable {
    private Number[][] series;

    public PolarDiagramDTO() {
        this.series = null;
    }

    public void setNumberSeries(Number[][] series) {
        this.series = series;
    }

    public Number[][] getNumberSeries() {
        return series;
    }
}
