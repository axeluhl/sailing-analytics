package com.sap.sailing.domain.base.impl;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;

public class RemoteSailingServerReferenceImpl extends NamedImpl implements RemoteSailingServerReference {
    private static final long serialVersionUID = 3561284977118738878L;

    /** the URL of the server */
    private final URL url;
    private boolean include;
    private final Set<UUID> selectedEventIds;

    public RemoteSailingServerReferenceImpl(String name, URL url, boolean include, Iterable<UUID> selectedEventIds) {
        super(name);
        this.url = url;
        this.include = include;
        // ensure the set is a copy we can modify in the updateSelectedEventIds(...) method but which we present
        // as immutable by wrapping with an unmodifiableSet in getSelectedEventIds().
        this.selectedEventIds = new HashSet<>();
        Util.addAll(selectedEventIds, this.selectedEventIds);
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public boolean isInclude() {
        return include;
    }

    @Override
    public void setInclude(boolean include) {
        this.include = include;
    }

    @Override
    public Set<UUID> getSelectedEventIds() {
        return selectedEventIds != null ? Collections.unmodifiableSet(selectedEventIds) : Collections.emptySet();
    }

    @Override
    public void updateSelectedEventIds(Iterable<UUID> selectedEventIds) {
        this.selectedEventIds.clear();
        Util.addAll(selectedEventIds, this.selectedEventIds);
    }
}
