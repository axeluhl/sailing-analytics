package com.sap.sailing.gwt.ui.shared.dispatch.regatta;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class RegattaProgressDTO implements DTO {
    private ArrayList<RegattaProgressSeriesDTO> series = new ArrayList<>();
    
    public void addSeries(RegattaProgressSeriesDTO regattaProgressOfSeries) {
        series.add(regattaProgressOfSeries);
    }
    
    public List<RegattaProgressSeriesDTO> getSeries() {
        return series;
    }
}
