package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.LogAnalyzer;
import com.sap.sailing.domain.abstractlog.MultiLogAnalyzer.AnalyzerFactory;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMarkMappingEvent;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;

public class DeviceMarkMappingFinder<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends DeviceMappingFinder<LogT, EventT, VisitorT, Mark> {
    
    public static class Factory implements AnalyzerFactory<Map<Mark, List<DeviceMapping<Mark>>>> {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public LogAnalyzer<Map<Mark, List<DeviceMapping<Mark>>>> createAnalyzer(AbstractLog<?, ?> log) {
            return new DeviceMarkMappingFinder(log);
        }

    }

    public DeviceMarkMappingFinder(LogT log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(DeviceMappingEvent<?, ?> mapping) {
        return mapping instanceof DeviceMarkMappingEvent;
    }
}
