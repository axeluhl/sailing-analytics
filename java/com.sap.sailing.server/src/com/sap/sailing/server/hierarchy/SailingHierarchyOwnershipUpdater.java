package com.sap.sailing.server.hierarchy;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.SecurityUtils;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Encapsulates the logic to ensure consistency of group owners in the sailing domain object hierarchy.
 */
public class SailingHierarchyOwnershipUpdater {

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

    public SailingHierarchyOwnershipUpdater(final RacingEventService service, SecurityService securityService,
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
}
