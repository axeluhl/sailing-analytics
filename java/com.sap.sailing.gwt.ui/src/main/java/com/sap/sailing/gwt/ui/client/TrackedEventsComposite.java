package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.EventNameAndRaceName;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.Triple;


public class TrackedEventsComposite extends FormPanel implements EventDisplayer, RaceSelectionProvider {
    private final Set<RaceSelectionChangeListener> raceSelectionChangeListeners;

    private final boolean multiSelection;

    private boolean dontFireNextSelectionChangeEvent;

    private final SelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>> selectionModel;

    private CellTable<Triple<EventDAO, RegattaDAO, RaceDAO>> raceTable;
    
    private ListDataProvider<Triple<EventDAO, RegattaDAO, RaceDAO>> raceList;
    
    private VerticalPanel panel;

    private DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    private DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));
    
    private Label noTrackedRacesLabel = null;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final EventRefresher eventRefresher;

    private Button btnRemove = null;
    private Button btnRefresh = null;
    
    private TextBox filterRacesTextbox;
    
    private List<Triple<EventDAO, RegattaDAO, RaceDAO>> availableRaceList;
    
    public TrackedEventsComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, final EventRefresher eventRefresher, 
            StringConstants stringConstants, boolean hasMultiSelection) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.eventRefresher = eventRefresher;
        this.multiSelection = hasMultiSelection;
        this.raceSelectionChangeListeners = new HashSet<RaceSelectionChangeListener>();
        this.availableRaceList = new ArrayList<Triple<EventDAO,RegattaDAO,RaceDAO>>();
        
        selectionModel = multiSelection ? new MultiSelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>>() : new SingleSelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>>();

        panel = new VerticalPanel();
        setWidget(panel);
        
        filterRacesTextbox = new TextBox();
        filterRacesTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String text = filterRacesTextbox.getText();

                raceList.getList().clear();
                
                if(text == null || text.isEmpty()) {
                    raceList.getList().addAll(availableRaceList);
                } else {
                    String textAsUppercase = text.toUpperCase();
                    for (Triple<EventDAO, RegattaDAO, RaceDAO> triple : availableRaceList) {
                        if(triple.getC().name != null) {
                            if(triple.getC().name.toUpperCase().contains(textAsUppercase))
                                raceList.getList().add(triple);
                        }
                    }
                }
            }
        });
        panel.add(filterRacesTextbox);
        
        noTrackedRacesLabel = new Label(stringConstants.noRacesYet());
        noTrackedRacesLabel.setWordWrap(false);
        panel.add(noTrackedRacesLabel);

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
                    return dateFormatter.render(object.getC().startOfRace) + " " + timeFormatter.render(object.getC().startOfRace);
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

        raceTable.setVisible(false);
        panel.add(raceTable);
        
        raceList = new ListDataProvider<Triple<EventDAO, RegattaDAO, RaceDAO>>();
        raceList.addDataDisplay(raceTable);
        
        raceTable.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                fireRaceSelectionChanged(getSelectedEventAndRace());
            }
        });
        
        
        
        HorizontalPanel trackedRacesButtonPanel = new HorizontalPanel();
        
        trackedRacesButtonPanel.setSpacing(10);
        panel.add(trackedRacesButtonPanel);
        
        btnRemove = new Button("Stop tracking");
//      btnRemove = new Button(stringConstants.remove());
        btnRemove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent click) {
                for (Triple<EventDAO, RegattaDAO, RaceDAO> selection : getSelectedEventAndRace()) {
                    if (selection.getC().currentlyTracked) {
                        stopTrackingRace(selection.getA(), selection.getC());
                    }
                }
            }
        });
        trackedRacesButtonPanel.add(btnRemove);
        
        btnRefresh = new Button(stringConstants.refresh());
        btnRefresh.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventRefresher.fillEvents();
            }
        });
        trackedRacesButtonPanel.add(btnRefresh);
        
    }

    @Override
    public List<Triple<EventDAO, RegattaDAO, RaceDAO>> getSelectedEventAndRace() {
        List<Triple<EventDAO, RegattaDAO, RaceDAO>> result = new ArrayList<Triple<EventDAO,RegattaDAO,RaceDAO>>();

        if (raceList != null) {
            for (Triple<EventDAO, RegattaDAO, RaceDAO> raceTriple : raceList.getList()) {
                if (selectionModel.isSelected(raceTriple)) {
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
        if(events.size() == 0) {
            raceTable.setVisible(false);
            btnRemove.setVisible(false);
            noTrackedRacesLabel.setVisible(true);
        } else {
            raceTable.setVisible(true);
            if(eventRefresher != null) {
                btnRemove.setVisible(true);
            }
            noTrackedRacesLabel.setVisible(false);
        }

        raceList.getList().clear();
        availableRaceList.clear();
        
        // Handler columnSortHandler = getRaceTableColumnSortHandler(raceList.getList(), raceNameColumn, raceStartTrackingColumn);
        // raceTable.addColumnSortHandler(columnSortHandler);
        
        for (EventDAO event : events) {
            for (RegattaDAO regatta : event.regattas) {
                for (RaceDAO race : regatta.races) {
                    Triple<EventDAO, RegattaDAO, RaceDAO> triple = new Triple<EventDAO, RegattaDAO, RaceDAO>(event, regatta, race);
                    if (race != null){
                        availableRaceList.add(triple);
                    }
                    raceList.getList().add(triple);
                }
            }
        }
    }

    private void stopTrackingRace(final EventDAO event, final RaceDAO race) {
        sailingService.stopTrackingRace(new EventNameAndRaceName(event.name, race.name), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Exception trying to stop tracking race " + race.name + "in event "+event.name+": "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                eventRefresher.fillEvents();
            }
        });
    }


}
