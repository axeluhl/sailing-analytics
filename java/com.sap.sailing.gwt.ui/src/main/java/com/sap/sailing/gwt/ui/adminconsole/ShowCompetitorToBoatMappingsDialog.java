package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class ShowCompetitorToBoatMappingsDialog extends DialogBox {
    public ShowCompetitorToBoatMappingsDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages, 
            final ErrorReporter errorReporter, String leaderboardName, final String raceColumnName, final String fleetName,
            String raceName) {
        super();
        setText(stringMessages.actionShowCompetitorToBoatAssignments());
        setAnimationEnabled(true);
        setGlassEnabled(true);
        Button okButton = new Button(stringMessages.ok());
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                ShowCompetitorToBoatMappingsDialog.this.hide();
            }
        });
        VerticalPanel vPanel = new VerticalPanel();
        CompetitorToBoatMappingsViewPanel competitorPanel = new CompetitorToBoatMappingsViewPanel(sailingService,
                stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName);
        vPanel.add(new Label(stringMessages.race() + ": " + raceName));
        vPanel.add(competitorPanel);
        vPanel.add(okButton);
        setWidget(vPanel);
    }
}
