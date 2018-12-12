package com.sap.sailing.server.hierarchy;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.shiro.SecurityUtils;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

/**
 * Encapsulates the logic to ensure consistency of group owners in the sailing domain object hierarchy.
 */
public class SailingHierarchyOwnershipUpdater {
    public static SailingHierarchyOwnershipUpdater createOwnershipUpdater(boolean createNewGroup,
            UUID existingGroupIdOrNull, String newGroupName, boolean migrateCompetitors, boolean migrateBoats,
            RacingEventService service) {
        SecurityService securityService = service.getSecurityService();

        final UserGroup sourceGroup = securityService.getUserGroup(existingGroupIdOrNull);

        final GroupOwnerUpdateStrategy updateStrategy;
        if (!createNewGroup) {
            updateStrategy = createExitingGroupModifyingUpdate(sourceGroup);
        } else {
            updateStrategy = createNewGroupUsingUpdate(newGroupName, securityService, sourceGroup);
        }
        return new SailingHierarchyOwnershipUpdater(service, securityService, updateStrategy, migrateCompetitors,
                migrateBoats);
    }

    public interface GroupOwnerUpdateStrategy {
        boolean needsUpdate(QualifiedObjectIdentifier identifier, OwnershipAnnotation currentOwnership);

        UserGroup getNewGroupOwner();
    }

    private final RacingEventService service;
    private final SecurityService securityService;
    private final GroupOwnerUpdateStrategy updateStrategy;
    private final boolean updateCompetitors;
    private final boolean updateBoats;
    private final Set<QualifiedObjectIdentifier> objectsToUpdateOwnershipsFor;

    private SailingHierarchyOwnershipUpdater(final RacingEventService service, SecurityService securityService,
            final GroupOwnerUpdateStrategy updateStrategy, final boolean updateCompetitors, final boolean updateBoats) {
        this.service = service;
        this.securityService = securityService;
        this.updateStrategy = updateStrategy;
        this.updateCompetitors = updateCompetitors;
        this.updateBoats = updateBoats;
        objectsToUpdateOwnershipsFor = new HashSet<>();
    }

    public void updateGroupOwnershipForEventHierarchy(Event event) {
        updateGroupOwnershipForEventHierarchyInternal(event);
        commitChanges();
    }

    private void updateGroupOwnershipForEventHierarchyInternal(Event event) {
        updateGroupOwner(event.getIdentifier());
        SailingHierarchyWalker.walkFromEvent(event, false, new EventHierarchyVisitor() {
            @Override
            public void visit(Leaderboard leaderboard, Set<LeaderboardGroup> leaderboardGroups) {
                updateGroupOwnershipForLeaderboardHierarchy(leaderboard);
            }

            @Override
            public void visit(LeaderboardGroup leaderboardGroup) {
                // No LeaderboardGroups with overall leaderboard are visited -> no infinite recursion occurs
                updateGroupOwnershipForLeaderboardGroupHierarchyInternal(leaderboardGroup);
            }
        });
    }

    public void updateGroupOwnershipForLeaderboardGroupHierarchy(LeaderboardGroup leaderboardGroup) {
        updateGroupOwnershipForLeaderboardGroupHierarchyInternal(leaderboardGroup);
        commitChanges();
    }

    public void updateGroupOwnershipForLeaderboardGroupHierarchyInternal(LeaderboardGroup leaderboardGroup) {
        updateGroupOwner(leaderboardGroup.getIdentifier());
        SailingHierarchyWalker.walkFromLeaderboardGroup(service, leaderboardGroup, true,
                new LeaderboardGroupHierarchyVisitor() {
                    @Override
                    public void visit(Leaderboard leaderboard) {
                        updateGroupOwnershipForLeaderboardHierarchy(leaderboard);
                    }

                    @Override
                    public void visit(Event event) {
                        // Only events of LeaderboardGroups with overall leaderboard are visited -> no infinite
                        // recursion occurs
                        updateGroupOwnershipForEventHierarchyInternal(event);
                    }
                });
    }

