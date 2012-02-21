package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

/**
 * 
 * @author Lennart Hensler (D054527)
 * 
 */
public class LeaderboardGroupOverviewPanel extends AbstractEventPanel {
    
    private TextBox locationTextBox;
    private TextBox nameTextBox;
    private TextBox fromTextBox;
    private TextBox untilTextBox;
    private CheckBox onlyLiveCheckBox;

    public LeaderboardGroupOverviewPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, final StringMessages stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("95%");

        // Build search GUI
        CaptionPanel searchCaptionPanel = new CaptionPanel(stringConstants.searchEvents());
        searchCaptionPanel.setWidth("100%");
        mainPanel.add(searchCaptionPanel);
        
        HorizontalPanel searchFunctionalPanel = new HorizontalPanel();
        searchCaptionPanel.add(searchFunctionalPanel);
        searchFunctionalPanel.setWidth("100%");
        
        Label locationLbl = new Label(stringConstants.location() + ":");
        searchFunctionalPanel.add(locationLbl);
        locationTextBox = new TextBox();
        locationTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchFunctionalPanel.add(locationTextBox);
        
        Label nameLbl = new Label(stringConstants.name() + ":");
        searchFunctionalPanel.add(nameLbl);
        nameTextBox = new TextBox();
        nameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchFunctionalPanel.add(nameTextBox);
        
        onlyLiveCheckBox = new CheckBox(stringConstants.onlyLiveEvents());
        onlyLiveCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                onCheckBoxLiveChange();
            }
        });
        searchFunctionalPanel.add(onlyLiveCheckBox);
        
        Label fromDateLbl = new Label(stringConstants.from() + ":");
        searchFunctionalPanel.add(fromDateLbl);
        fromTextBox = new TextBox();
        fromTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchFunctionalPanel.add(fromTextBox);
        
        Label toDateLbl = new Label(stringConstants.until() + ":");
        searchFunctionalPanel.add(toDateLbl);
        untilTextBox = new TextBox();
        untilTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchFunctionalPanel.add(untilTextBox);
        
        //Build group GUI TODO
        
        //Build group details GUI TODO
        
        //Loading the data TODO
        
        //Set checkbox as true, because we can't search for old events right now
        //TODO Remove after searching for old events is possible
        onlyLiveCheckBox.setValue(true, true);
        onlyLiveCheckBox.setEnabled(false);
        //Until here
    }

    private void loadEvents() {
        //TODO
    }
    
    private void refreshEventsTable() {
        //TODO
    }
    
    private void groupSelectionChanged() {
        //TODO
    }
    
    private void loadGroupDetails(LeaderboardGroupDTO group) {
        //TODO
    }
    
    private void onCheckBoxLiveChange() {
        if (onlyLiveCheckBox.getValue()) {
            String today = DateTimeFormat.getFormat("dd.MM.yyyy").format(new Date()).toString();
            
            fromTextBox.setText(today);
            fromTextBox.setEnabled(false);
            untilTextBox.setText(today);
            untilTextBox.setEnabled(false);
        } else {
            fromTextBox.setText("");
            fromTextBox.setEnabled(true);
            untilTextBox.setText("");
            untilTextBox.setEnabled(true);
        }
        onSearchCriteriaChange();
    }
    
    private void onSearchCriteriaChange() {
        //Get search criteria
        String location = locationTextBox.getText();
        String name = nameTextBox.getText();
        boolean onlyLive = onlyLiveCheckBox.getValue();
        Date from = null;
        Date until = null;
        try {
            from = DateTimeFormat.getFormat("dd.MM.yyyy").parse(fromTextBox.getText());
            until = DateTimeFormat.getFormat("dd.MM.yyyy").parse(untilTextBox.getText());
            //Adding 24 hours to until, so that it doesn't result in an empty table if 'from' and 'until' are equal.
            //Instead you'll have a 24 hour range
            long time = until.getTime() + 24 * 60 * 60 * 1000;
            until = new Date(time);
        } catch (IllegalArgumentException e) {}
        //Filter list by criteria TODO
//        eventsTableProvider.getList().clear();
//        for (EventDTO event : availableEvents) {
//            if (checkSearchCriteria(event, location, name, onlyLive, from, until)) {
//                eventsTableProvider.getList().add(event);
//            }
//        }
//        //Now sort again according to selected criterion
//        ColumnSortEvent.fire(eventsTable, eventsTable.getColumnSortList());
    }
    
    private boolean checkSearchCriteria(LeaderboardGroupDTO forGroup, String location, String name, boolean onlyLive, Date from, Date until) {
        boolean result = true;
        //TODO
        if (result && !location.equals("")) {
//            result = !textContainingStringsToCheck(Arrays.asList(location.split("\\s")), forEvent.locations);
        }
        if (result && !name.equals("")) {
//            result = !textContainingStringsToCheck(Arrays.asList(name.split("\\s")), forEvent.name);
        }
        //If only live events are allowed the check of the dates isn't needed
        if (result && onlyLive) {
//            result = forEvent.currentlyTracked();
        } else if (result) {
//            Date startDate = forEvent.getStartDate();
//            if (from != null && until != null) {
//                result = from.before(startDate) && until.after(startDate); 
//            } else if (from != null) {
//                result = from.before(startDate);
//            } else if (until != null) {
//                result = until.after(startDate); 
//            }
        }
        
        return result;
    }
    
    @Override
    public void fillEvents(List<EventDTO> result) {
        // TODO Auto-generated method stub
        
    }

}
