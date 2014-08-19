package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;

public class EditMarkPassingsPanel extends Grid implements RaceSelectionChangeListener, CompetitorSelectionChangeListener {
    
    
    Button setSelectedTimeAsMarkPassingsButton;
    Button svButton;
    Button removeSetMarkPassingsButton;
    IntegerBox suppressMarkPassings;
    
    Grid wayPointSelectionGrid;
    TextBox displayMarkPassingsTimeBox;

    public EditMarkPassingsPanel(SailingServiceAsync sailingService, Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider,
            StringMessages stringMessages, AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter) {
    
        super(3,2);
        svButton = new Button(stringMessages.save());
        removeSetMarkPassingsButton = new Button("Remove Manually-Set MarkPassing");
        setSelectedTimeAsMarkPassingsButton = new Button("Set Selected Time as MarkPassing");
        suppressMarkPassings = new IntegerBox();
        setWidget(0, 0, setSelectedTimeAsMarkPassingsButton);
        setWidget(0, 1, removeSetMarkPassingsButton);
        setWidget(1, 0, suppressMarkPassings);
        setWidget(2, 0, svButton);
        setVisible(false);
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        // TODO Auto-generated method stub
        
    }
}
