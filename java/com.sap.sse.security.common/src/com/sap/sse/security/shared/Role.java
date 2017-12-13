package com.sap.sse.security.shared;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.Renamable;

/**
 * Equality ({@link #equals(Object)} and {@link #hashCode()} are expected to be based solely on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Role extends NamedWithID, Renamable {
    Set<WildcardPermission> getPermissions();
    
    @Override
    UUID getId();

    void setPermissions(Iterable<WildcardPermission> permissions);
}
