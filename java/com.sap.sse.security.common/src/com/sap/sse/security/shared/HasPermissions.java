package com.sap.sse.security.shared;

import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

/**
 * Represents the "type" of object on which a permission can be granted. In a typical wildcard permission
 * of the form "A:B:C" this represents the first part. For example, if we may want to describe a permission
 * for updating a leaderboard then this may look like this: <code>LEADERBOARD:UPDATE:KW2017 Laser Int.</code> where
 * <code>LEADERBOARD</code> then is the {@link #name()} of this permission, {@link DefaultActions#UPDATE UPDATE} is
 * the operation mode, and <code>"KW2017 Laser Int."</code> is the object identifier that may not be unique outside
 * of the type qualifier represented by this permission ("LEADERBOARD" in the example).
 *
 * @author Axel Uhl (d043530)
 *
 */
public interface HasPermissions {
    String getName();

    /**
     * @return the actions for which permissions can be defined on the logical, securable type represented by this
     *         object. Only those actions need to be accepted by methods such as {@link #getStringPermission(Action...)},
     *         and it is acceptable for those methods to throw an {@link IllegalArgumentException} if an action is
     *         passed that is not contained in the array returned by this method.
     */
    Action[] getAvailableActions();
    
    IdentifierStrategy identifierStrategy();

    /**
     * @return {@code true} if and only if objects of this logical type support the {@code action} as one of their
     *         {@link #getAvailableActions() available actions}
     */
    boolean supports(Action action);

    /**
     * If one or more modes are specified, a string permission is rendered that has the
     * {@link Action#getStringPermission() permission strings} of those modes listed in the second wildcard permission
     * component. Otherwise, only the primary permission (representing the object type) with one segment is returned.
     */
    String getStringPermission(Action... actions);

    /**
     * Same as {@link #getStringPermission(Action...)}, only that the result is a {@link WildcardPermission} instead of a
     * {@link String}
     */
    WildcardPermission getPermission(Action... actions);

    /**
     * Produces a string permission for this permission, the <code>mode</code> specified as the second wildcard
     * permission segment, and the <code>objectIdentifier</code> as the third wildcard permission segment. The object
     * identifiers must be unique within the scope defined by this {@link HasPermissions} which represents an object
     * category or type, such as, e.g., "LEADERBOARD."
     * 
     * @param typeRelativeObjectIdentifier
     *            can be any string; this method will take care of encoding the identifiers such that they are legal in
     *            the context of a permission part; see also {@link PermissionStringEncoder}
     */
    String getStringPermissionForObjects(Action action, String... typeRelativeObjectIdentifier);
    String getStringPermissionForObject(Action action, Object object);

    /**
     * Qualifies the {@code objectIdentifier} which only has to be unique within the scope of the type identified
     * by this permission with this permission's type name. For example, if this permission is for the "LEADERBOARD"
     * type, and the {@code objectIdentifier} is {@code "abc"} then the resulting qualified identifier will be
     * "LEADERBOARD/abc". This assumes that the {@link #name()} method returns only values that do not contain a "/".
     */
    QualifiedObjectIdentifier getQualifiedObjectIdentifier(String typeRelativeObjectIdentifier);

    /**
     * Same as {@link #getStringPermissionForObjects(Action, String...)}, only that the result is a
     * {@link WildcardPermission} instead of a {@link String}
     * @deprecated  better option is to use the variant {@link #getPermissionForObject(Action action, Object object)} instead.
     * {@link #getPermissionForObject(Action action, Object object)} uses this implementation in a private manner but is
     * creating the permission id by using the corresponding {{@link #identifierStrategy()}.
     * If no concrete object (e.g. unit test) is available, this method can still be used.
     */
    @Deprecated
    WildcardPermission getPermissionForObjects(Action action, String... objectIdentifiers);
    WildcardPermission getPermissionForObject(Action action, Object object);

    /**
     * Same as {@link #getPermissionForObjects(Action, String...)}, only that this method gets the
     * {@link WildcardPermission}s for all given actions
     */
    WildcardPermission[] getPermissionsForObjects(final Action[] actions, final String... objectIdentifiers);

    public static interface Action {
        /**
         * Returns the action as represented in the second part of a {@link WildcardPermission}. This shall be a string
         * that will not need further encoding such as by a {@link WildcardPermissionEncoder} so that it can be used
         * in a {@link WildcardPermission} immediately and without change.
         */
        String name();
    }

    /**
     * A mode of operation on a resource; the typical "CRUD" operations.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    public enum DefaultActions implements Action {
        CREATE, READ, UPDATE, DELETE, CHANGE_OWNERSHIP, CHANGE_ACL;
        
        public static final Action[] MUTATION_ACTIONS = new Action[] { CREATE, UPDATE, DELETE, CHANGE_OWNERSHIP,
                CHANGE_ACL };
        
        public static final Action[] READ_AND_WRITE_ACTIONS = new Action[] { CREATE, READ, UPDATE, DELETE };
        /**
         * Returns all {@link DefaultActions} plus the {@code actions} passed, combined in one new array
         */
        public static Action[] plus(Action... actions) {
            final Action[] result = new Action[values().length+actions.length];
            System.arraycopy(values(), 0, result, 0, values().length);
            System.arraycopy(actions, 0, result, values().length, actions.length);
            return result;
        }
    }

}
