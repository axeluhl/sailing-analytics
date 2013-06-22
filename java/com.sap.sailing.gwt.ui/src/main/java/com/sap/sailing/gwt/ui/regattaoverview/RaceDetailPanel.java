package com.sap.sailing.gwt.ui.regattaoverview;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RaceDetailPanel extends SimplePanel {
    
    private Button closeButton;
    private Label raceLabel;
    
    public RaceDetailPanel(final StringMessages stringMessages, ClickHandler closeButtonHandler) {
        
        closeButton = new Button(stringMessages.close());
        if (closeButtonHandler != null) {
            closeButton.addClickHandler(closeButtonHandler);
        }
        
        raceLabel = new Label();
        
        Grid grid = new Grid(4, 2);
        grid.setWidget(1, 0, new Label("Race"));
        grid.setWidget(1, 1, raceLabel);
        
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(closeButton);
        mainPanel.add(grid);
        setWidget(mainPanel);
    }

    public void show(RegattaOverviewEntryDTO entry) {
        raceLabel.setText(entry.regattaDisplayName + " " + entry.raceInfo.raceName);
    }
    
}