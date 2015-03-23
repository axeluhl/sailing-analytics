package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.gwt.ui.adminconsole.AbstractLeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SelectRacesDialog extends DataEntryDialog<Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality>> {
    private final RaceTableWrapper<MultiSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>
        racesTable;
    
    public SelectRacesDialog(SailingServiceAsync sailingService, ErrorReporter errorReporter, final StringMessages stringMessages,
            Collection<RaceColumnDTOAndFleetDTOWithNameBasedEquality> races,
            String leaderboardName, DialogCallback<Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality>> dialogCallback) {
        super(stringMessages.selectRaces(), stringMessages.selectRaces(), stringMessages.ok(), stringMessages.cancel(),
                new Validator<Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>() {
                    @Override
                    public String getErrorMessage(Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> valueToValidate) {
                        if (valueToValidate.isEmpty()) {
                            return stringMessages.selectAtLeastOne();
                        }
                        return null;
                    }
        }, true, dialogCallback);
        racesTable = new RaceTableWrapper<MultiSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>(
                sailingService, stringMessages, errorReporter, /* multiSelection */ true);
        racesTable.setSelectedLeaderboardName(leaderboardName);
        racesTable.getDataProvider().getList().addAll(races);
        racesTable.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                validate();
            }
        });
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        return racesTable.asWidget();
    }

    @Override
    protected Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> getResult() {
        return racesTable.getSelectionModel().getSelectedSet();
    }

}
