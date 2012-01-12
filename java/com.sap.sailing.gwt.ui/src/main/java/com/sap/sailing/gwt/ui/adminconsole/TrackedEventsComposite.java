package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringConstants;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.server.api.EventNameAndRaceName;

/**
 * Shows the 
 */
public class TrackedEventsComposite extends FormPanel implements EventDisplayer, RaceSelectionProvider {
    private final Set<RaceSelectionChangeListener> raceSelectionChangeListeners;

    private final Set<TrackedRaceChangedListener> raceIsTrackedRaceChangeListener;

    private final boolean multiSelection;

    private boolean dontFireNextSelectionChangeEvent;

    private final SelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>> selectionModel;

    private CellTable<Triple<EventDAO, RegattaDAO, RaceDAO>> raceTable;

    private ListDataProvider<Triple<EventDAO, RegattaDAO, RaceDAO>> raceList;

    private VerticalPanel panel;

    private DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    private DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));

    private Label noTrackedRacesLabel = null;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final EventRefresher eventRefresher;

    private Button btnUntrack = null;
    private Button btnRefresh = null;
    private Button btnRemoveRace = null;

    private TextBox filterRacesTextbox;

    private List<Triple<EventDAO, RegattaDAO, RaceDAO>> availableRaceList;

    private Triple<EventDAO, RegattaDAO, RaceDAO> lastSelectedTriple;

    public TrackedEventsComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final EventRefresher eventRefresher, StringConstants stringConstants, boolean hasMultiSelection) {
        if (eventRefresher == null) {
            throw new IllegalArgumentException("eventRefresher must not be null");
        }
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.eventRefresher = eventRefresher;
        this.multiSelection = hasMultiSelection;
        this.raceSelectionChangeListeners = new HashSet<RaceSelectionChangeListener>();
        this.raceIsTrackedRaceChangeListener = new HashSet<TrackedRaceChangedListener>();
        this.availableRaceList = new ArrayList<Triple<EventDAO, RegattaDAO, RaceDAO>>();
        this.lastSelectedTriple = null;
        raceList = new ListDataProvider<Triple<EventDAO, RegattaDAO, RaceDAO>>();
        selectionModel = multiSelection ? new MultiSelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>>()
                : new SingleSelectionModel<Triple<EventDAO, RegattaDAO, RaceDAO>>();
        panel = new VerticalPanel();
        setWidget(panel);
        HorizontalPanel filterPanel = new HorizontalPanel();
        panel.add(filterPanel);
        Label lblFilterEvents = new Label(stringConstants.filterRacesByName() + ":");
        filterPanel.setSpacing(5);
        filterPanel.add(lblFilterEvents);
        filterPanel.setCellVerticalAlignment(lblFilterEvents, HasVerticalAlignment.ALIGN_MIDDLE);
        filterRacesTextbox = new TextBox();
        filterRacesTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fillRaceListFromAvailableRacesApplyingFilter();
            }
        });
        filterPanel.add(filterRacesTextbox);

        noTrackedRacesLabel = new Label(stringConstants.noRacesYet());
        noTrackedRacesLabel.setWordWrap(false);
        panel.add(noTrackedRacesLabel);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        raceTable = new CellTable<Triple<EventDAO, RegattaDAO, RaceDAO>>(/* pageSize */200, tableRes);
        ListHandler<Triple<EventDAO, RegattaDAO, RaceDAO>> columnSortHandler = new ListHandler<Triple<EventDAO, RegattaDAO, RaceDAO>>(
                raceList.getList());
        TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> eventNameColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
            @Override
            public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                return object.getA().name;
            }
        };
        eventNameColumn.setSortable(true);

        columnSortHandler.setComparator(eventNameColumn, new Comparator<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
            @Override
            public int compare(Triple<EventDAO, RegattaDAO, RaceDAO> t1, Triple<EventDAO, RegattaDAO, RaceDAO> t2) {
                EventDAO eventOne = t1.getA();
                EventDAO eventTwo = t2.getA();
                boolean ascending = isSortedAscending();
                if (eventOne.name.equals(eventTwo.name)) {
                    return 0;
                }
                int val = -1;
                val = (eventOne != null && eventTwo != null && ascending) ? (eventOne.name.compareTo(eventTwo.name))
                        : -(eventTwo.name.compareTo(eventOne.name));
                return val;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = raceTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
            }
        });

        TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> regattaNameColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {

            @Override
            public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                return object.getB().boatClass.name;
            }
        };
        regattaNameColumn.setSortable(true);

        columnSortHandler.setComparator(regattaNameColumn, new Comparator<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
            @Override
            public int compare(Triple<EventDAO, RegattaDAO, RaceDAO> t1, Triple<EventDAO, RegattaDAO, RaceDAO> t2) {
                RegattaDAO regattaOne = t1.getB();
                RegattaDAO regattaTwo = t2.getB();
                boolean ascending = isSortedAscending();
                if (regattaOne.boatClass.name.equals(regattaTwo.boatClass.name)) {
                    return 0;
                }
                int val = -1;
                val = (regattaOne != null && regattaTwo != null && ascending) ? (regattaOne.boatClass.name
                        .compareTo(regattaTwo.boatClass.name)) : -(regattaTwo.boatClass.name
                        .compareTo(regattaOne.boatClass.name));
                return val;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = raceTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
            }
        });

        TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> raceNameColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {

            @Override
            public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                return object.getC().name;
            }
        };
        raceNameColumn.setSortable(true);

        columnSortHandler.setComparator(raceNameColumn, new Comparator<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
            @Override
            public int compare(Triple<EventDAO, RegattaDAO, RaceDAO> t1, Triple<EventDAO, RegattaDAO, RaceDAO> t2) {
                boolean ascending = isSortedAscending();
                if (t1.getC().name.equals(t2.getC().name)) {
                    return 0;
                }
                int val = -1;
                val = (t1 != null && t2 != null && ascending) ? (t1.getC().name.compareTo(t2.getC().name)) : -(t2
                        .getC().name.compareTo(t1.getC().name));
                return val;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = raceTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
            }
        });

        TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> raceStartColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
            @Override
            public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                if (object.getC().startOfRace != null) {
                    return dateFormatter.render(object.getC().startOfRace) + " "
                            + timeFormatter.render(object.getC().startOfRace);
                }

                return "";
            }
        };
        raceStartColumn.setSortable(true);

        columnSortHandler.setComparator(raceStartColumn, new Comparator<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
            @Override
            public int compare(Triple<EventDAO, RegattaDAO, RaceDAO> t1, Triple<EventDAO, RegattaDAO, RaceDAO> t2) {
                if (t1.getC().startOfRace != null && t2.getC().startOfRace != null) {
                    boolean ascending = isSortedAscending();
                    int val = -1;
                    val = (t1.getC().startOfRace.after(t2.getC().startOfRace) && ascending) ? 1 : -1;
                    return val;
                }
                return 0;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = raceTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
            }
        });

        TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>> raceTrackedColumn = new TextColumn<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
            @Override
            public String getValue(Triple<EventDAO, RegattaDAO, RaceDAO> object) {
                if (object.getC().currentlyTracked == true)
                    return "tracked";

                return "";
            }
        };

        raceTable.addColumn(eventNameColumn, stringConstants.event());
        raceTable.addColumn(regattaNameColumn, stringConstants.regatta());
        raceTable.addColumn(raceNameColumn, stringConstants.race());
        raceTable.addColumn(raceStartColumn, stringConstants.startTime());
        raceTable.addColumn(raceTrackedColumn, stringConstants.tracked());
        raceTable.setWidth("300px");
        raceTable.setSelectionModel(selectionModel);
        raceTable.setVisible(false);
        panel.add(raceTable);
        raceList.addDataDisplay(raceTable);
        raceTable.addColumnSortHandler(columnSortHandler);
        raceTable.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedEventAndRace = getSelectedEventAndRace();
                if (selectedEventAndRace.isEmpty()) {
                    lastSelectedTriple = null;
                    btnRemoveRace.setEnabled(false);
                    btnUntrack.setEnabled(false);
                } else {
                    lastSelectedTriple = selectedEventAndRace.get(0);
                    btnRemoveRace.setEnabled(true);
                    btnUntrack.setEnabled(true);
                }
                fireRaceSelectionChanged(selectedEventAndRace);
            }
        });
        HorizontalPanel trackedRacesButtonPanel = new HorizontalPanel();
        trackedRacesButtonPanel.setSpacing(10);
        panel.add(trackedRacesButtonPanel);
        btnRemoveRace = new Button(stringConstants.remove());
        btnRemoveRace.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (Triple<EventDAO, RegattaDAO, RaceDAO> selection : getSelectedEventAndRace()) {
                    removeAndUntrackRace(selection.getA(), selection.getC());
                }
            }
        });
        btnRemoveRace.setEnabled(false);
        trackedRacesButtonPanel.add(btnRemoveRace);
        btnUntrack = new Button(stringConstants.stopTracking());
        btnUntrack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent click) {
                for (Triple<EventDAO, RegattaDAO, RaceDAO> selection : getSelectedEventAndRace()) {
                    if (selection.getC().currentlyTracked) {
                        stopTrackingRace(selection.getA(), selection.getC());
                    }
                }
            }
        });
        btnUntrack.setEnabled(false);
        trackedRacesButtonPanel.add(btnUntrack);

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
        List<Triple<EventDAO, RegattaDAO, RaceDAO>> result = new ArrayList<Triple<EventDAO, RegattaDAO, RaceDAO>>();
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
        if (events.isEmpty()) {
            raceTable.setVisible(false);
            btnUntrack.setVisible(false);
            btnRemoveRace.setVisible(false);
            noTrackedRacesLabel.setVisible(true);
        } else {
            raceTable.setVisible(true);
            btnUntrack.setVisible(true);
            btnUntrack.setEnabled(false);
            btnRemoveRace.setVisible(true);
            btnRemoveRace.setEnabled(false);
            noTrackedRacesLabel.setVisible(false);
        }
        availableRaceList.clear();
        for (EventDAO event : events) {
            for (RegattaDAO regatta : event.regattas) {
                for (RaceDAO race : regatta.races) {
                    Triple<EventDAO, RegattaDAO, RaceDAO> triple = new Triple<EventDAO, RegattaDAO, RaceDAO>(event,
                            regatta, race);
                    if (race != null) {
                        availableRaceList.add(triple);
                    }
                }
            }
        }
        fillRaceListFromAvailableRacesApplyingFilter();
        if (lastSelectedTriple != null) {
            selectRaceByName(lastSelectedTriple.getA().name, lastSelectedTriple.getC().name);
        }
    }

    public void addTrackedRaceChangeListener(TrackedRaceChangedListener listener) {
        this.raceIsTrackedRaceChangeListener.add(listener);
    }

    private void stopTrackingRace(final EventDAO event, final RaceDAO race) {
        final EventNameAndRaceName eventNameAndRaceName = new EventNameAndRaceName(event.name, race.name);
        sailingService.stopTrackingRace(eventNameAndRaceName, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Exception trying to stop tracking race " + race.name + "in event "
                        + event.name + ": " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                eventRefresher.fillEvents();
                for (TrackedRaceChangedListener listener : raceIsTrackedRaceChangeListener) {
                    listener.changeTrackingRace(eventNameAndRaceName, false);
                }
            }
        });
    }

    private void removeAndUntrackRace(final EventDAO event, final RaceDAO race) {
        final EventNameAndRaceName eventNameAndRaceName = new EventNameAndRaceName(event.name, race.name);
        sailingService.removeAndUntrackRace(eventNameAndRaceName,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Exception trying to stop tracking race " + race.name + "in event "
                                + event.name + ": " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        eventRefresher.fillEvents();
                        for (TrackedRaceChangedListener listener : raceIsTrackedRaceChangeListener) {
                            listener.changeTrackingRace(eventNameAndRaceName, false);
                        }
                    }
                });
    }

    private void fillRaceListFromAvailableRacesApplyingFilter() {
        String text = filterRacesTextbox.getText();
        List<String> wordsToFilter = Arrays.asList(text.split(" "));
        raceList.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (Triple<EventDAO, RegattaDAO, RaceDAO> triple : availableRaceList) {
                boolean failed = false;
                for (String word : wordsToFilter) {
                    String textAsUppercase = word.toUpperCase().trim();
                    if (!triple.getA().name.toUpperCase().contains(textAsUppercase)
                            && !triple.getB().boatClass.name.toUpperCase().contains(textAsUppercase)
                            && !triple.getC().name.toUpperCase().contains(textAsUppercase)) {
                        failed = true;
                        break;
                    }
                }
                if (!failed) {
                    raceList.getList().add(triple);
                }
            }
        } else {
            raceList.getList().addAll(availableRaceList);
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(raceTable, raceTable.getColumnSortList());
    }

}
