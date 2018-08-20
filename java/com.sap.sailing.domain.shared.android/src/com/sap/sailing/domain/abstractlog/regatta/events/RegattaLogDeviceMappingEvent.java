package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.regatta.MappingEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDefinedMarkAnalyzer;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.WithID;

/**
 * Event type for mapping {@link DeviceIdentifier devices} to {@code items} ({@link Competitor competitors} or {@link Mark marks}).
 * <p>
 * Not both {@link #getFrom()} and {@link #getToInclusive()} may be {@code null} at the same time (which would be a mapping for "all times",
 * as the semantics of somehow later limiting this range would become unclear
 * <p>
 * If one end of the time range enclosed by {@link #getFrom()} and {@link #getToInclusive()} is open, this can be closed
 * by a {@link RegattaLogCloseOpenEndedDeviceMappingEvent}.
 * 
 * @see RegattaLogDefinedMarkAnalyzer for rules by which {@link DeviceMapping}s are derived from these events.
 * @author Fredrik Teschke
 */
public interface RegattaLogDeviceMappingEvent<ItemType extends WithID> extends RegattaLogEvent, Revokable {
    ItemType getMappedTo();
    DeviceIdentifier getDevice();
    
    /**
     * The {@code return} value may be {@code null}, symbolizing an open range.
     * In that case the device should be mapped to the item for any timepoint before that specified by {@link #getToInclusive()}.
     */
    TimePoint getFrom();
    
    /**
     * The {@code return} value may be {@code null}, symbolizing an open range.
     * In that case the device should be mapped to the item for any timepoint after that specified by {@link #getFrom()}.
     * Otherwise, this marks the <em>inclusive</em> end of the interval, other than the {@link TimeRange#to()} semantics which
     * uses an <em>exclusive</em> interval end. To bridge this gap, whenever mapping this event's from/to interval to a
     * {@link TimeRange}, at least the resolution of the {@link TimePoint} representation must be added to the result of
     * this method to make it into a valid end of a {@link TimeRange} that includes the time interval covered by this mapping.
     */
    TimePoint getToInclusive();

    /**
     * Visitor patter implementation for event mapping handling differentiation.
     * 
     * @param visitor
     */
    void accept(MappingEventVisitor visitor);
}
