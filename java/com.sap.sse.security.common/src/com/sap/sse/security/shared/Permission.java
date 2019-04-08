package com.sap.sse.security.shared;

public interface Permission {
    String name();
    
    int ordinal();
    
    /**
     * If one or more modes are specified, a string permission is rendered that has the
     * {@link Mode#getStringPermission() permission strings} of those modes listed in the second wildcard permission
     * component. Otherwise, only the primary permission with one segment is returned.
     */
    String getStringPermission(Mode... modes);
    
    /**
     * Produces a string permission for this permission, the <code>mode</code> specified as the second wildcard permission
     * segment, and the <code>objectIdentifier</code> as the third wildcard permission segment.
     */
    String getStringPermissionForObjects(Mode mode, String... objectIdentifiers);
    
    public static interface Mode {
        String name();
        
        int ordinal();
        
        String getStringPermission();
    }
    
    public enum DefaultModes implements Mode {
        CREATE, READ, UPDATE, DELETE;

        @Override
        public String getStringPermission() {
            return name();
        }
    }

}
