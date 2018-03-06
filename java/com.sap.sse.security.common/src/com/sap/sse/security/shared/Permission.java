package com.sap.sse.security.shared;

/**
 * Represents the "type" of object on which a permission can be granted. In a typical wildcard permission
 * of the form "A:B:C" this represents the first part. For example, if we may want to describe a permission
 * for updating a leaderboard then this may look like this: <code>LEADERBOARD:UPDATE:KW2017 Laser Int.</code> where
 * <code>LEADERBOARD</code> then is the {@link #name()} of this permission, {@link DefaultModes#UPDATE UPDATE} is
 * the operation mode, and <code>"KW2017 Laser Int."</code> is the object identifier that may not be unique outside
 * of the type qualifier represented by this permission ("LEADERBOARD" in the example).
 *
 * @author Axel Uhl (d043530)
 *
 */
public interface Permission {
    String name();

    int ordinal();

    /**
     * If one or more modes are specified, a string permission is rendered that has the
     * {@link Mode#getStringPermission() permission strings} of those modes listed in the second wildcard permission
     * component. Otherwise, only the primary permission (representing the object type) with one segment is returned.
     */
    String getStringPermission(Mode... modes);

    /**
     * Same as {@link #getStringPermission(Mode...)}, only that the result is a {@link WildcardPermission} instead of a
     * {@link String}
     */
    WildcardPermission getPermission(Mode... modes);

    /**
     * Produces a string permission for this permission, the <code>mode</code> specified as the second wildcard
     * permission segment, and the <code>objectIdentifier</code> as the third wildcard permission segment.
     */
    String getStringPermissionForObjects(Mode mode, String... objectIdentifiers);

    /**
     * Same as {@link #getStringPermissionForObjects(Mode, String...)}, only that the result is a
     * {@link WildcardPermission} instead of a {@link String}
     */
    WildcardPermission getPermissionForObjects(Mode mode, String... objectIdentifiers);

    public static interface Mode {
        String name();

        int ordinal();

        String getStringPermission();
    }

    /**
     * A mode of operation on a resource; the typical "CRUD" operations.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    public enum DefaultModes implements Mode {
        CREATE, READ, UPDATE, DELETE;

        @Override
        public String getStringPermission() {
            return name();
        }
    }

}
