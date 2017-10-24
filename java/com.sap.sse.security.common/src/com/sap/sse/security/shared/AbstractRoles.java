package com.sap.sse.security.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public enum AbstractRoles {
    spectator(UUID.fromString("244cb84c-2b8a-4557-b175-db963072cfbc"), "spectator", Collections.<String>emptyList()),
    moderator(UUID.fromString("91c88a11-c977-4c24-b0a6-15bac0525764"), "moderator", asList("race:live_replay")),
    sailor(UUID.fromString("eb774a5c-9350-4094-97d5-6b15ab49feac"), "sailor", Collections.<String>emptyList()),
    coach(UUID.fromString("8e72e67d-cc65-4de4-a985-03a7a3c48e93"), "coach", Collections.<String>emptyList()),
    eventmanager(UUID.fromString("7920a761-1378-4c98-9129-0fbe586c5cd7"), "eventmanager",
            asList(
                "race_board:*",
                
                /* AdminConsole:
                Permission.MANAGE_ALL_COMPETITORS,
                Permission.MANAGE_COURSE_LAYOUT,
                Permission.MANAGE_DEVICE_CONFIGURATION,
                Permission.MANAGE_EVENTS,
                Permission.MANAGE_IGTIMI_ACCOUNTS,
                Permission.MANAGE_LEADERBOARD_GROUPS,
                Permission.MANAGE_LEADERBOARDS,
                Permission.MANAGE_LEADERBOARD_RESULTS,
                Permission.MANAGE_MEDIA,
                Permission.MANAGE_RACELOG_TRACKING,
                Permission.MANAGE_REGATTAS,
                Permission.MANAGE_RESULT_IMPORT_URLS,
                Permission.MANAGE_STRUCTURE_IMPORT_URLS,
                Permission.MANAGE_TRACKED_RACES,
                Permission.MANAGE_WIND,*/
                
                // back-end:
                "event:*",
                "regatta:*",
                "leaderboard:*",
                "leaderboard_group:*")),
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
