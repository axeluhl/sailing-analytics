package com.sap.sailing.domain.swisstimingadapter;

/**
 * Configuration parameters that can be used to connect to a TracTrac event / race.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SwissTimingConfiguration {
    String getName();
    
    String getHostname();
    
    int getPort();
}
