package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.NamedDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

/**
 * Allows administrators to manage the structure of a complete event. Each event consists of several substructures like
 * regattas, races, series and groups (big fleets divided into racing groups).
 * 
 * @author Frank Mittag (C5163974)
 * 
 */
public class RegattaStructureManagementPanel extends SimplePanel implements RegattaDisplayer {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final List<EventDTO> events;
    private ListBox eventsComboBox;
    private EventDTO selectedEvent;

    private CaptionPanel eventDetailsCaptionPanel;

    private Label eventVenueLabel;
    private CellTable<RegattaDTO> regattaTable;
    private SingleSelectionModel<RegattaDTO> regattaSelectionModel;
    private ListDataProvider<RegattaDTO> regattaProvider;
    private final RegattaRefresher regattaRefresher;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    private boolean supportEvents = false;

    public RegattaStructureManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, RegattaRefresher regattaRefresher) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;

        events = new ArrayList<EventDTO>();
        selectedEvent = null;

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");

        if(supportEvents) {
            HorizontalPanel eventsPanel = new HorizontalPanel();
            eventsPanel.setSpacing(5);
            mainPanel.add(eventsPanel);

            Label managedEventsLabel = new Label(stringMessages.events() + ":");
            eventsPanel.add(managedEventsLabel);

            eventsComboBox = new ListBox();
            eventsComboBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    onEventSelectionChanged();
                }
            });
            eventsPanel.add(eventsComboBox);

            Button createEventBtn = new Button(stringMessages.add());
            createEventBtn.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    openCreateEventDialog();
                }
            });
            eventsPanel.add(createEventBtn);

            createEventDetailsPanel();
            mainPanel.add(eventDetailsCaptionPanel);
            eventDetailsCaptionPanel.setVisible(false);

            fillEvents();
        } else {
            createEventDetailsPanel();
            mainPanel.add(eventDetailsCaptionPanel);
        }
    }

    private void createEventDetailsPanel() {
        eventDetailsCaptionPanel = new CaptionPanel();
        eventDetailsCaptionPanel.setWidth("95%");

        VerticalPanel eventDetailsPanel = new VerticalPanel();
        eventDetailsPanel.setSpacing(5);
        eventDetailsCaptionPanel.add(eventDetailsPanel);

        eventVenueLabel = new Label("");
        eventDetailsPanel.add(eventVenueLabel);

        createRegattaDetails(eventDetailsPanel);
    }

    private void createRegattaDetails(Panel parentPanel) {
        Button addRegattaBtn = new Button(stringMessages.addRegatta());
        parentPanel.add(addRegattaBtn);
        addRegattaBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateRegattaDialog();
            }
        });

        // regatta table
        TextColumn<RegattaDTO> regattaNameColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.name;
            }
        };

        TextColumn<RegattaDTO> regattaBoatClassColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                final NamedDTO boatClass = regatta.boatClass;
                return boatClass==null?"(null)":boatClass.name;
            }
        };

        TextColumn<RegattaDTO> regattaScoringSystemColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                ScoringSchemeType scoringScheme = regatta.scoringScheme;
                String scoringSystem = scoringScheme == null ? "" : ScoringSchemeTypeFormatter.format(scoringScheme, stringMessages);               
                return scoringSystem;
            }
        };
        
        final SafeHtmlCell seriesCell = new SafeHtmlCell();
        Column<RegattaDTO, SafeHtml> regattaSeriesColumn = new Column<RegattaDTO, SafeHtml>(seriesCell) {
            @Override
            public SafeHtml getValue(RegattaDTO regatta) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                int seriesCount = regatta.series.size();
                int i = 1;
                for (SeriesDTO serie : regatta.series) {
                    builder.appendEscaped(i + ". " + serie.name);
                    if (serie.isMedal()) {
                        builder.appendEscaped(" (" + stringMessages.medalSeries() + ")");
                    }
                    if (i < seriesCount) {
                        builder.appendHtmlConstant("<br>");
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };

        final SafeHtmlCell regattaRacesCell = new SafeHtmlCell();
        Column<RegattaDTO, SafeHtml> regattaRacesColumn = new Column<RegattaDTO, SafeHtml>(regattaRacesCell) {
            @Override
            public SafeHtml getValue(RegattaDTO regatta) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();

                int seriesCount = regatta.series.size();
                int i = 1;
                for (SeriesDTO serie : regatta.series) {
                    int raceColumnsCount = serie.getRaceColumns().size();
                    int j = 1;
                    if(!serie.getRaceColumns().isEmpty()) {
                        for(RaceColumnDTO raceColumn: serie.getRaceColumns()) {
                            builder.appendEscaped(j + ". " + raceColumn.getRaceColumnName());
                            if(j < raceColumnsCount) {
                                builder.appendEscaped(", ");
                            }
                            j++;
                        }
                    } else {
                        builder.appendEscaped(stringMessages.noRacesYet());
                    }
                    if(i < seriesCount) {
                        builder.appendHtmlConstant("<br>");
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };

        final SafeHtmlCell fleetsCell = new SafeHtmlCell();
        Column<RegattaDTO, SafeHtml> regattaFleetsColumn = new Column<RegattaDTO, SafeHtml>(fleetsCell) {
            @Override
            public SafeHtml getValue(RegattaDTO regatta) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                int seriesCount = regatta.series.size();
                int i = 1;
                for (SeriesDTO serie : regatta.series) {
                    int fleetsCount = serie.getFleets().size();
                    int j = 1;
                    for(FleetDTO fleet: serie.getFleets()) {
                        builder.appendEscaped(j + ". " + fleet.name);
                        builder.appendEscaped(" (" + fleet.getOrderNo() + ") ");
                        if (j < fleetsCount) {
                            builder.appendEscaped(", ");
                        }
                        j++;
                    }
                    if(i < seriesCount) {
                        builder.appendHtmlConstant("<br>");
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };

        ImagesBarColumn<RegattaDTO, RegattaConfigImagesBarCell> regattaActionColumn = new ImagesBarColumn<RegattaDTO, RegattaConfigImagesBarCell>(
                new RegattaConfigImagesBarCell(stringMessages));
        regattaActionColumn.setFieldUpdater(new FieldUpdater<RegattaDTO, String>() {
            @Override
            public void update(int index, RegattaDTO regatta, String value) {
                if (RegattaConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveRegatta(regatta.name))) {
                        removeRegatta(regatta);
                    }
                } else if (RegattaConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRacesOfRegattaSeries(regatta);
                }
            }
        });
        
        regattaTable = new CellTable<RegattaDTO>(10000, tableRes);
        regattaTable.setWidth("100%");
        regattaTable.addColumn(regattaNameColumn, stringMessages.regattaName());
        regattaTable.addColumn(regattaBoatClassColumn, stringMessages.boatClass());
        regattaTable.addColumn(regattaScoringSystemColumn, stringMessages.scoringSystem());
        regattaTable.addColumn(regattaSeriesColumn, stringMessages.series());
        regattaTable.addColumn(regattaRacesColumn, stringMessages.races());
        regattaTable.addColumn(regattaFleetsColumn, stringMessages.fleets());
        regattaTable.addColumn(regattaActionColumn, stringMessages.actions());
        
        regattaSelectionModel = new SingleSelectionModel<RegattaDTO>();
        regattaSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
            }
        });
        regattaTable.setSelectionModel(regattaSelectionModel);

        regattaProvider = new ListDataProvider<RegattaDTO>();
        regattaProvider.addDataDisplay(regattaTable);
        parentPanel.add(regattaTable);
    }
    
    private void editRacesOfRegattaSeries(final RegattaDTO regatta) {
        RaceColumnInRegattaSeriesDialog raceDialog = new RaceColumnInRegattaSeriesDialog(regatta, stringMessages, 
                new AsyncCallback<Pair<SeriesDTO, List<RaceColumnDTO>>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(final Pair<SeriesDTO, List<RaceColumnDTO>> result) {
                        updateRacesOfRegattaSeries(regatta, result.getA(), result.getB());
                    }
                });
        raceDialog.show();
    }

    private void updateRacesOfRegattaSeries(final RegattaDTO regatta, final SeriesDTO series, List<RaceColumnDTO> newRaceColumns) {
        final RegattaIdentifier regattaIdentifier = new RegattaName(regatta.name);
        
        List<RaceColumnDTO> existingRaceColumns = series.getRaceColumns();
        final List<String> raceColumnsToAdd = new ArrayList<String>();
        final List<String> raceColumnsToRemove = new ArrayList<String>();
        
        for(RaceColumnDTO newRaceColumn: newRaceColumns) {
            if(!existingRaceColumns.contains(newRaceColumn)) {
                raceColumnsToAdd.add(newRaceColumn.name);
            }
        }

        for(RaceColumnDTO existingRaceColumn: existingRaceColumns) {
            if(!newRaceColumns.contains(existingRaceColumn)) {
                raceColumnsToRemove.add(existingRaceColumn.name);
            }
        }

        sailingService.addColumnsToSeries(regattaIdentifier, series.name, raceColumnsToAdd, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to add race columns "
                            + raceColumnsToAdd + " to series " + series.name
                            + ": " + caught.getMessage());

                }

                @Override
                public void onSuccess(Void v) {
                    regattaRefresher.fillRegattas();
                }
            });
        
        sailingService.removeColumnsFromSeries(regattaIdentifier, series.name, raceColumnsToRemove, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to remove race columns "
                        + raceColumnsToAdd + " from series " + series.name
                        + ": " + caught.getMessage());

            }

            @Override
            public void onSuccess(Void v) {
                regattaRefresher.fillRegattas();
            }
        });
    }
    
    private void removeRegatta(final RegattaDTO regatta) {
        final RegattaIdentifier regattaIdentifier = new RegattaName(regatta.name);
        sailingService.removeRegatta(regattaIdentifier, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to remove regatta " + regatta.name + ": " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                regattaRefresher.fillRegattas();
            }
        });
    }
    
    private void onEventSelectionChanged() {
        int selIndex = eventsComboBox.getSelectedIndex();
        String selItemText = eventsComboBox.getItemText(selIndex);

        for (EventDTO eventDTO : events) {
            if (eventDTO.name.equals(selItemText)) {
                selectedEvent = eventDTO;
                eventDetailsCaptionPanel.setVisible(true);
                updateEventDetails();
                break;
            }
        }
    }

    private void openCreateEventDialog() {
        EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(events), stringMessages,
                new AsyncCallback<EventDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                    }

                    @Override
                    public void onSuccess(EventDTO newEvent) {
                        createNewEvent(newEvent);
                    }
                });
        dialog.show();
    }

    private void openCreateRegattaDialog() {
        Collection<RegattaDTO> existingRegattas = null;
        if(supportEvents) {
            existingRegattas = Collections.unmodifiableCollection(selectedEvent.regattas);
        } else {
            existingRegattas = Collections.unmodifiableCollection(regattaProvider.getList());
        }
        
        RegattaWithSeriesAndFleetsCreateDialog dialog = new RegattaWithSeriesAndFleetsCreateDialog(existingRegattas, stringMessages,
                new AsyncCallback<RegattaDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                    }

                    @Override
                    public void onSuccess(RegattaDTO newRegatta) {
                        createNewRegatta(newRegatta);
                    }
                });
        dialog.show();
    }

    private void updateEventDetails() {
        if (selectedEvent != null) {
            eventDetailsCaptionPanel.setCaptionText(selectedEvent.name);
            eventVenueLabel.setText(stringMessages.venue() + ": " + selectedEvent.venue.name);
            
            // load the regattas for this event
            sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
                @Override
                public void onFailure(Throwable t) {
                    errorReporter.reportError("Error trying to read regattas of event " + selectedEvent.name + ": " + t.getMessage());
                }

                @Override
                public void onSuccess(List<RegattaDTO> regattas) {
                    regattaProvider.getList().clear();
                    regattaProvider.getList().addAll(regattas);
                }
            });
        }
    }

    private void createNewEvent(final EventDTO newEvent) {
        sailingService.createEvent(newEvent.name, newEvent.venue.name, new AsyncCallback<EventDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new event" + newEvent.name + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(EventDTO newEvent) {
                events.add(newEvent);
                eventsComboBox.addItem(newEvent.name);
                int index = getEventItemIndex(newEvent.name);
                eventsComboBox.setSelectedIndex(index);
            }
        });
    }

    private void createNewRegatta(final RegattaDTO newRegatta) {
        LinkedHashMap<String, Pair<List<Triple<String, Integer, Color>>, Boolean>> seriesStructure =
                new LinkedHashMap<String, Pair<List<Triple<String, Integer, Color>>, Boolean>>();
        for (SeriesDTO seriesDTO : newRegatta.series) {
            List<Triple<String, Integer, Color>> fleets = new ArrayList<Triple<String, Integer, Color>>();
            for(FleetDTO fleetDTO : seriesDTO.getFleets()) {
                Triple<String, Integer, Color> fleetTriple = new Triple<String, Integer, Color>(fleetDTO.name, fleetDTO.getOrderNo(), fleetDTO.getColor());
                fleets.add(fleetTriple);
            }
            Pair<List<Triple<String, Integer, Color>>, Boolean> seriesPair = new Pair<List<Triple<String, Integer, Color>>, Boolean>(fleets, seriesDTO.isMedal());
            seriesStructure.put(seriesDTO.name, seriesPair);
        }
        sailingService.createRegatta(newRegatta.name, newRegatta.boatClass.name, seriesStructure, true,
                newRegatta.scoringScheme, new AsyncCallback<RegattaDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new regatta" + newRegatta.name + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(RegattaDTO regatta) {
                regattaRefresher.fillRegattas();
            }
        });
    }

    private int getEventItemIndex(String eventName) {
        for (int i = 0; i < eventsComboBox.getItemCount(); i++) {
            if (eventName.equals(eventsComboBox.getItemText(i))) {
                return i;
            }
        }
        return -1;
    }

    private void fillEvents() {
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getEvents() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<EventDTO> result) {
                events.clear();
                events.addAll(result);
                eventsComboBox.clear();
                eventsComboBox.addItem("Please select an event...");
                eventsComboBox.setSelectedIndex(0);
                for (EventDTO event : result) {
                    eventsComboBox.addItem(event.name);
                }
            }
        });
    }

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        regattaProvider.getList().clear();
        regattaProvider.getList().addAll(regattas);
    }
}
