package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLogCompetitorRegistrationDialog extends AbstractCompetitorRegistrationsDialog {

    private String leaderboardName;

    public RegattaLogCompetitorRegistrationDialog(String boatClass, SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean editable, String leaderboardName,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Set<CompetitorDTO>> callback) {
        super(sailingService, stringMessages, errorReporter, editable, callback, boatClass);
        this.leaderboardName = leaderboardName;
    }

    @Override
    protected void setRegisteredCompetitors() {
        sailingService.getCompetitorRegistrationsInRegattaLog(leaderboardName,
                new AsyncCallback<Collection<CompetitorDTO>>() {
                    @Override
                    public void onSuccess(Collection<CompetitorDTO> registeredCompetitors) {
                        move(allCompetitorsTable, registeredCompetitorsTable, registeredCompetitors);
                    }

                    @Override
                    public void onFailure(Throwable reason) {
                        errorReporter.reportError("Could not load already registered competitors: "
                                + reason.getMessage());
                    }
                });
    }

    @Override
    public void addAdditionalWidgets(FlowPanel mainPanel) {
    }

    @Override
    protected void setRegisterableCompetitors() {
        allCompetitorsTable.refreshCompetitorList(null, new Callback<Iterable<CompetitorDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                setRegisteredCompetitors();
            }
            
            @Override
            public void onFailure(Throwable reason) {
            }
        });
    }
}
