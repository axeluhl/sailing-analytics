package com.sap.sailing.gwt.ui.regattaoverview;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceEventDTO;
import com.sap.sailing.gwt.ui.shared.RaceEventLogDTO;

public class RegattaOverviewPanel extends FlowPanel {
    private final SailingServiceAsync sailingService;
    @SuppressWarnings("unused")
	private final ErrorReporter errorReporter;
    @SuppressWarnings("unused")
	private final StringMessages stringMessages;

    private final Label label;
    
    private final VerticalPanel mainPanel;
    
	public RegattaOverviewPanel(final SailingServiceAsync sailingService, 
            ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        mainPanel = new VerticalPanel();
        this.add(mainPanel);
        
        label = new Label("Hallo world");
        mainPanel.add(label);
        
        loadEventLog();
	}
	
	private void loadEventLog() {
		sailingService.getRaceEventLog(new AsyncCallback<RaceEventLogDTO>() {
			
			@Override
			public void onSuccess(RaceEventLogDTO result) {
				label.setText(result.raceName);
				int i = 0;
				for(RaceEventDTO raceEvent: result.raceEvents) {
					mainPanel.add(new Label(i++ + ".) " + raceEvent.eventName));
				}
			}
			
			@Override
			public void onFailure(Throwable arg0) {
			}
		});
	}
}
