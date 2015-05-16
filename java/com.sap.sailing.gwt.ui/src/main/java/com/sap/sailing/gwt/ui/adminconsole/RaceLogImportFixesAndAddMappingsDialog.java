package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RaceLogImportFixesAndAddMappingsDialog extends AbstractLogImportFixesAndAddMappingsDialog {
    private String leaderboardName;
    private String raceColumnName;
    private String fleetName;


    public RaceLogImportFixesAndAddMappingsDialog(SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, String leaderboardName, String raceColumnName, String fleetName,
            DialogCallback<Collection<DeviceMappingDTO>> callback) {
        super(sailingService,errorReporter, stringMessages, callback);
        
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        
        getCompetitorRegistrations(sailingService, errorReporter);
        getMarks(sailingService, errorReporter);       
    }
    
    @Override
    void getMarks(SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        sailingService.getMarksInRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Iterable<MarkDTO>>() {
            @Override
            public void onSuccess(Iterable<MarkDTO> result) {
                markTable.refresh(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load marks: " + caught.getMessage());
            }
        });
    }

    @Override
    void getCompetitorRegistrations(SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        sailingService.getCompetitorRegistrations(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Collection<CompetitorDTO>>() {
            @Override
            public void onSuccess(Collection<CompetitorDTO> result) {
                competitorTable.refreshCompetitorList(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load competitors: " + caught.getMessage());
            }
        });
    }

}
