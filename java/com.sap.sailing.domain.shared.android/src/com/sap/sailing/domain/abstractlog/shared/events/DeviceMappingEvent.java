package com.sap.sailing.domain.abstractlog.shared.events;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogDefinedMarkFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

/**
 * Event type for mapping {@link DeviceIdentifier devices} to {@code items} ({@link Competitor competitors} or {@link Mark marks}).
 * <p>
 * Not both {@link #getFrom()} and {@link #getTo()} may be {@code null} at the same time (which would be a mapping for "all times",
 * as the semantics of somehow later limiting this range would become unclear
 * <p>
 * If one end of the time range enclosed by {@link #getFrom()} and {@link #getTo()} is open, this can be closed
 * by a {@link CloseOpenEndedDeviceMappingEvent}.
 * 
 * @see RaceLogDefinedMarkFinder for rules by which {@link DeviceMapping}s are derived from these events.
 * @author Fredrik Teschke
 */
public interface DeviceMappingEvent<VisitorT, ItemType extends WithID> extends AbstractLogEvent<VisitorT>, Revokable {
    ItemType getMappedTo();
    DeviceIdentifier getDevice();
    
    /**
     * The {@code return} value may be {@code null}, symbolizing an open range.
     * In that case the device should be mapped to the item for any timepoint before that specified by {@link #getTo()}.
     */
    TimePoint getFrom();
    
    /**
     * The {@code return} value may be {@code null}, symbolizing an open range.
     * In that case the device should be mapped to the item for any timepoint after that specified by {@link #getFrom()}.
     */
    TimePoint getTo();
}
