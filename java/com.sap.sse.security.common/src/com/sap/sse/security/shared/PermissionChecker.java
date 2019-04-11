package com.sap.sse.security.shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.Role;

/**
 * The {@link PermissionChecker} is an implementation of the permission checking algorithm also described in <a href=
 * "https://wiki.sapsailing.com/wiki/info/security/permission-concept#algorithm-boolean-ispermitted-principalcollection-principals-wildcardpermission-permission-for-composite-realm">Permission
 * Concept Wiki article</a>. It checks permissions in four stages where earlier stages overwrite later.
 * 
 * 1. Firstly, the data object's ACL is checked if it grants or explicitly revokes the permission. 2. Thereafter, the
 * permissions directly assigned to the user are checked. 3. Finally the permissions implied by roles the user has are
 * checked. This includes roles qualified by the group/user ownerships of the objetcs to which they are applied.
 * 
 * @author Jonas Dann, Axel Uhl (d043530)
 */
public class PermissionChecker {
    public enum PermissionState {
        GRANTED,
        REVOKED,
        NONE
    }
    
    // TODO use BiFunction and Method reference when we can use Java 8
    private interface WildcardPermissionChecker {
        boolean check(WildcardPermission grantedPermission, WildcardPermission permissionToCheck);
    }
    
    private static final WildcardPermissionChecker impliesChecker = new WildcardPermissionChecker() {
        
        @Override
        public boolean check(WildcardPermission grantedPermission, WildcardPermission permissionToCheck) {
            return grantedPermission.implies(permissionToCheck);
        }
    };
    
    private static final WildcardPermissionChecker impliesAnyChecker = new WildcardPermissionChecker() {
        
        @Override
        public boolean check(WildcardPermission grantedPermission, WildcardPermission permissionToCheck) {
            return grantedPermission.impliesAny(permissionToCheck);
        }
    };
    
    private static <U extends SecurityUser<?, ?, G>, G extends SecurityUserGroup<?>> Iterable<G> getGroupsOfUser(U user) {
        return (Iterable<G>) (user == null ? Collections.<G>emptySet() : user.getUserGroups());
    }
    
    /**
     * @param permission
     *            Permission of the form "data_object_type:action:instance_id". The instance id can be omitted when a
     *            general permission for the data object type is asked after (e.g. "event:create"). If the action
     *            contains more than one sub-part (divided by {@link WildcardPermission#SUBPART_DIVIDER_TOKEN}) then
     *            this is considered an error, and an {@link IllegalArgumentException} will be thrown.
     * @param ownership
     *            may be {@code null}, causing user- or tenant-parameterized roles and no user ownership override to be
     *            applied
     * @param acl
     *            may be {@code null} in which case no ACL-specific checks are performed
     */
    public static <RD extends RoleDefinition, R extends AbstractRole<RD, G, UR>, O extends AbstractOwnership<G, UR>, UR extends UserReference, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean isPermitted(
            WildcardPermission permission, U user, U allUser,
            O ownership, A acl) {
        return isPermitted(permission, user, getGroupsOfUser(user), allUser, getGroupsOfUser(allUser), ownership, acl);
    }

