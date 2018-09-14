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
    /**
     * The separator character used to separate the permission type {@link #name()} from the object identifier
     * when providing a {@link #getQualifiedObjectIdentifier(String) qualified object identifier}.
     */
    char QUALIFIER_SEPARATOR = '/';
    
    String name();

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
     * permission segment, and the <code>objectIdentifier</code> as the third wildcard permission segment. The object
     * identifiers must be unique within the scope defined by this {@link Permission} which represents an object
     * category or type, such as, e.g., "LEADERBOARD."
     * 
     * @param objectIdentifiers
     *            can be any string; this method will take care of encoding the identifiers such that they are legal in
     *            the context of a permission part; see also {@link PermissionStringEncoder}
     */
    String getStringPermissionForObjects(Mode mode, String... objectIdentifiers);
    
    /**
     * Qualifies the {@code objectIdentifier} which only has to be unique within the scope of the type identified
     * by this permission with this permission's type name. For example, if this permission is for the "LEADERBOARD"
     * type, and the {@code objectIdentifier} is {@code "abc"} then the resulting qualified identifier will be
     * "LEADERBOARD/abc". This assumes that the {@link #name()} method returns only values that do not contain a "/".
     */
    String getQualifiedObjectIdentifier(String objectIdentifier);

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
