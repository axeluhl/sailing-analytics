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
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.impl.RaceLogEventComparator;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceMapping;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.impl.DeviceMappingImpl;
import com.sap.sailing.domain.tracking.impl.TimedComparator;

/**
 * Extracts the {@link DeviceMapping}s with the appropriate {@link TimeRange}s from the {@link RaceLog}.
 * If there are overlapping mappings for an {@code item} (overlap in time), then the resulting mappings are constructed as follows:
 * 
 * Depending on the overlap between two mappings for the same item, one of the following cases is true
 * (--- is timeRange of higher priority mapping, xxx is timeRange of lower priority mapping):
 *      xxxxxxxxxxxx            starting situation
 *     --------------           case 1: higher prio range completely includes other: overwrite other completely
 *      xxx----xxxxx            case 2: higher prio range lies within other: keep two parts of other (combination of 3 & 4)
 *     --------xxxxx            case 3: higher prio range ends within other: keep only right part of other
 *      xxx----------           case 4: higher prio range starts within other: keep only left part of other
 *      
 * If both mappings are for the same {@code item} and {@code device}, they can however simply be merged.
 *      
 * As stated in {@link DeviceMappingEvent}, an open-ended mapping can be closed by another open-ended mapping for the same
 * {@code item} and {@link competitor}.
 * If there are several possible {@link DeviceMappingEvent}s that could close an open range, the closest one is chosen.
 * An open-ended {@link DeviceMappingEvent} can close several others.
 */
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

	protected DeviceMapping<ItemT> getMapping(DeviceIdentifier device, ItemT item,
			TimeRange range) {
		return new DeviceMappingImpl<ItemT>(item, device, range);
	}

    private <T> List<DeviceMapping<ItemT>> getItemSet(Map<T, List<DeviceMapping<ItemT>>> map, T item) {
    	List<DeviceMapping<ItemT>> list = map.get(item);
        if (list == null) {
            list = new ArrayList<DeviceMapping<ItemT>>();
            map.put(item, list);
        }
        return list;
    }
    
    /**
     * If an overlap in time is found between this mapping, and one already found for the same {@code item}, the new
     * mapping overrides the one in the initialSet for the time of the overlap.
     */
    private List<DeviceMapping<ItemT>> getOverlapFreeMappings(List<DeviceMapping<ItemT>> initial,
    		DeviceMapping<ItemT> toBeAdded) {
        TimeRange timeRange = toBeAdded.getTimeRange();
        ItemT item = toBeAdded.getMappedTo();
        DeviceIdentifier device = toBeAdded.getDevice();
    	List<DeviceMapping<ItemT>> result = new ArrayList<DeviceMapping<ItemT>>();
        
        
        for (DeviceMapping<ItemT> otherMapping : initial) {
        	TimeRange otherTimeRange = otherMapping.getTimeRange();
        	
        	//time ranges intersect? further investigate
        	if (otherTimeRange.intersects(timeRange)) {

        		//same device
        		if (otherMapping.getDevice() == device) {
        			
        			//two open ranges that close each other
        			if ((otherTimeRange.openEnd() && timeRange.openBeginning() && otherTimeRange.from().before(timeRange.to())) ||
        					(timeRange.openEnd() && otherTimeRange.openBeginning() && timeRange.from().before(otherTimeRange.to()))) {
        				//shouldn't happen here any more -> dealt with in combineOpenRanges
        				//timeRange = timeRange.intersection(otherTimeRange);
        				
        			//otherwise merge
        			} else {
        				timeRange = timeRange.union(otherTimeRange);
        			}

        		//different device -> transform according to four cases
        		} else {
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
        		}
        		
        	//no intersection -> add both
        	} else {
        		result.add(otherMapping);
        	}
        }
        
        //other mappings have been checked for conflicts, now add toBeAdded (time range may have grown through merges)
        result.add(getMapping(device, item, timeRange));
        
        return result;
    }

    @Override
    protected Map<ItemT, List<DeviceMapping<ItemT>>> performAnalysis() {
    	Map<ItemT, List<DeviceMapping<ItemT>>> preliminary = new HashMap<ItemT, List<DeviceMapping<ItemT>>>();
    	Map<ItemT, List<DeviceMapping<ItemT>>> result = new HashMap<ItemT, List<DeviceMapping<ItemT>>>();
        
        /* iterate over events in order
         * -> events with higher importance (later, higher author prio) are located towards the end, and should
         * therefore override those before
         */
        for (RaceLogEvent e : raceLog.getUnrevokedEvents()) {
            if (e instanceof DeviceMappingEvent && isValidMapping(((DeviceMappingEvent<?>) e))) {
                @SuppressWarnings("unchecked")
				DeviceMappingEvent<ItemT> mappingEvent = (DeviceMappingEvent<ItemT>) e;
                ItemT item = mappingEvent.getMappedTo();
                getItemSet(preliminary, item).add(getMapping(mappingEvent));
            }
        }
        
        //combine mappings that close each other
        for (ItemT item : preliminary.keySet()) {
        	preliminary.put(item, combineOpenRanges(preliminary.get(item), item));
        }
        
        //resolve overlaps
        for (ItemT item : preliminary.keySet()) {
        	for (DeviceMapping<ItemT> mapping : preliminary.get(item)) {
                List<DeviceMapping<ItemT>> mappings = getOverlapFreeMappings(getItemSet(result, item), mapping);
                Collections.sort(mappings, new TimedComparator());
                result.put(item, mappings);
        	}
        }
        
        return result;
    }

    /**
     * Combines mappings with open ranges for same device, that also close each other.
     * If no matching open-ended mapping is found at all, the mapping is left untouched.
     * One open-ended mapping can close multiple other open-ended mappings, if it is the closest for those.
     * Mappings without open end are left untouched.
     */
	private List<DeviceMapping<ItemT>> combineOpenRanges(List<DeviceMapping<ItemT>> list, ItemT item) {
		//sort by devices
		Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> byDevice = new HashMap<DeviceIdentifier, List<DeviceMapping<ItemT>>>();
		for (DeviceMapping<ItemT> mapping : list) {
			getItemSet(byDevice, mapping.getDevice()).add(mapping);
		}
		
		List<DeviceMapping<ItemT>> overallResult = new ArrayList<DeviceMapping<ItemT>>(); 

		for (DeviceIdentifier device : byDevice.keySet()) {
			List<DeviceMapping<ItemT>> result = new ArrayList<DeviceMapping<ItemT>>(); 
			List<DeviceMapping<ItemT>> openEnds = new ArrayList<DeviceMapping<ItemT>>();
			List<DeviceMapping<ItemT>> openBeginnings = new ArrayList<DeviceMapping<ItemT>>();
			
			//find open-ended
			for (DeviceMapping<ItemT> mapping : list) {
				if (mapping.getTimeRange().openEnd()) openEnds.add(mapping);
				else if (mapping.getTimeRange().openBeginning()) openBeginnings.add(mapping);
				else result.add(mapping);
			}

			Collections.sort(openEnds, Collections.reverseOrder(new TimedComparator()));
			List<DeviceMapping<ItemT>> usedOpenEnds = new ArrayList<DeviceMapping<ItemT>>();
			List<DeviceMapping<ItemT>> usedOpenBeginnings = new ArrayList<DeviceMapping<ItemT>>();

			//now find closest matching
			for (DeviceMapping<ItemT> openBeginning : openBeginnings) {
				TimeRange openBeginningRange = openBeginning.getTimeRange();
				for (DeviceMapping<ItemT> openEnd : openEnds) {
					TimeRange openEndRange = openEnd.getTimeRange();
					if (openEndRange.from().before(openBeginningRange.to())) {
						usedOpenEnds.add(openEnd);
						usedOpenBeginnings.add(openBeginning);
						result.add(getMapping(device, item, openEndRange.intersection(openBeginningRange)));
						break;
					}
				}
			}
			
			openEnds.removeAll(usedOpenEnds);
			openBeginnings.removeAll(usedOpenBeginnings);
			result.addAll(openEnds);
			result.addAll(openBeginnings);
			
			overallResult.addAll(result);
		}
		
		Collections.sort(overallResult, RaceLogEventComparator.INSTANCE);
		return overallResult;
	}
}
