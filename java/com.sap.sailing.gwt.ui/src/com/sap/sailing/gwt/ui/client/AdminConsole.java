package com.sap.sailing.gwt.ui.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.shared.EventDAO;

public class AdminConsole implements EntryPoint, ErrorReporter, EventRefresher {
    /**
     * Create a remote service proxy to talk to the server-side sailing service.
     */
    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private DialogBox errorDialogBox;
    private HTML serverResponseLabel;
    private Button dialogCloseButton;
    private Set<EventDisplayer> eventDisplayers;
    
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while " //$NON-NLS-1$
            + "attempting to contact the server. Please check your network " + "connection and try again."; //$NON-NLS-1$ //$NON-NLS-2$


    @Override
    public void onModuleLoad() {
        StringConstants stringConstants = GWT.create(StringConstants.class);
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("95%", "95%");
        errorDialogBox = createErrorDialog();
        
        TabPanel tabPanel = new TabPanel();
        tabPanel.setAnimationEnabled(true);
        rootPanel.add(tabPanel, 10, 10);
        tabPanel.setSize("95%", "95%");

        eventDisplayers = new HashSet<EventDisplayer>();
        EventManagementPanel eventManagementPanel = new EventManagementPanel(sailingService, this, this, stringConstants);
        eventDisplayers.add(eventManagementPanel);
        eventManagementPanel.setSize("90%", "90%");
        tabPanel.add(eventManagementPanel, stringConstants.tracTracEvents(), false);
        WindPanel windPanel = new WindPanel(sailingService, this, this, stringConstants);
        eventDisplayers.add(windPanel);
        windPanel.setSize("90%", "90%");
        tabPanel.add(windPanel, stringConstants.wind(), /* asHTML */ false);
        final RaceMapPanel raceMapPanel = new RaceMapPanel(sailingService, this, this, stringConstants);
        eventDisplayers.add(raceMapPanel);
        raceMapPanel.setSize("90%", "90%");
        tabPanel.add(raceMapPanel, stringConstants.map(), /* asHTML */ false);
        LeaderboardPanel defaultLeaderboardPanel = new LeaderboardPanel(sailingService, stringConstants.defaultLeaderboard(), this, stringConstants);
        defaultLeaderboardPanel.setSize("90%", "90%");
        tabPanel.add(defaultLeaderboardPanel, stringConstants.defaultLeaderboard(), /* asHTML */ false);
        
        tabPanel.selectTab(0);
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if(raceMapPanel.isVisible()) {
					raceMapPanel.onResize();
				}				
			}
		});
        fillEvents();
    }

    private DialogBox createErrorDialog() {
        // Create the popup dialog box
        final DialogBox myErrorDialogBox = new DialogBox();
        myErrorDialogBox.setText("Remote Procedure Call"); //$NON-NLS-1$
        myErrorDialogBox.setAnimationEnabled(true);
        dialogCloseButton = new Button("Close"); //$NON-NLS-1$
        // We can set the id of a widget by accessing its Element
        dialogCloseButton.getElement().setId("closeButton"); //$NON-NLS-1$
        final Label textToServerLabel = new Label();
        serverResponseLabel = new HTML();
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.addStyleName("dialogVPanel"); //$NON-NLS-1$
        dialogVPanel.add(new HTML("<b>Sending name to the server:</b>")); //$NON-NLS-1$
        dialogVPanel.add(textToServerLabel);
        dialogVPanel.add(new HTML("<br><b>Server replies:</b>")); //$NON-NLS-1$
        dialogVPanel.add(serverResponseLabel);
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        dialogVPanel.add(dialogCloseButton);
        myErrorDialogBox.setWidget(dialogVPanel);
        // Add a handler to close the DialogBox
        dialogCloseButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                myErrorDialogBox.hide();
            }
        });
        return myErrorDialogBox;
    }

    @Override
    public void reportError(String message) {
        errorDialogBox.setText(message);
        serverResponseLabel.addStyleName("serverResponseLabelError"); //$NON-NLS-1$
        serverResponseLabel.setHTML(SERVER_ERROR);
        errorDialogBox.center();
        dialogCloseButton.setFocus(true);
    }
    
    @Override
    public void fillEvents() {
        sailingService.listEvents(new AsyncCallback<List<EventDAO>>() {
            @Override
            public void onSuccess(List<EventDAO> result) {
                for (EventDisplayer eventDisplayer : eventDisplayers) {
                    eventDisplayer.fillEvents(result);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                reportError("Remote Procedure Call listEvents() - Failure");
            }
        });
    }

}
