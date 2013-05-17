package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindLinesDTO implements IsSerializable {
    
    private Map<PositionDTO, SortedMap<Long, List<PositionDTO>>> windLinesMap;

    public Map<PositionDTO, SortedMap<Long, List<PositionDTO>>> getWindLinesMap() {
        return windLinesMap;
    }

    public void setWindLinesMap(Map<PositionDTO, SortedMap<Long, List<PositionDTO>>> windLinesMap) {
        this.windLinesMap = windLinesMap;
    }
    
    public void addWindLine(PositionDTO position, Long timePoint, List<PositionDTO> windLine) {
        if (windLinesMap == null) {
            windLinesMap = new HashMap<PositionDTO, SortedMap<Long, List<PositionDTO>>>();
        }
        if (!windLinesMap.containsKey(position)) {
            windLinesMap.put(position, new TreeMap<Long, List<PositionDTO>>());
        }
        windLinesMap.get(position).put(timePoint, windLine);
    }
    
    public List<PositionDTO> getWindLine(PositionDTO position, Long timePoint) {
        if (windLinesMap != null) {
            if (windLinesMap.get(position) != null) {
                return windLinesMap.get(position).get(timePoint);
            }
        }
        return null;
    }
}