    public void updateGroupOwnershipForLeaderboardHierarchy(Leaderboard leaderboard) {
        updateGroupOwner(leaderboard.getIdentifier());
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
            updateGroupOwner(regattaLeaderboard.getRegatta().getIdentifier());
        }
        SailingHierarchyWalker.walkFromLeaderboard(leaderboard, new LeaderboardHierarchyVisitor() {
            @Override
            public void visit(TrackedRace race) {
                updateGroupOwner(race.getIdentifier());
            }

            @Override
            public void visit(Boat boat) {
                if (updateBoats) {
                    updateGroupOwner(boat.getIdentifier());
                }
            }

            @Override
            public void visit(Competitor competitor) {
                if (updateCompetitors) {
                    updateGroupOwner(competitor.getIdentifier());
                }
            }
        });
    }

    private void updateGroupOwner(QualifiedObjectIdentifier id) {
        final OwnershipAnnotation ownership = securityService.getOwnership(id);
        if (updateStrategy.needsUpdate(id, ownership)) {
            String permissionToCheck = id.getTypeIdentifier() + WildcardPermission.PART_DIVIDER_TOKEN
                    + DefaultActions.CHANGE_OWNERSHIP.name() + WildcardPermission.PART_DIVIDER_TOKEN
                    + id.getTypeRelativeObjectIdentifier();
            SecurityUtils.getSubject().checkPermission(permissionToCheck);
            objectsToUpdateOwnershipsFor.add(id);
        }
    }

    private void commitChanges() {
        final UserGroup groupOwnerToSet = updateStrategy.getNewGroupOwner();
        for (QualifiedObjectIdentifier id : objectsToUpdateOwnershipsFor) {
            final OwnershipAnnotation ownership = securityService.getOwnership(id);
            securityService.setOwnership(id, ownership == null ? null : (User) ownership.getAnnotation().getUserOwner(),
                    groupOwnerToSet);
        }
    }



    private static GroupOwnerUpdateStrategy createExitingGroupModifyingUpdate(final UserGroup sourceGroup) {
        if (sourceGroup == null) {
            throw new RuntimeException("User group does not exist");
        }
        final GroupOwnerUpdateStrategy updateStrategy;
        updateStrategy = new GroupOwnerUpdateStrategy() {
            @Override
            public boolean needsUpdate(QualifiedObjectIdentifier identifier, OwnershipAnnotation currentOwnership) {
                return currentOwnership == null
                        || !sourceGroup.equals(currentOwnership.getAnnotation().getTenantOwner());
            }

            @Override
            public UserGroup getNewGroupOwner() {
                return sourceGroup;
            }
        };
        return updateStrategy;
    }

    private static GroupOwnerUpdateStrategy createNewGroupUsingUpdate(String newGroupName,
            SecurityService securityService, final UserGroup sourceGroup) {
        if (newGroupName == null || newGroupName.isEmpty()) {
            throw new RuntimeException("No name for new Group given");
        }

        final GroupOwnerUpdateStrategy updateStrategy;
        updateStrategy = new GroupOwnerUpdateStrategy() {

            private UserGroup groupOwnerToSet;

            @Override
            public boolean needsUpdate(QualifiedObjectIdentifier identifier, OwnershipAnnotation currentOwnership) {
                return true;
            }

            @Override
            public UserGroup getNewGroupOwner() {
                if (groupOwnerToSet == null) {
                    try {
                        if (sourceGroup != null) {
                            // When migrating from an existing user group -> copy as much as possible from the
                            // existing group to make the migrated objects to be visible for most people as before
                            groupOwnerToSet = copyUserGroup(sourceGroup, newGroupName, securityService);
                        } else {
                            // The migration may start at an object that currently has no group owner (e.g. in case
                            // this owner was just deleted) -> in this case we just create a new group
                            groupOwnerToSet = securityService.createUserGroup(UUID.randomUUID(), newGroupName);
                        }
                    } catch (UserGroupManagementException e) {
                        throw new RuntimeException("Could not create user group");
                    }
                }
                return groupOwnerToSet;
            }
        };
        return updateStrategy;
    }

    private static UserGroup copyUserGroup(UserGroup userGroupToCopy, String name, SecurityService securitySerice)
            throws UserGroupManagementException {
        // explicitly loading the current version of the group in case the given instance e.g. originates from the UI
        // and is possible out of date.
        final UUID newGroupId = UUID.randomUUID();
        return securitySerice.setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredSecurityTypes.USER_GROUP, newGroupId, name, () -> {
                    final UserGroup createdUserGroup = securitySerice.createUserGroup(newGroupId, name);
                    securitySerice.copyUsersAndRoleAssociations(userGroupToCopy, createdUserGroup);
                    return createdUserGroup;
                });
    }
}
