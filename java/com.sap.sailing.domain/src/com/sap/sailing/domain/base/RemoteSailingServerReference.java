package com.sap.sailing.domain.base;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Named;

/**
 * Represents a {@link URL}-based reference to a remote server. {@link List<UUID>} represents the list of event id's
 * which are selected for inclusion or exclusion. {@link Boolean include} flag determines whether selected events will
 * be included or excluded during loading process. If it's not present (equals to null) then all events will be loaded
 * from remote server instance.
 * 
 * @author Frank
 * 
 */
public interface RemoteSailingServerReference extends Named {
    URL getURL();

    Boolean getInclude();

    void setInclude(Boolean include);

    Set<UUID> getSelectedEventIds();

    void updateSelectedEventIds(Iterable<UUID> selectedEventIds);
}
