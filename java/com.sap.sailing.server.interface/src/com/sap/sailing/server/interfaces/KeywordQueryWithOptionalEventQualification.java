package com.sap.sailing.server.interfaces;

import java.util.HashSet;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.common.search.KeywordQuery;

/**
 * A specialization of a generic {@link KeywordQuery} which can provide a set of event identifiers to exclude or include
 * in the keyword search.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class KeywordQueryWithOptionalEventQualification extends KeywordQuery {
    private static final long serialVersionUID = -6009690613242058561L;
    private final boolean include;
    private final Iterable<UUID> eventUUIDs;
    
    public KeywordQueryWithOptionalEventQualification(Iterable<String> keywords) {
        this(Util.toArray(keywords, new String[Util.size(keywords)]));
    }
    
    public KeywordQueryWithOptionalEventQualification(String... keywords) {
        this(/* include */ false, /* nothing to exclude; search all */ null, keywords);
    }

    public KeywordQueryWithOptionalEventQualification(Iterable<String> keywords, boolean include, Iterable<UUID> eventUUIDs) {
        this(include, eventUUIDs, Util.toArray(keywords, new String[Util.size(keywords)]));
    }
    
    public KeywordQueryWithOptionalEventQualification(boolean include, Iterable<UUID> eventUUIDs, String... keywords) {
        super(keywords);
        this.include = include;
        final HashSet<UUID> eventUUIDsAsSet = new HashSet<>();
        this.eventUUIDs = eventUUIDsAsSet;
        if (eventUUIDs != null) {
            Util.addAll(eventUUIDs, eventUUIDsAsSet);
        }
    }

    public boolean isInclude() {
        return include;
    }
    
    public Iterable<UUID> getEventUUIDs() {
        return eventUUIDs;
    }
}
