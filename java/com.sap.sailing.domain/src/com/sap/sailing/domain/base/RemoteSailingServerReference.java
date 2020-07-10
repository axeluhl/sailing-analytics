package com.sap.sailing.domain.base;

import java.net.URL;
import java.util.UUID;

import com.sap.sse.common.Named;

/**
 * Represents a {@link URL}-based reference to a remote server. {@link List<UUID>} represents the list of event id's to
 * exclude selected events from the loading process
 * 
 * @author Frank
 * 
 */
public interface RemoteSailingServerReference extends Named {
    URL getURL();
    Iterable<UUID> getExcludedEventIds();
    void addExcludedEventIds(Iterable<UUID> eventIdsToExclude);
}
