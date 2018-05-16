package com.sap.sse.security.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

// TODO shouldn't these be part of the Sailing domain and not of SSE?
public enum AbstractRoles {
    spectator(UUID.fromString("244cb84c-2b8a-4557-b175-db963072cfbc"), "spectator", Collections.<String>emptyList()),
    moderator(UUID.fromString("91c88a11-c977-4c24-b0a6-15bac0525764"), "moderator", asList("race:live_replay")),
    mediaeditor(UUID.fromString("2e22583b-c9c1-4ee4-8231-e8e320a8a411"), "mediaeditor", asList("media:*"));
    
    private final UUID id;
    private final String rolename;
    private final Iterable<String> permissions;
    
    private AbstractRoles(UUID id, String rolename, Iterable<String> permissions) {
        this.id = id;
        this.rolename = rolename;
        this.permissions = permissions;
    }
    
    public String getDisplayName() {
        return rolename;
    }
    
    public UUID getId() {
        return id;
    }

    public Iterable<String> getPermissions() {
        return permissions;
    }
    
    private static <T> Iterable<T> asList(@SuppressWarnings("unchecked") T... elements) {
        ArrayList<T> list = new ArrayList<T>(elements.length);
        for (T element : elements) {
            list.add(element);
        }
        return list;
    }
}