    /**
     * @param permission
     *            Permission of the form "data_object_type:action:instance_id". The instance id can be omitted when a
     *            general permission for the data object type is asked after (e.g. "event:create"). If the action
     *            contains more than one sub-part (divided by {@link WildcardPermission#SUBPART_DIVIDER_TOKEN}) then
     *            this is considered an error, and an {@link IllegalArgumentException} will be thrown.
     * @param ownership
     *            may be {@code null}, causing user- or tenant-parameterized roles and no user ownership override to be
     *            applied
     * @param acl
     *            may be {@code null} in which case no ACL-specific checks are performed
     */
    public static <RD extends RoleDefinition, R extends AbstractRole<RD, G, UR>, O extends AbstractOwnership<G, UR>, UR extends UserReference, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean isPermitted(
            WildcardPermission permission, U user,
            Iterable<G> groupsOfWhichUserIsMember, U allUser,
            Iterable<G> groupsOfWhichAllUserIsMember, O ownership, A acl) {
        List<Set<String>> parts = permission.getParts();
        // permission has at least data object type and action as parts
        // and data object part only has one sub-part
        if (parts.get(0).size() != 1) {
            throw new WrongPermissionFormatException(permission);
        }
        PermissionState result = PermissionState.NONE;
        
        // 1. check ACL
        if (acl != null) {
            // if no specific action is requested then this translates to a request for all permissions ("*")
            final String action;
            if (parts.size() < 2) {
                action = WildcardPermission.WILDCARD_TOKEN;
            } else {
                if (parts.get(1).size() > 1) {
                    throw new IllegalArgumentException("Permission to check must not have more than one sub-part: "+parts.get(1));
                }
                action = (String) parts.get(1).toArray()[0];
            }
            Set<G> allGroups = new HashSet<>();
            Util.addAll(groupsOfWhichUserIsMember, allGroups);
            Util.addAll(groupsOfWhichAllUserIsMember, allGroups);
            result = acl.hasPermission(action, allGroups);
        }

        // anonymous can only grant it if not already decided by acl
        if (result == PermissionState.NONE) {
            PermissionState anonymous = checkUserPermissions(permission, allUser, groupsOfWhichAllUserIsMember,
                    ownership, impliesChecker, /* matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven */ true);
            if (anonymous == PermissionState.GRANTED) {
                result = anonymous;
            }
        }
        if (result == PermissionState.NONE) {
            result = checkUserPermissions(permission, user, groupsOfWhichUserIsMember, ownership, impliesChecker,
                    /* matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven */ true);
        }
        return result == PermissionState.GRANTED;
    }

    /**
     * Checks if a user has a specific role either for a given ownership or globally if no ownership exists.
     */
    public static <RD extends RoleDefinition, R extends AbstractRole<RD, G, U>, O extends AbstractOwnership<G, U>, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean ownsUserASpecificRole(
            U user, U allUser, O ownership,
            String requiredRoleName) {
        assert requiredRoleName != null;
        return ownsUserASpecificRole(user, ownership, requiredRoleName)
                || ownsUserASpecificRole(allUser, ownership, requiredRoleName);

    }

