package com.sap.sailing.domain.base.impl;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;

public class RemoteSailingServerReferenceImpl extends NamedImpl implements RemoteSailingServerReference {
    private static final long serialVersionUID = 3561284977118738878L;

    /** the URL of the server */
    private final URL url;
    private final Set<UUID> excludedEventIds;
    
    public RemoteSailingServerReferenceImpl(String name, URL url, List<UUID> excludedEventIds) {
        super(name);
        this.url = url;
        // ensure the list is really immutable by copying it and handing out only unmodifiableList wrappers for it
        this.excludedEventIds = new HashSet<>(excludedEventIds);
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public Iterable<UUID> getExcludedEventIds() {
        return Collections.unmodifiableSet(excludedEventIds);
    }
    
    @Override
    public void addExcludedEventIds(Iterable<UUID> eventIdsToExclude) {
        Util.addAll(eventIdsToExclude, excludedEventIds);
    }
}
