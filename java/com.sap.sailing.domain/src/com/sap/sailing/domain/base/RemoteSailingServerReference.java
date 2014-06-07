package com.sap.sailing.domain.base;

import java.net.URL;

import com.sap.sse.common.Named;

/**
 * Represents a {@link URL}-based reference to a remote server.
 * 
 * @author Frank
 * 
 */
public interface RemoteSailingServerReference extends Named {
    URL getURL();
}