    private static <RD extends RoleDefinition, R extends AbstractRole<RD, G, U>, O extends AbstractOwnership<G, U>, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean ownsUserASpecificRole(
            U user, O ownership, String requiredRoleName) {
        if (user == null) {
            return false;
        }
        for (R roleOwnedByUser : user.getRoles()) {
            if (roleOwnedByUser.getName().equals(requiredRoleName)) {
                if (roleOwnedByUser.getQualifiedForTenant() == null && roleOwnedByUser.getQualifiedForUser() == null) {
                    // the role is not qualified by a user or group which means it counts for any ownership
                    return true;
                }
                if (ownership == null || (ownership.getTenantOwner() == null && ownership.getUserOwner() == null)) {
                    // the role is qualified but there is now ownership which means the user needs the unqualified
                    // version of the role
                    continue;
                }
                if (roleOwnedByUser.getQualifiedForTenant() != null
                        && roleOwnedByUser.getQualifiedForTenant().equals(ownership.getTenantOwner())) {
                    return true;
                }
                if (roleOwnedByUser.getQualifiedForUser() != null
                        && roleOwnedByUser.getQualifiedForUser().equals(ownership.getUserOwner())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * This is not the fully featured permission check!<br>
     * This method checks if a user owns all permissions implied by the given {@link WildcardPermission}. If a user
     * wants to grant another user a specific permissions, he needs to own all permissions that may be implied by the
     * granted permission.<br>
     * The regular permission check only succeeds if a requested permission is completely implied by a granted
     * permission. This is fine when checking for a specific permission like USER:UPDATE:my_user. If one user grants a
     * meta permission like USER:*:* for another user this would mean the current user needs an equivalent meta
     * permission or one that is even more general like *:*:* or USER,USER_GROUP:*:*. In contrast to that, a user who
     * owns the permissions USER:*:* and USER_GROUP:*:* may not grant USER,USER_GROUP:*:* because this meta permission
     * is not implied by a single permission held by the current user.<br>
     * To solve this we use the given {@link HasPermissions} instances to resolve wildcards for the type and action
     * parts and construct distinct permissions to check. This means the given {@link HasPermissions} need to be a
     * complete list for the running system. Otherwise we potentially do not check for all required types if the given
     * {@link WildcardPermission} has a wildcard as type part.
     */
    public static <RD extends RoleDefinition, R extends AbstractRole<RD, G, UR>, O extends AbstractOwnership<G, UR>, UR extends UserReference, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean checkMetaPermission(
            WildcardPermission permission,
            Iterable<HasPermissions> allPermissionTypes, U user, U allUser, O ownership) {
        return checkMetaPermissionInternal(permission, allPermissionTypes, user, allUser, wp -> ownership);
    }
    
    /**
     * This is not the fully featured permission check!<br>
     * See {@link #checkMetaPermission(WildcardPermission, Iterable, SecurityUser, SecurityUser, AbstractOwnership)} for
     * more information about the check.<br>
     * This version of the meta permission check relies on dynamic resolution of ownerships via the given
     * ownershipResolver. Due to the absence of ownership information this check is not possible in the UI.
     * 
     * @param ownershipResolver
     *            due to the fact that complex {@link WildcardPermission WildcardPermissions} are expanded using
     *            {@link #expandSingleWildcardPermissionToDistinctPermissions(WildcardPermission, Iterable, boolean)},
     *            for permissions containing a single id or a list of distinct ids (no wildcard in the id part) this
     *            results in expanded permissions that can be used to resolve to a single
     *            {@link QualifiedObjectIdentifier} which in fact allows the surrounding system to resolve an
     *            {@link Ownership} for those permissions. Using this, it is possible to restrict the permission check
     *            to the effectively existing owning qualification. If no {@link Ownership} can be resolved, the check
     *            can not be restricted to an owning qualification and must pass unrestricted in this consequence.
     */
    public static <RD extends RoleDefinition, R extends AbstractRole<RD, G, UR>, O extends AbstractOwnership<G, UR>, UR extends UserReference, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean checkMetaPermissionWithOwnershipResolution(
            WildcardPermission permission, Iterable<HasPermissions> allPermissionTypes, U user, U allUser,
            Function<QualifiedObjectIdentifier, O> ownershipResolver) {
        return checkMetaPermissionInternal(permission, allPermissionTypes, user, allUser, wp -> {
            final Iterable<QualifiedObjectIdentifier> qualifiedObjectIdentifiers = wp.getQualifiedObjectIdentifiers();
            final O ownership;
            Iterator<QualifiedObjectIdentifier> iterator = qualifiedObjectIdentifiers.iterator();
            if (!iterator.hasNext()) {
                ownership = null;
            } else {
                QualifiedObjectIdentifier firstIdentifier = iterator.next();
                if (iterator.hasNext() || firstIdentifier == null) {
                    // No single distinct identifier
                    ownership = null;
                } else {
                    ownership = ownershipResolver.apply(firstIdentifier);
                }
            }
            return ownership;
        });
    }
    
    private static <RD extends RoleDefinition, R extends AbstractRole<RD, G, UR>, O extends AbstractOwnership<G, UR>, UR extends UserReference, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean checkMetaPermissionInternal(
            WildcardPermission permission,
            Iterable<HasPermissions> allPermissionTypes, U user, U allUser, Function<WildcardPermission, O> ownershipResolver) {
        assert permission != null;
        assert allPermissionTypes != null;
        final Set<WildcardPermission> effectivePermissionsToCheck = expandSingleWildcardPermissionToDistinctPermissions(permission, allPermissionTypes, true);
        for (WildcardPermission effectiveWildcardPermissionToCheck : effectivePermissionsToCheck) {
            final O ownership = ownershipResolver.apply(effectiveWildcardPermissionToCheck);
            if (checkUserPermissions(effectiveWildcardPermissionToCheck, user, getGroupsOfUser(user), ownership,
                    impliesChecker, /* matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven */ true) != PermissionState.GRANTED
                    && checkUserPermissions(effectiveWildcardPermissionToCheck, allUser, getGroupsOfUser(allUser),
                            ownership, impliesChecker, /* matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven */ true) != PermissionState.GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * WildcardPermissions can be pretty complex and allow wildcards for all parts. With the knowledge of all specific
     * permissions that are implied by a WildcardPermission, we can do more distinct checks that do not require a user
     * to have permission with an equivalent wildcard.
     * 
     * With the specific knowledge of all existing secured types and their associated actions, we can:
     * <ul>
     * <li>Resolve * for the type to a set of existing types</li>
     * <li>Filter a list of action per declared type (pruning of impossible permissions)</li>
     * </ul>
     * 
     * A user having distinct permissions for all existing types, will in fact be able to succeed a permission check for
     * *, although, he does not explicitly have * as a permission.
     * 
     * The following rules apply for the expansion of permissions:
     * <ul>
     * <li>A list of types is expanded to single type permissions. Explicitly declared but unknown types are not
     * pruned.</li>
     * <li>A type wildcard is expanded to single permissions for all known types</li>
     * <li>A list of actions is expanded to single action permissions. For known types, the set of actions is pruned to
     * the effectively existing actions. For unknown types, the action list is preserved.</li>
     * <li>A action wildcard is expanded to single action permissions per known type or a wildcard for unknown
     * types.</li>
     * </ul>
     * 
     * @param expandToSingleIds
     *            if true, a list of ids is also expanded to distinct permissions instead of a permission having a list
     *            of ids. This is useful when it is necessary to resolve e.g. type identifiers from a
     *            {@link WildcardPermission}.
     */
    private static Set<WildcardPermission> expandSingleWildcardPermissionToDistinctPermissions(WildcardPermission permission,
            Iterable<HasPermissions> allPermissionTypes, boolean expandToSingleIds) {
        List<Set<String>> parts = permission.getParts();
        final Set<String> typeParts;
        final boolean isTypePartWildcard;
        if (parts.size() >= 1) {
            typeParts = parts.get(0);
            isTypePartWildcard = typeParts.isEmpty() || typeParts.contains(WildcardPermission.WILDCARD_TOKEN);
        } else {
            typeParts = null;
            isTypePartWildcard = true;
        }

        final Set<String> actionParts;
        final boolean isActionPartWildcard;
        if (parts.size() >= 2) {
            actionParts = parts.get(1);
            isActionPartWildcard = actionParts.isEmpty() || actionParts.contains(WildcardPermission.WILDCARD_TOKEN);
        } else {
            actionParts = null;
            isActionPartWildcard = true;
        }

        final Set<String> idParts;
        final boolean isIdPartWildcard;
        if (parts.size() >= 3) {
            idParts = parts.get(2);
            isIdPartWildcard = idParts.isEmpty() || idParts.contains(WildcardPermission.WILDCARD_TOKEN);
        } else {
            idParts = null;
            isIdPartWildcard = true;
        }

        Map<String, HasPermissions> allPermissionTypesByName = new HashMap<>();
        for (HasPermissions hasPermissions : allPermissionTypes) {
            allPermissionTypesByName.put(hasPermissions.getName(), hasPermissions);
        }

        final Set<String> effectiveIdPartsToCheck;
        if (isIdPartWildcard) {
            effectiveIdPartsToCheck = Collections.singleton("");
        } else if(expandToSingleIds) {
            effectiveIdPartsToCheck = idParts;
        } else {
            effectiveIdPartsToCheck = Collections.singleton(Util.joinStrings(WildcardPermission.SUBPART_DIVIDER_TOKEN, idParts));
        }
        
        final Set<String> effectiveTypePartsToCheck;
        if (isTypePartWildcard) {
            effectiveTypePartsToCheck = allPermissionTypesByName.keySet();
        } else {
            effectiveTypePartsToCheck = typeParts;
        }

        final Set<WildcardPermission> effectivePermissionsToCheck = new HashSet<>();
        for (String typePart : effectiveTypePartsToCheck) {
            HasPermissions hasPermissions = allPermissionTypesByName.get(typePart);
            final Set<String> effectiveActionPartsToCheck;
            if (hasPermissions == null) {
                effectiveActionPartsToCheck = actionParts;
            } else {
                effectiveActionPartsToCheck = new HashSet<>();
                for (Action action : hasPermissions.getAvailableActions()) {
                    if (isActionPartWildcard || actionParts.contains(action.name())) {
                        effectiveActionPartsToCheck.add(action.name());
                    }
                }
            }

            for (String actionPart : effectiveActionPartsToCheck) {
                for (String idPart : effectiveIdPartsToCheck) {
                    String idSuffix = idPart.isEmpty() ? "" : WildcardPermission.PART_DIVIDER_TOKEN + idPart;
                    effectivePermissionsToCheck.add(new WildcardPermission(
                            typePart + WildcardPermission.PART_DIVIDER_TOKEN + actionPart + idSuffix));
                }
            }
        }
        return effectivePermissionsToCheck;
    }
    
    /**
     * Checks if a user has at least one permission implied by the given {@link WildcardPermission}.<br>
     * This is e.g. useful to decide if a user can see a specific tab/button/... in the UI. For example a user who has
     * Permission USER:UPDATE:abc may see the user management UI. A generic Permission check for USER:UPDATE will fail
     * because only users that have an unqualified version of a permission would pass that permission check. This means
     * we need to check the permission for any possibly existing object ID.
     */
    public static <RD extends RoleDefinition, R extends AbstractRole<RD, G, UR>, O extends AbstractOwnership<G, UR>, UR extends UserReference, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean hasUserAnyPermission(
            WildcardPermission permission, Iterable<HasPermissions> allPermissionTypes, U user, U allUser,
            O ownership) {
        assert permission != null;
        assert allPermissionTypes != null;
        final Set<WildcardPermission> effectivePermissionsToCheck = expandSingleWildcardPermissionToDistinctPermissions(permission, allPermissionTypes, false);
        
        for (WildcardPermission effectiveWildcardPermissionToCheck : effectivePermissionsToCheck) {
            if (checkUserPermissions(effectiveWildcardPermissionToCheck, user, getGroupsOfUser(user), ownership, impliesAnyChecker,
                    /* matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven */ false) == PermissionState.GRANTED
                    || checkUserPermissions(effectiveWildcardPermissionToCheck, allUser, getGroupsOfUser(allUser), ownership, impliesAnyChecker,
                            /* matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven */ false) == PermissionState.GRANTED) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param permissionChecker
     *            The permissionChecker is the one that checks if one permission implies another permission. This means,
     *            it just calls {@link WildcardPermission#implies(WildcardPermission)} in most cases. In case of an any
     *            check this needs to call {@link WildcardPermission#impliesAny(WildcardPermission)).
     * @param matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven
     *            If {@code false} and no ownership is given, all associated {@link Role Roles} of an user are checked.
     *            For a normal permission check, this needs to be {@code true}, because Qualified {@link Roles Roles}
     *            can not grant unqualified permissions. In case of an any check it is necessary to include all
     *            qualified roles of an user, because even a qualified {@link Role} may grant any of the included
     *            permissions. in this case, the parameter is intended to be {@code false}.
     */
    private static <RD extends RoleDefinition, R extends AbstractRole<RD, G, UR>, O extends AbstractOwnership<G, UR>, UR extends UserReference, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> PermissionState checkUserPermissions(
            WildcardPermission permission, U user, Iterable<G> groupsOfWhichUserIsMember, O ownership,
            WildcardPermissionChecker permissionChecker, boolean matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven) {
        PermissionState result = PermissionState.NONE;
        // 2. check direct permissions
        if (result == PermissionState.NONE && user != null) { // no direct permissions for anonymous users
            for (WildcardPermission directPermission : user.getPermissions()) {
                if (permissionChecker.check(directPermission, permission)) {
                    result = PermissionState.GRANTED;
                    break;
                }
            }
        }
        
        // TODO documentation
        final boolean ownershipIsGiven = ownership != null
                && (ownership.getTenantOwner() != null || ownership.getUserOwner() != null);
        if (ownershipIsGiven) {
            final G tenantOwner = ownership.getTenantOwner();
            if (tenantOwner != null) {
                final boolean userIsMemberOfGroup = Util.contains(groupsOfWhichUserIsMember, tenantOwner);
                if (isPermissionGrantedByGroup(permission, tenantOwner, userIsMemberOfGroup, permissionChecker)) {
                    result = PermissionState.GRANTED;
                }
            }
        } else if (!matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven) {
            for (G groupToCheck : groupsOfWhichUserIsMember) {
                if (isPermissionGrantedByGroup(permission, groupToCheck, true, permissionChecker)) {
                    result = PermissionState.GRANTED;
                    break;
                }
            }
        }
        
        // 3. check role permissions
        if (result == PermissionState.NONE && user != null) { // an anonymous user does not have any roles
            for (R role : user.getRoles()) {
                if (implies(role, permission, ownership, permissionChecker,
                        matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven)) {
                    result = PermissionState.GRANTED;
                    break;
                }
            }
        }
        return result;
    }
    
    private static <RD extends RoleDefinition, U extends SecurityUser<RD, ?, G>, G extends SecurityUserGroup<RD>> boolean isPermissionGrantedByGroup(
            WildcardPermission permission, G groupToCheck, boolean userIsMemberOfGroup,
            WildcardPermissionChecker permissionChecker) {
        for (Map.Entry<RD, Boolean> entry : groupToCheck.getRoleDefinitionMap().entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue()) || userIsMemberOfGroup) {
                final RD roleDefinition = entry.getKey();
                for (WildcardPermission grantedPermission : roleDefinition.getPermissions()) {
                    if (permissionChecker.check(grantedPermission, permission)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * @param role
     *            the role; it may have a {@link Role#getQualifiedForTenant() tenant} and/or
     *            {@link Role#getQualifiedForUser() user} qualification which means the role's permissions are granted
     *            only if the {@code ownership}'s {@link Ownership#getTenantOwner() tenant} and/or
     *            {@link Ownership#getUserOwner() user} owner match the role qualification. E.g.:
     *            {@code event-admin:tw2016 -> {event:edit:*, regatta:edit:*}} This role would grant the user edit
     *            permission for every event and regatta where the tenant owner is "tw2016".
     * @param permission
     *            the permission for which to check whether it shall be granted to the user for the object whose
     *            {@code ownership} information is provided. E.g. "regatta:edit:tw2016-dyas" (would return true if the
     *            object "tw2016-dyas" would have "tw2016" as the tenant owner in case the role is qualified with the
     *            "tw2016" tenant)
     * @param ownership
     *            Ownership of the data object for which the {@code permission} is requested
     * @param permissionChecker
     *            The permissionChecker is the one that checks if one permission implies another permission. This means,
     *            it just calls {@link WildcardPermission#implies(WildcardPermission)} in most cases. In case of an any
     *            check this needs to call {@link WildcardPermission#impliesAny(WildcardPermission)).
     * @param matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven
     *            If {@code false} and no ownership is given, all associated {@link Role Roles} of an user are checked.
     *            For a normal permission check, this needs to be {@code true}, because Qualified {@link Roles Roles}
     *            can not grant unqualified permissions. In case of an any check it is necessary to include all
     *            qualified roles of an user, because even a qualified {@link Role} may grant any of the included
     *            permissions.
     */
    private static <RD extends RoleDefinition, R extends AbstractRole<RD, G, UR>, O extends AbstractOwnership<G, UR>, UR extends UserReference, U extends SecurityUser<RD, R, G>, G extends SecurityUserGroup<RD>, A extends SecurityAccessControlList<G>> boolean implies(
            R role, WildcardPermission permission, O ownership,
            WildcardPermissionChecker permissionChecker,
           boolean matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven) {
       final boolean roleIsTenantQualified = role.getQualifiedForTenant() != null;
       final boolean roleIsUserQualified = role.getQualifiedForUser() != null;
       final boolean permissionsApply;
       boolean result;
       // It is possible that we have an ownership with null tenantOwner and null userOwner. This should be handled like no ownership was given.
       final boolean ownershipIsGiven = ownership != null && (ownership.getTenantOwner() != null || ownership.getUserOwner() != null);
       if (roleIsTenantQualified || roleIsUserQualified) {
           if (!ownershipIsGiven) {
               permissionsApply = !matchOnlyNonQualifiedRolesIfNoOwnershipIsGiven; // qualifications cannot be verified as no ownership info is provided; permissions do not apply
           } else {
               permissionsApply = (!roleIsTenantQualified || Util.equalsWithNull(role.getQualifiedForTenant(), ownership.getTenantOwner())) &&
                                  (!roleIsUserQualified || isSameUser(role.getQualifiedForUser(), ownership.getUserOwner()));
           }
       } else {
           permissionsApply = true; // permissions apply without qualifications
       }
       if (permissionsApply) {
           result = false; // if the role grants no permissions at all or no permission implies the one requested for, access is not granted
            for (WildcardPermission rolePermission : role.getPermissions()) {
                if (permissionChecker.check(rolePermission, permission)) {
                    result = true;
                    break;
                }
            }
        } else {
            result = false;
        }
        return result;
    }
   
    /**
     * There are cases where need to check instances of different implementations if they represent the same user.
     */
    public static boolean isSameUser(UserReference user1, UserReference user2) {
        if (user1 == user2) {
            return true;
        }
        if (user1 == null || user2 == null) {
            return false;
        }
        return user1.getName().equals(user2.getName());
    }
}