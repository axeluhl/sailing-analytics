package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;


public class TrackedEventsComposite extends FormPanel implements EventDisplayer, RaceSelectionProvider {
    private final Set<RaceSelectionChangeListener> raceSelectionChangeListeners;

    private final boolean multiSelection;

    private boolean dontFireNextSelectionChangeEvent;

    private final SelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>> selectionModel;

    private CellTable<Triple<EventDAO, RegattaDAO, RaceDAO>> raceTable;
    
    private ListBox regattasComboBox = null;

    private ListBox eventsComboBox = null;

    private ListDataProvider<Triple<EventDAO, RegattaDAO, RaceDAO>> raceList;
    
    private VerticalPanel panel;

    private DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer();
    
    public TrackedEventsComposite(StringConstants stringConstants, boolean multiSelection) {
        this.multiSelection = multiSelection;
        this.raceSelectionChangeListeners = new HashSet<RaceSelectionChangeListener>();
        
        selectionModel = multiSelection ? new MultiSelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>>() : new SingleSelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>>();

        Label label = new Label("NEW TRACKED RACES VIEW: " + stringConstants.noRacesYet());
        label.setWordWrap(false);
        setWidget(label);
    }

    @Override
    public List<Triple<EventDAO, RegattaDAO, RaceDAO>> getSelectedEventAndRace() {
        List<Triple<EventDAO, RegattaDAO, RaceDAO>> result = new ArrayList<Triple<EventDAO,RegattaDAO,RaceDAO>>();

        if (raceList != null) {
            for (Triple<EventDAO, RegattaDAO, RaceDAO> raceTriple : raceList.getList()) {
                if (multiSelection && selectionModel.isSelected(raceTriple)) {
                    result.add(raceTriple);
                }
            }
        }

        return result;
    }
    
    public void selectRaceByName(String eventName, String raceName) {
        if (raceList != null) {
            for (Triple<EventDAO, RegattaDAO, RaceDAO> raceTriple : raceList.getList()) {
                EventDAO event = raceTriple.getA();
                RaceDAO race = raceTriple.getC();

                if (event.name.equals(eventName) && race.name.equals(raceName)) {
                    dontFireNextSelectionChangeEvent = true;
                    selectionModel.setSelected(raceTriple, true);
                    break;
                }
            }
        }
    }
    
    public void clearSelection() { 
        if (raceList != null) {
            for (Triple<EventDAO, RegattaDAO, RaceDAO> raceTriple : raceList.getList()) {
                selectionModel.setSelected(raceTriple, false);
            }
        }
    }

    @Override
    public void addRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        raceSelectionChangeListeners.add(listener);
    }

    @Override
    public void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        raceSelectionChangeListeners.remove(listener);
    }

    private void fireRaceSelectionChanged(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        if (dontFireNextSelectionChangeEvent) {
            dontFireNextSelectionChangeEvent = false;
        } else {
            for (RaceSelectionChangeListener listener : raceSelectionChangeListeners) {
                listener.onRaceSelectionChange(selectedRaces);
            }
        }
    }

    @Override
    public void fillEvents(List<EventDAO> events) {
        if(raceTable == null)
        {
            panel = new VerticalPanel();
/*
            HorizontalPanel regattaFilterPanel = new HorizontalPanel();
            panel.add(regattaFilterPanel);
            
            Label regattaFilterLabel = new Label("Filter by regatta: ");
            regattaFilterLabel.setWordWrap(false);
            regattaFilterPanel.add(regattaFilterLabel);

            regattasComboBox = new ListBox();
            regattasComboBox.addItem("All Regattas");
            
            regattaFilterPanel.add(regattasComboBox);
*/            
            AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
            raceTable = new CellTable<Triple<EventDAO, RegattaDAO, RaceDAO>>(/* pageSize */ 200, tableRes);
            
            TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> eventNameColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
                @Override
                public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                    return object.getA().name;
                }
            };

            TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> regattaNameColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
                @Override
                public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                    return object.getB().boatClass.name;
                }
            };

            TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> raceNameColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
                @Override
                public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                    return object.getC().name;
                }
            };

            TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> raceStartColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
                @Override
                public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                    if(object.getC().startOfRace != null) {
                        return dateFormatter.render(object.getC().startOfRace);
                    }
                    
                    return "";
                }
            };

            TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> raceTrackedColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
                @Override
                public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                    if(object.getC().currentlyTracked == true)
                        return "tracked";
                    
                    return "";
                }
            };

            raceTable.addColumn(eventNameColumn, "Event");
            raceTable.addColumn(regattaNameColumn, "Regatta");
            raceTable.addColumn(raceNameColumn, "Race");
            raceTable.addColumn(raceStartColumn, "Start time");
            raceTable.addColumn(raceTrackedColumn, "Tracked");
            raceTable.setWidth("300px");
            raceTable.setSelectionModel(selectionModel);
    
            panel.add(raceTable);
            
            raceList = new ListDataProvider<Triple<EventDAO, RegattaDAO, RaceDAO>>();
            raceList.addDataDisplay(raceTable);
            
            raceTable.getSelectionModel().addSelectionChangeHandler(new Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    fireRaceSelectionChanged(getSelectedEventAndRace());
                }
            });
            setWidget(panel);
        }
        
        
        if(events.size() == 0)
            raceTable.setVisible(false);
        else
            raceTable.setVisible(true);
            

        raceList.getList().clear();
        // Handler columnSortHandler = getRaceTableColumnSortHandler(raceList.getList(), raceNameColumn, raceStartTrackingColumn);
        // raceTable.addColumnSortHandler(columnSortHandler);
        
        for (EventDAO event : events) {
            for (RegattaDAO regatta : event.regattas) {
                for (RaceDAO race : regatta.races) {
                    raceList.getList().add(new Triple<EventDAO, RegattaDAO, RaceDAO>(event, regatta, race));
                }
            }
        }
    }

}
