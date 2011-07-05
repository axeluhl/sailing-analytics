package com.sap.sailing.gwt.ui.client;

import java.util.Arrays;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.shared.EventDAO;

/**
 * Allows the user to start and stop tracking of events, regattas and races using the TracTrac connector.
 * In particular, previously configured connections can be retrieved from a drop-down list which then
 * pre-populates all connection parameters. The user can also choose to enter connection information
 * manually. Using a "hierarchical" entry system comparable to that of, e.g., the Eclipse CVS connection
 * setup wizard, components entered will be used to automatically assemble the full URL which can still
 * be overwritten manually. There is a propagation order across the fields. Hostname propagates to
 * JSON URL, Live URI and Stored URI. Port Live Data propagates to Port Stored Data, incremented by one.
 * The ports propagate to Live URI and Stored URI, respectively. The event name propagates to the JSON URL.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class EventManagementPanel extends FormPanel {
    private CellList<EventDAO> eventsList;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final IntegerBox storedPortIntegerbox;
    private final TextBox jsonURLBox;
    private final TextBox liveURIBox;
    private final TextBox storedURIBox;
    private final IntegerBox livePortIntegerbox;
    private final TextBox hostnameTextbox;
    private final TextBox eventNameTextbox;
    

    public EventManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        StringConstants stringConstants = GWT.create(StringConstants.class);
        VerticalPanel verticalPanel = new VerticalPanel();
        this.setWidget(verticalPanel);
        verticalPanel.setSize("100%", "100%");
        
        Grid grid = new Grid(7, 2);
        verticalPanel.add(grid);
        verticalPanel.setCellWidth(grid, "100%");
        
        Label lblPredefined = new Label("Tracked Before");
        grid.setWidget(0, 0, lblPredefined);
        
        ListBox comboBox = new ListBox();
        grid.setWidget(1, 0, comboBox);
        comboBox.addItem(stringConstants.kielWeel2011());
        comboBox.addItem(stringConstants.stgAccount());
        
        Button btnAdd = new Button("Track Again");
        grid.setWidget(1, 1, btnAdd);
        btnAdd.setWidth("100%");
        
        Label lblTrackNewEvent = new Label("Track New Event");
        grid.setWidget(2, 0, lblTrackNewEvent);
        
        Grid grid_1 = new Grid(5, 3);
        grid.setWidget(3, 0, grid_1);
        
        Label lblHostname = new Label("Hostname");
        grid_1.setWidget(0, 1, lblHostname);
        
        hostnameTextbox = new TextBox();
        hostnameTextbox.setText("germanmaster.traclive.dk");
        hostnameTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateLiveURI();
                updateStoredURI();
                updateJsonUrl();
            }
        });
        grid_1.setWidget(0, 2, hostnameTextbox);
        
        Label lblEventName = new Label("Event name");
        grid_1.setWidget(1, 1, lblEventName);
        
        eventNameTextbox = new TextBox();
        eventNameTextbox.setText("event_2011...");
        eventNameTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateJsonUrl();
            }
        });
        grid_1.setWidget(1, 2, eventNameTextbox);
        
        Label lblLivePort = new Label("Port Live Data");
        grid_1.setWidget(2, 1, lblLivePort);
        
        HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
        grid_1.setWidget(2, 2, horizontalPanel_1);
        
        livePortIntegerbox = new IntegerBox();
        livePortIntegerbox.setText("1520");
        livePortIntegerbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updatePortStoredData();
                updateStoredURI();
                updateLiveURI();
                updateJsonUrl();
            }
        });
        horizontalPanel_1.add(livePortIntegerbox);
        
        Label lblStoredPort = new Label("Port Stored Data");
        horizontalPanel_1.add(lblStoredPort);
        
        storedPortIntegerbox = new IntegerBox();
        storedPortIntegerbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateStoredURI();
            }
        });
        horizontalPanel_1.add(storedPortIntegerbox);
        
        Label lblJsonUrl = new Label("JSON URL");
        grid_1.setWidget(3, 1, lblJsonUrl);
        
        jsonURLBox = new TextBox();
        grid_1.setWidget(3, 2, jsonURLBox);
        jsonURLBox.setVisibleLength(80);
        
        Label lblLiveUri = new Label("Live URI");
        grid_1.setWidget(4, 1, lblLiveUri);
        
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        grid_1.setWidget(4, 2, horizontalPanel);
        
        liveURIBox = new TextBox();
        liveURIBox.setVisibleLength(30);
        horizontalPanel.add(liveURIBox);
        
        Label lblStoredUri = new Label("Stored URI");
        horizontalPanel.add(lblStoredUri);
        horizontalPanel.setCellVerticalAlignment(lblStoredUri, HasVerticalAlignment.ALIGN_MIDDLE);
        
        storedURIBox = new TextBox();
        storedURIBox.setVisibleLength(30);
        horizontalPanel.add(storedURIBox);
        
        Label lblEventsConnectedTo = new Label("Events Currently Tracked");
        grid.setWidget(5, 0, lblEventsConnectedTo);
        
        Cell<EventDAO> eventCell = new AbstractCell<EventDAO>() {
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context, EventDAO value, SafeHtmlBuilder sb) {
                sb.appendEscaped(value.name);
            }
        };
        eventsList = new CellList<EventDAO>(eventCell);
        grid.setWidget(6, 0, eventsList);
        grid.getCellFormatter().setHeight(6, 0, "100%");
        eventsList.setWidth("100%");
        eventsList.setVisibleRange(0, 5);
        
        Button btnRemove = new Button("Remove");
        grid.setWidget(6, 1, btnRemove);
        btnRemove.setWidth("100%");
        grid.getCellFormatter().setVerticalAlignment(6, 1, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(6, 0, HasVerticalAlignment.ALIGN_TOP);
        
        fillEvents();
        updatePortStoredData();
        updateLiveURI();
        updateStoredURI();
        updateJsonUrl();
    }
    
    private void updatePortStoredData() {
        storedPortIntegerbox.setValue(livePortIntegerbox.getValue()+1);
    }
    
    private void updateLiveURI() {
        liveURIBox.setValue("tcp://"+hostnameTextbox.getValue()+":"+livePortIntegerbox.getValue());
    }

    private void updateStoredURI() {
        storedURIBox.setValue("tcp://"+hostnameTextbox.getValue()+":"+storedPortIntegerbox.getValue());
    }
    
    private void updateJsonUrl() {
        jsonURLBox.setValue("http://"+hostnameTextbox.getValue()+"/events/"+eventNameTextbox.getValue()+"/jsonservice.php");
    }

    private void fillEvents() {
        sailingService.listEvents(new AsyncCallback<EventDAO[]>() {
            @Override
            public void onSuccess(EventDAO[] result) {
                eventsList.setRowCount(result.length);
                eventsList.setRowData(Arrays.asList(result));
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call listEvents() - Failure");
            }
        });
    }
    
}
