package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogImpl;
import com.sap.sailing.domain.abstractlog.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.impl.LogEventComparator;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRevokeEventImpl;
import com.sap.sailing.domain.base.Mark;
import com.sap.sse.common.WithID;

public class RegattaLogImpl extends AbstractLogImpl<RegattaLogEvent, RegattaLogEventVisitor> implements RegattaLog {

    private static final long serialVersionUID = 98032278604708475L;

    public RegattaLogImpl(Serializable identifier) {
        super(identifier, new LogEventComparator());
    }

    public RegattaLogImpl(String nameForReadWriteLock, Serializable identifier) {
        super(nameForReadWriteLock, identifier, new LogEventComparator());
    }

    @Override
    protected RegattaLogEvent createRevokeEvent(AbstractLogEventAuthor author, RegattaLogEvent toRevoke, String reason) {
        return new RegattaLogRevokeEventImpl(author, toRevoke, reason);
    }
    
    @Override
    public void revokeDefineMarkEventAndRelatedDeviceMappings(final RegattaLogDefineMarkEvent event, final AbstractLogEventAuthor author, final Logger logger) {
        if (event != null){
            RegattaLogEvent revokeEvent = new RegattaLogRevokeEventImpl(author, event, "Revoked by AdminConsole (RaceLogTracking)");
            this.add(revokeEvent);
            
            final List<RegattaLogEvent> regattaLogDeviceMarkMappingEvents = new AllEventsOfTypeFinder<>(this, /* only unrevoked */ true, RegattaLogDeviceMappingEvent.class).analyze();
            
            for (RegattaLogEvent deviceMappingEvent : regattaLogDeviceMarkMappingEvents) {
                @SuppressWarnings("unchecked") // The list can only contain device mapping events
                WithID withID = ((RegattaLogDeviceMappingEvent<WithID>) deviceMappingEvent).getMappedTo();
                if (withID instanceof Mark && ((Mark) withID).getId().toString().equals(event.getMark().getId().toString())) {
                    RegattaLogEvent revokeDeviceMapping = new RegattaLogRevokeEventImpl(author, deviceMappingEvent, 
                            "Revoked because AdminConsole (RaceLogTracking) revoked mark");
                    this.add(revokeDeviceMapping);
                }
            }
        } else {
            logger.warning("Could not revoke event for mark. Mark not found in RegattaLog.");
        }
    }
}
