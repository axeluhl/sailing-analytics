package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.tracking.DeviceMapping;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.impl.DeviceMappingImpl;
import com.sap.sailing.domain.tracking.impl.TimedComparator;

public abstract class DeviceMappingFinder<ItemT extends WithID> extends RaceLogAnalyzer<Map<ItemT, List<DeviceMapping<ItemT>>>> {
    public DeviceMappingFinder(RaceLog raceLog) {
		super(raceLog);
	}
    
    protected abstract boolean isValidMapping(DeviceMappingEvent<?> mapping);

	protected DeviceMapping<ItemT> getMapping(DeviceMappingEvent<ItemT> event) {
		return new DeviceMappingImpl<ItemT>(event.getMappedTo(), event.getDevice(),
				new TimeRangeImpl(event.getFrom(), event.getTo()));
	}

	protected DeviceMapping<ItemT> getMapping(DeviceIdentifier device, ItemT item,
			TimePoint from, TimePoint to) {
		return new DeviceMappingImpl<ItemT>(item, device, new TimeRangeImpl(from, to));
	}

    private List<DeviceMapping<ItemT>> getItemSet(
    		Map<ItemT, List<DeviceMapping<ItemT>>> map, ItemT item) {
    	List<DeviceMapping<ItemT>> list = map.get(item);
        if (list == null) {
            list = new ArrayList<DeviceMapping<ItemT>>();
            map.put(item, list);
        }
        return list;
    }
    
    /**
     * If an overlap in time is found between the this mapping, and one already found, the new mapping overrides
     * the one in the initialSet for the time of the overlap.
     * @param initial
     * @param toBeAdded
     * @return
     */
    private List<DeviceMapping<ItemT>> getOverlapFreeMappings(List<DeviceMapping<ItemT>> initial,
    		DeviceMapping<ItemT> toBeAdded) {
        TimeRange timeRange = toBeAdded.getTimeRange();
        ItemT item = toBeAdded.getMappedTo();
        DeviceIdentifier device = toBeAdded.getDevice();
    	List<DeviceMapping<ItemT>> result = new ArrayList<DeviceMapping<ItemT>>();
        result.add(toBeAdded);
        
        for (DeviceMapping<ItemT> otherMapping : initial) {
            TimeRange otherTimeRange = otherMapping.getTimeRange();
            
            if (otherTimeRange.intersects(timeRange)) {
                /*
                 * one of the following can be true (--- is timeRange of toBeAdded, xxx is timeRange of otherMapping)
                 *      xxxxxxxxxxxx            timeRange of the otherMapping
                 *      
                 *     --------------           case 1: overwrite otherMapping completely
                 *      xxx----xxxxx            case 2: keep two parts of otherMapping (combination of 3 & 4)
                 *     --------xxxxx            case 3: keep only right part of otherMapping
                 *      xxx----------           case 4: keep only left part of otherMapping
                 */
                if (otherTimeRange.liesWithin(timeRange)) {
                    //case 1: just ignore the other mapping, as it is completely overwritten by the new one
                } else {
                    if (otherTimeRange.startsBefore(timeRange)) {
                        //case 4
                        result.add(getMapping(device, item, otherTimeRange.from(), timeRange.from().minus(1)));
                    }
                    if (otherTimeRange.endsAfter(timeRange)) {
                        //case 3
                    	result.add(getMapping(device, item, timeRange.to().plus(1), otherTimeRange.to()));
                    }
                }
            } else {
                //no overlap between these mappings, can just add the other one
                result.add(otherMapping);
            }
        }
        return result;
    }

    @Override
    protected Map<ItemT, List<DeviceMapping<ItemT>>> performAnalysis() {
    	Map<ItemT, List<DeviceMapping<ItemT>>> result = new HashMap<ItemT, List<DeviceMapping<ItemT>>>();
        
        /* iterate over events in order
         * events with higher importance (later, higher author prio) are located towards the end, and should
         * therefore override those before
         */
        for (RaceLogEvent e : raceLog.getUnrevokedEvents()) {
            if (e instanceof DeviceMappingEvent && isValidMapping(((DeviceMappingEvent<?>) e))) {
                @SuppressWarnings("unchecked")
				DeviceMappingEvent<ItemT> mappingEvent = (DeviceMappingEvent<ItemT>) e;
                ItemT item = mappingEvent.getMappedTo();
                List<DeviceMapping<ItemT>> mappings = getOverlapFreeMappings(getItemSet(result, item), getMapping(mappingEvent));
                Collections.sort(mappings, new TimedComparator());
                result.put(item, getOverlapFreeMappings(getItemSet(result, item), getMapping(mappingEvent)));
            }
        }
        
        return result;
    }
}
