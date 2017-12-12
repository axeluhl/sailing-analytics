package com.sap.sse.security.shared;

public enum DefaultPermissions implements Permission {
    ALL_EVENTS("event:*");
    
    private DefaultPermissions(String stringPermission) {
        this.stringPermission = stringPermission;
    }
    
    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    public String getStringPermission(Mode... modes) {
        final String result;
        if (modes==null || modes.length==0) {
            result = stringPermission;
        } else {
            final StringBuilder modesString = new StringBuilder();
            boolean first = true;
            for (Mode mode : modes) {
                if (first) {
                    first = false;
                } else {
                    modesString.append(',');
                }
                modesString.append(mode.getStringPermission());
            }
            result = stringPermission+":"+modesString.toString();
        }
        return result;
    }
    
    @Override
    public WildcardPermission getPermission(Mode... modes) {
        return new WildcardPermission(getStringPermission(modes));
    }

    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    public String getStringPermissionForObjects(Mode mode, String... objectIdentifiers) {
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
    
    @Override
    public WildcardPermission getPermissionForObjects(Mode mode, String... objectIdentifiers) {
        return new WildcardPermission(getStringPermissionForObjects(mode, objectIdentifiers));
    }

    private final String stringPermission;
}

