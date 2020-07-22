package com.sap.sailing.domain.base;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Named;

/**
 * Represents a {@link URL}-based reference to a remote server. {@link List<UUID>} represents the list of event IDs
 * which are selected for inclusion or exclusion. The {@link #isInclude() include} flag determines whether
 * {@link #getSelectedEventIds() selected events} will be included or excluded during loading process.
 * 
 * @author Frank
 * 
 */
public interface RemoteSailingServerReference extends Named {
    URL getURL();

    boolean isInclude();

    void setInclude(boolean include);

    /**
     * @return never {@code null}, always a valid, immutable and possibly empty set
     */
    Set<UUID> getSelectedEventIds();

    void updateSelectedEventIds(Iterable<UUID> selectedEventIds);
}
