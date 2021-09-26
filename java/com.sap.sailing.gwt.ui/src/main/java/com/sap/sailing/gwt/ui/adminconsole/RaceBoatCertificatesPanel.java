package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;

/**
 * This panel is a {@link AbstractBoatCertificatesPanel} where the storage context for the certificate assignments is a
 * {@code RaceLog}, identified by a {@link StrippedLeaderboardDTO}, a {@link RaceColumnDTO} and a {@link FleetDTO}
 * 
 * @author Daniel Lisunkin (i505543)
 * @author Axel Uhl (d043530)
 *
 */
public class RaceBoatCertificatesPanel extends AbstractBoatCertificatesPanel {
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;

    public RaceBoatCertificatesPanel(final SailingServiceWriteAsync sailingServiceWrite, final UserService userService,
            final StrippedLeaderboardDTOWithSecurity leaderboard, final RaceColumnDTO raceColumn, final FleetDTO fleet,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        // TODO problem: what is the secured object here? What is it for other RaceLog updates?
        super(sailingServiceWrite, userService, leaderboard, stringMessages, errorReporter,
                /* context update permission check: */ () -> userService.hasPermission(leaderboard,
                        DefaultActions.UPDATE),
                leaderboard.getName() + "/" + raceColumn.getName() + "/" + fleet.getName());
        this.leaderboardName = leaderboard.getName();
        this.raceColumnName = raceColumn.getName();
        this.fleetName = fleet.getName();
        refresh();
    }

    @Override
    protected void assignCertificates(SailingServiceWriteAsync sailingServiceWrite,
            Map<String, ORCCertificate> certificatesByBoatIdAsString,
            AsyncCallback<Triple<Integer, Integer, Integer>> callback) {
        sailingServiceWrite.assignORCPerformanceCurveCertificates(leaderboardName, raceColumnName, fleetName,
                certificatesByBoatIdAsString, callback);
    }

    @Override
    protected void getORCCertificateAssignemtnsByBoatIdAsString(SailingServiceAsync sailingService,
            AsyncCallback<Map<String, ORCCertificate>> callbackForGetCertificates) {
        sailingService.getORCCertificateAssignmentsByBoatIdAsString(leaderboardName, raceColumnName, fleetName, callbackForGetCertificates);
    }

    protected void getBoats(SailingServiceAsync sailingService,
            final AsyncCallback<Collection<BoatDTO>> callbackForGetBoats) {
        sailingService.getBoatRegistrationsForLeaderboard(leaderboardName, callbackForGetBoats);
    }
}
