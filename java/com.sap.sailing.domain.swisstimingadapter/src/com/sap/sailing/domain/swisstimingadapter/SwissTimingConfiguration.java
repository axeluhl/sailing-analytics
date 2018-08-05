package com.sap.sailing.domain.swisstimingadapter;

/**
 * Configuration parameters that can be used to connect to a SwissTiming event.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SwissTimingConfiguration {
    String getName();
    
    String getJsonURL();
    
    String getHostname();
    
    Integer getPort();

    String getUpdateURL();

    String getUpdateUsername();

    String getUpdatePassword();
}
