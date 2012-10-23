package com.sap.sailing.gwt.ui.shared;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindLinesDTO implements IsSerializable {
    
    private SortedMap<Long, List<PositionDTO>> windLinesMap;

    public SortedMap<Long, List<PositionDTO>> getWindLinesMap() {
        return windLinesMap;
    }

    public void setWindLinesMap(SortedMap<Long, List<PositionDTO>> windLinesMap) {
        this.windLinesMap = windLinesMap;
    }
    
    public void addWindLine(Long timePoint, List<PositionDTO> windLine) {
        if (windLinesMap == null) {
            windLinesMap = new TreeMap<Long, List<PositionDTO>>();
        }
        windLinesMap.put(timePoint, windLine);
    }
    
    public List<PositionDTO> getWindLine(Long timePoint) {
        if (windLinesMap != null) {
            return windLinesMap.get(timePoint);
        }
        return null;
    }
}
