package com.sap.sse.security.shared;

import java.util.UUID;

import com.sap.sse.common.NamedWithID;

/**
 * A group of users; equality and hash code are based solely on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface UserGroup extends NamedWithID, WithQualifiedObjectIdentifier {
    public Iterable<SecurityUser> getUsers();
    
    public void add(SecurityUser user);
    public void remove(SecurityUser user);
    public boolean contains(SecurityUser user);

    @Override
    UUID getId();
}
