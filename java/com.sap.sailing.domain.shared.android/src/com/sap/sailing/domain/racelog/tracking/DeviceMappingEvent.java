package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;

/**
 * Event type for mapping {@link DeviceIdentifier devices} to {@code items} ({@link Competitor competitors} or {@link Mark marks}).
 * <p>
 * Not both {@link #getFrom()} and {@link #getTo()} may be {@code null} at the same time (which would be a mapping for "all times",
 * as the semantics of somehow later limiting this range would become unclear, whereas for a time range only open to one end the
 * semantics are explained below.
 * <p>
 * If one end of the time range enclosed by {@link #getFrom()} and {@link #getTo()} is open, this can be closed
 * by another {@link DeviceMappingEvent} in the same {@link RaceLog} with equal {@code item} and {@code device},
 * that is open to the other side and lies in the open direction of this event.
 * E.g. the following two {@link DeviceMappingEvent}s, that are both open to one side, together form one closed range
 * (|---- shall denote a time range that is open in the direction of increasing time values):
 * <ul>
 * <li>1:   |------ </li>
 * <li>2:        ------| </li>
 * <li>1+2: |----------| </li>
 * </ul>
 * 
 * Or for the case of multiple such events:
 * <ul>
 * <li>1:           |------ </li>
 * <li>2:                 ------| </li>
 * <li>3:               |------- </li>
 * <li>4:                                |------ </li>
 * <li>5:                                     -----| </li>
 * <li>1+2+3+4+5:   |-----------|        |---------| </li>
 * </ul>
 * @author Fredrik Teschke
 */
public interface DeviceMappingEvent<ItemType extends WithID> extends RaceLogEvent, Revokable {
    ItemType getMappedTo();
    DeviceIdentifier getDevice();
    
    /**
     * The {@code return} type may be {@code null}, symbolizing an open range.
     * In that case the device should be mapped to the item for any timepoint before that specified by {@link #getTo()}.
     */
    TimePoint getFrom();
    
    /**
     * The {@code return} type may be {@code null}, symbolizing an open range.
     * In that case the device should be mapped to the item for any timepoint after that specified by {@link #getFrom()}.
     */
    TimePoint getTo();
}
