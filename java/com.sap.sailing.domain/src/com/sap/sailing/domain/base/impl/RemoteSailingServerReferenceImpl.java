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
    private Boolean include;
    private final Set<UUID> selectedEventIds;
    
    public RemoteSailingServerReferenceImpl(String name, URL url, Boolean include, List<UUID> selectedEventIds) {
        super(name);
        this.url = url;
        this.include = include;
        // ensure the list is really immutable by copying it and handing out only unmodifiableList wrappers for it
        this.selectedEventIds = new HashSet<>(selectedEventIds);
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public Boolean getInclude() {
        return include;
    }

    @Override
    public void setInclude(Boolean include) {
        this.include = include;
    }

    @Override
    public Set<UUID> getSelectedEventIds() {
        return Collections.unmodifiableSet(selectedEventIds);
    }

    @Override
    public void updateSelectedEventIds(Iterable<UUID> selectedEventIds) {
        this.selectedEventIds.clear();
        Util.addAll(selectedEventIds, this.selectedEventIds);
    }
}
