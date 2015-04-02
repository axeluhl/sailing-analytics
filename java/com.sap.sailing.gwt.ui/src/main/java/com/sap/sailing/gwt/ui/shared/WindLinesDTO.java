package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;

public class WindLinesDTO implements IsSerializable {
    
    private Map<DegreePosition, SortedMap<Long, List<DegreePosition>>> windLinesMap;

    public Map<DegreePosition, SortedMap<Long, List<DegreePosition>>> getWindLinesMap() {
        return windLinesMap;
    }

    public void setWindLinesMap(Map<DegreePosition, SortedMap<Long, List<DegreePosition>>> windLinesMap) {
        this.windLinesMap = windLinesMap;
    }
    
    public void addWindLine(DegreePosition position, Long timePoint, List<DegreePosition> windLine) {
        if (windLinesMap == null) {
            windLinesMap = new HashMap<DegreePosition, SortedMap<Long, List<DegreePosition>>>();
        }
        if (!windLinesMap.containsKey(position)) {
            windLinesMap.put(position, new TreeMap<Long, List<DegreePosition>>());
        }
        windLinesMap.get(position).put(timePoint, windLine);
    }
    
    public List<DegreePosition> getWindLine(Position position, Long timePoint) {
        if (windLinesMap != null) {
            if (windLinesMap.get(position) != null) {
                return windLinesMap.get(position).get(timePoint);
            }
        }
        return null;
    }
}
