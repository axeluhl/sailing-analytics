package com.sap.sailing.domain.common.security;




public enum Permission implements com.sap.sse.security.shared.Permission {
    // AdminConsole permissions
    MANAGE_EVENTS,
    MANAGE_PAIRING_LISTS,
    MANAGE_REGATTAS,
    MANAGE_TRACKED_RACES,
    SHOW_TRACKED_RACES,
    MANAGE_RACELOG_TRACKING,
    MANAGE_ALL_COMPETITORS,
    MANAGE_ALL_BOATS,
    MANAGE_COURSE_LAYOUT,
    MANAGE_WIND,
    MANAGE_IGTIMI_ACCOUNTS,
    MANAGE_EXPEDITION_DEVICE_CONFIGURATIONS,
    MANAGE_LEADERBOARDS,
    MANAGE_LEADERBOARD_RESULTS,
    MANAGE_LEADERBOARD_GROUPS,
    MANAGE_RESULT_IMPORT_URLS,
    MANAGE_STRUCTURE_IMPORT_URLS,
    MANAGE_MEDIA,
    MANAGE_SAILING_SERVER_INSTANCES,
    MANAGE_LOCAL_SERVER_INSTANCE,
    MANAGE_REPLICATION,
    MANAGE_MASTERDATA_IMPORT,
    MANAGE_DEVICE_CONFIGURATION,
    MANAGE_USERS,
    MANAGE_FILE_STORAGE,
    MANAGE_MARK_PASSINGS,
    MANAGE_MARK_POSITIONS,
    CAN_REPLAY_DURING_LIVE_RACES,
    DETAIL_TIMER,
    
    // back-end permissions
    EVENT,
    REGATTA,
    LEADERBOARD,
    LEADERBOARD_GROUP,
    TRACKED_RACE,
    DATA_MINING,
    ;

    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    @Override
    public String getStringPermission(com.sap.sse.security.shared.Permission.Mode... modes) {
        final String result;
        if (modes==null || modes.length==0) {
            result = name();
        } else {
            final StringBuilder modesString = new StringBuilder();
            boolean first = true;
            for (com.sap.sse.security.shared.Permission.Mode mode : modes) {
                if (first) {
                    first = false;
                } else {
                    modesString.append(',');
                }
                modesString.append(mode.getStringPermission());
            }
            result = name()+":"+modesString.toString();
        }
        return result;
    }

    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    @Override
    public String getStringPermissionForObjects(com.sap.sse.security.shared.Permission.Mode mode, String... objectIdentifiers) {
        final StringBuilder result = new StringBuilder(getStringPermission(mode));
        if (objectIdentifiers!=null && objectIdentifiers.length>0) {
            result.append(':');
            boolean first = true;
            for (String objectIdentifier : objectIdentifiers) {
                if (first) {
                    first = false;
                } else {
                    result.append(',');
                }
                result.append(objectIdentifier);
            }
        }
        return result.toString();
    }
    
    /**
     * The mode of interaction with a resource; used as the second element of a wildcard permission
     * 
     * @author Axel Uhl (d043530)
     *
     */
    public static enum Mode implements com.sap.sse.security.shared.Permission.Mode {
        CREATE, READ, UPDATE, DELETE;

        @Override
        public String getStringPermission() {
            return name();
        }
    }
}
