package com.sap.sse.security.shared;

import java.util.ArrayList;
import java.util.Collections;

public enum AbstractRoles implements AbstractRole {
    spectator("spectator", Collections.<String>emptyList()),
    moderator("moderator", asList("race:live_replay")),
    sailor("sailor", Collections.<String>emptyList()),
    coach("coach", Collections.<String>emptyList()),
    eventmanager("eventmanager",
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
    mediaeditor("mediaeditor", asList("media:*"));
    
    private final String rolename;
    private final Iterable<String> permissions;
    
    private AbstractRoles(String rolename, Iterable<String> permissions) {
        this.rolename = rolename;
        this.permissions = permissions;
    }
    
    public String getRolename() {
        return rolename;
    }

    @Override
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
