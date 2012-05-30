package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;

/**
 * Allows administrators to manage the structure of a complete event.
 * Each event consists of several substructures like regattas, races, series and groups (big fleets divided into racing groups).
 * @author Frank Mittag (C5163974)
 *
 */
public class EventStructureManagementPanel extends SimplePanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    
    private final List<EventDTO> events;
    private final ListBox eventsComboBox;
    
    public EventStructureManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages theStringMessages) {
        this.sailingService = sailingService;
        this.stringMessages = theStringMessages;
        this.errorReporter = errorReporter;
        
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
     
        HorizontalPanel eventsPanel = new HorizontalPanel();
        mainPanel.add(eventsPanel);
        
        Label managedEventsLabel = new Label(stringMessages.events() + ":");
        eventsPanel.add(managedEventsLabel);
        
        events = new ArrayList<EventDTO>();
        
        eventsComboBox = new ListBox();
        eventsComboBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
            }
        });
        eventsPanel.add(eventsComboBox);
        
        Button createEventBtn = new Button(stringMessages.add());
        createEventBtn.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
            }
        });
        eventsPanel.add(createEventBtn);
        
        fillEvents();
    }
    
    private void fillEvents() {
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getEvents() - Failure: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(List<EventDTO> result) {
                events.clear();
                events.addAll(result);
                eventsComboBox.clear();
                for (EventDTO event : result) {
                    eventsComboBox.addItem(event.name);
                }
            }
        });
    }

}
