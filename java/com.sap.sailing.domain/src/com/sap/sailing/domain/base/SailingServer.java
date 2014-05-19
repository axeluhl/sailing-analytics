package com.sap.sailing.domain.base;

import java.net.URL;

import com.sap.sailing.domain.common.Named;

/**
 * A SailingServer represents a runtime instance of our server.
 * 
 * @author Frank
 * 
 */
public interface SailingServer extends Named {
    URL getURL();
}
