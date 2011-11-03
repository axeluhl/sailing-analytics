package com.sap.sailing.domain.swisstimingadapter;

/**
 * A message receiver waiting for a response of a certain type
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Receiver {
    MessageType getExpectedResponseType();
}
