package com.sap.sailing.dashboards.gwt.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class MovingAveragesCache {

    private Map<String, MovingAverage> averagesForStringKeys;
    
    public MovingAveragesCache() {
        averagesForStringKeys = new HashMap<String, MovingAverage>();
    }
    
    public boolean containesCacheWithKey(String key) {
        return averagesForStringKeys.containsKey(key);
    }

    public void createMovingAverageWithKeyAndSize(String key, int size) {
        if (!averagesForStringKeys.containsKey(key)) {
            averagesForStringKeys.put(key, new MovingAverage(size));
        }
    }
    
    public void addValueToAverageWithKey(String key, double value) {
        if (averagesForStringKeys.containsKey(key)) {
            MovingAverage movingAverage = averagesForStringKeys.get(key);
            movingAverage.add(value);
            averagesForStringKeys.remove(key);
            averagesForStringKeys.put(key, movingAverage);
        }
    }
    
    public Double getValueForKey(String key) {
        Double result = null;
        MovingAverage movingAverage = averagesForStringKeys.get(key);
        if(movingAverage != null) {
            result = new Double(movingAverage.getAverage());
        }
        return result;
    }
}
