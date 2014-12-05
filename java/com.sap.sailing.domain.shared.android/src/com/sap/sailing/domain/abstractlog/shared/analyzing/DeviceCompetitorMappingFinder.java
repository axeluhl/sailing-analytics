package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.LogAnalyzer;
import com.sap.sailing.domain.abstractlog.MultiLogAnalyzer.AnalyzerFactory;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;

public class DeviceCompetitorMappingFinder<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends DeviceMappingFinder<LogT, EventT, VisitorT, Competitor> {

    public static class Factory implements AnalyzerFactory<Map<Competitor, List<DeviceMapping<Competitor>>>> {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public LogAnalyzer<Map<Competitor, List<DeviceMapping<Competitor>>>> createAnalyzer(AbstractLog<?, ?> log) {
            return new DeviceCompetitorMappingFinder(log);
        }
        
    }
    
    public DeviceCompetitorMappingFinder(LogT log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(DeviceMappingEvent<?, ?> mapping) {
        return mapping instanceof DeviceCompetitorMappingEvent;
    }
}
