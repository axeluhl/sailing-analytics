package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.server.api.EventNameAndRaceName;

public class WindPanel extends FormPanel implements EventDisplayer, WindShower, RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private final StringMessages stringMessages;
    private final WindSettingPanel windSettingPanel;
    private ColumnSortList columnSortList;
    private final IdentityColumn<WindDTO> removeColumn;
    private final TextColumn<WindDTO> timeColumn;
    private final TextColumn<WindDTO> speedInKnotsColumn;
    private final TextColumn<WindDTO> windDirectionInDegColumn;
    private final TextColumn<WindDTO> dampenedSpeedInKnotsColumn;
    private final TextColumn<WindDTO> dampenedWindDirectionInDegColumn;
    private final TrackedEventsComposite trackedEventsComposite;
    private final ListBox windSourceSelection;
    private final Map<WindSource, ListDataProvider<WindDTO>> windLists;
    private final CheckBox raceIsKnownToStartUpwindBox;
    private final WindChart windChart;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public WindPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            EventRefresher eventRefresher, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        windLists = new HashMap<WindSource, ListDataProvider<WindDTO>>();
        windSourceSelection = new ListBox();
        windSourceSelection.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setWindSource(/* runOnSuccess */ null);
            }
        });
        removeColumn = new IdentityColumn<WindDTO>(new ActionCell<WindDTO>(stringMessages.remove(), new Delegate<WindDTO>() {
            @Override
            public void execute(final WindDTO wind) {
                List<Triple<EventDTO, RegattaDTO, RaceDTO>> eventAndRaces = trackedEventsComposite.getSelectedEventAndRace();
                final Triple<EventDTO, RegattaDTO, RaceDTO> eventAndRace = eventAndRaces.get(eventAndRaces.size()-1);
                sailingService.removeWind(new EventNameAndRaceName(eventAndRace.getA().name, eventAndRace.getC().name), wind, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // remove row from underlying list:
                        windLists.get(WindSource.WEB).getList().remove(wind);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                                        WindPanel.this.errorReporter.reportError(
                                                WindPanel.this.stringMessages.errorSettingWindForRace()+ " "+eventAndRace.getC().name
                                                + ": "+ caught.getMessage());
                                    }
                });
            }
        }));
        timeColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return new Date(object.timepoint).toString();
            }
        };
        speedInKnotsColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.trueWindSpeedInKnots;
            }
        };
        windDirectionInDegColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.trueWindFromDeg;
            }
        };
        dampenedSpeedInKnotsColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.dampenedTrueWindSpeedInKnots;
            }
        };
        dampenedWindDirectionInDegColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.dampenedTrueWindFromDeg;
            }
        };
        grid = new Grid(4, 2); // first row: event/race selection; second row: wind source selection; third row: wind display
        trackedEventsComposite = new TrackedEventsComposite(sailingService, errorReporter, eventRefresher, stringMessages, false);
        trackedEventsComposite.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, trackedEventsComposite);
        windSettingPanel = new WindSettingPanel(sailingService, errorReporter, trackedEventsComposite, this);
        grid.setWidget(0, 1, windSettingPanel);
        HorizontalPanel windSourceSelectionPanel = new HorizontalPanel();
        windSourceSelectionPanel.setSpacing(10);
        windSourceSelectionPanel.add(new Label(stringMessages.windSource()));
        windSourceSelectionPanel.add(windSourceSelection);
        raceIsKnownToStartUpwindBox = new CheckBox(stringMessages.raceIsKnownToStartUpwind());
        windSourceSelectionPanel.add(raceIsKnownToStartUpwindBox);
        raceIsKnownToStartUpwindBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setWindSource(/* runOnSuccess */ new Runnable() {
                    public void run() {
                        clearOrShowWindBasedOnRaceSelection(trackedEventsComposite.getSelectedEventAndRace());
                    }
                });
            }
        });
        ImageResource settingsImage = resources.settingsIcon();
        Anchor showConfigAnchor = new Anchor(AbstractImagePrototype.create(settingsImage).getSafeHtml());
        showConfigAnchor.setTitle(stringMessages.configuration());
        showConfigAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<WindChartSettings>(windChart, stringMessages).show();
            }
        });
        windSourceSelectionPanel.add(showConfigAnchor);
        grid.setWidget(1, 0, windSourceSelectionPanel);
        windChart = new WindChart(new WindChartSettings(WindSource.values()), stringMessages, errorReporter);
        grid.setWidget(2, 0, windChart.getEntryWidget());
        grid.getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
        this.setWidget(grid);
    }

    private void clearOrShowWindBasedOnRaceSelection(List<Triple<EventDTO, RegattaDTO, RaceDTO>> selectedRaces) {
        if (selectedRaces.isEmpty()) {
            clearWindDisplay(); // no wind known for untracked race
        } else {
            showWind(selectedRaces.get(0).getA(), selectedRaces.get(0).getC());
        }
    }

    @Override
    public void fillEvents(List<EventDTO> result) {
        trackedEventsComposite.fillEvents(result);
    }

    @Override
    public void showWind(final EventDTO event, final RaceDTO race) {
        sailingService.getWindInfo(new EventNameAndRaceName(event.name, race.name),
        // TODO Time interval should be determined by a selection in the chart but be at most 60s. See bug #121. Consider incremental updates for new data only.
                null, null, // use race start and time of newest event as default time period
                null, // retrieve data on all wind sources
                new AsyncCallback<WindInfoForRaceDTO>() {
                    @Override
                    public void onSuccess(WindInfoForRaceDTO result) {
                        if (result != null) {
                            showWindForRace(result);
                            windSettingPanel.setEnabled(true);
                            updateWindSources(result);
                        } else {
                            clearWindDisplay(); // no wind known for untracked race
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(WindPanel.this.stringMessages.errorFetchingWindInformationForRace()+" " + race.name + ": "
                                + caught.getMessage());
                    }
                });
    }
    
    private void updateWindSources(WindInfoForRaceDTO result) {
        int selectedIndex = -1;
        for (WindSource windSource : result.windTrackInfoByWindSource.keySet()) {
            boolean found = false;
            int i=0;
            while (!found && i<windSourceSelection.getItemCount()) {
                if (windSource.name().equals(windSourceSelection.getItemText(i))) {
                    found = true;
                } else {
                    i++;
                }
            }
            if (!found) {
                windSourceSelection.addItem(windSource.name());
            }
            if (windSource == result.selectedWindSource) {
                selectedIndex = i;
            }
        }
        windSourceSelection.setSelectedIndex(selectedIndex);
    }

    private void clearWindDisplay() {
        grid.setWidget(2, 0, null);
        windSettingPanel.setEnabled(false);
        windLists.clear();
    }

    private void showWindForRace(WindInfoForRaceDTO result) {
        raceIsKnownToStartUpwindBox.setValue(result.raceIsKnownToStartUpwind);
        grid.setWidget(3, 0, null);
        VerticalPanel windDisplay = new VerticalPanel();
        grid.setWidget(3, 0, windDisplay);
        windChart.updateStripChartSeries(result);
        for (Map.Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
            Label windSourceLabel = new Label(stringMessages.windSource()+": "+e.getKey()+
                    ", "+stringMessages.dampeningInterval()+" "+e.getValue().dampeningIntervalInMilliseconds+"ms");
            windDisplay.add(windSourceLabel);
            timeColumn.setSortable(true);
            speedInKnotsColumn.setSortable(true);
            windDirectionInDegColumn.setSortable(true);
            dampenedSpeedInKnotsColumn.setSortable(true);
            dampenedWindDirectionInDegColumn.setSortable(true);
            CellTable<WindDTO> windTable = new CellTable<WindDTO>(/* pageSize */ 10000);
            if (e.getKey() == WindSource.WEB) {
                // only the WEB wind source is editable, hence has a "Remove" column
                windTable.addColumn(removeColumn, "Remove");
            }
            windTable.addColumn(timeColumn, "Time");
            windTable.addColumn(speedInKnotsColumn, "Speed (kn)");
            windTable.addColumn(windDirectionInDegColumn, "From (deg)");
            windTable.addColumn(dampenedSpeedInKnotsColumn, "Avg Speed (kn)");
            windTable.addColumn(dampenedWindDirectionInDegColumn, "Avg From (deg)");
            ListDataProvider<WindDTO> windList = new ListDataProvider<WindDTO>(e.getValue().windFixes);
            windLists.put(e.getKey(), windList);
            windList.addDataDisplay(windTable);
            Handler columnSortHandler = getWindTableColumnSortHandler(windList.getList(), timeColumn,
                    speedInKnotsColumn, windDirectionInDegColumn, dampenedSpeedInKnotsColumn, dampenedWindDirectionInDegColumn);
            windTable.addColumnSortHandler(columnSortHandler);
            List<ColumnSortInfo> sortedColumnList = new ArrayList<ColumnSortInfo>();
            if (columnSortList != null) {
                for (int i=0; i<columnSortList.size(); i++) {
                    sortedColumnList.add(columnSortList.get(i));
                }
            }
            columnSortList = windTable.getColumnSortList();
            if (sortedColumnList.isEmpty()) {
                columnSortList.push(timeColumn);
            } else {
                for (ColumnSortInfo sortInfo : sortedColumnList) {
                    columnSortList.push(sortInfo);
                }
                ColumnSortEvent.fire(windTable, columnSortList);
            }
            windDisplay.add(windTable);
        }
    }
    
    private Handler getWindTableColumnSortHandler(List<WindDTO> list, TextColumn<WindDTO> timeColumn,
            TextColumn<WindDTO> speedInKnotsColumn, TextColumn<WindDTO> windDirectionInDegColumn,
            TextColumn<WindDTO> dampenedSpeedInKnotsColumn, TextColumn<WindDTO> dampenedWindDirectionInDegColumn) {
        ListHandler<WindDTO> result = new ListHandler<WindDTO>(list);
        result.setComparator(timeColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.timepoint < o2.timepoint ? -1 : o1.timepoint == o2.timepoint ? 0 : 1;
            }
        });
        result.setComparator(speedInKnotsColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.trueWindSpeedInKnots < o2.trueWindSpeedInKnots ? -1 :
                    o1.trueWindSpeedInKnots == o2.trueWindSpeedInKnots ? 0 : 1;
            }
        });
        result.setComparator(windDirectionInDegColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.trueWindFromDeg < o2.trueWindFromDeg ? -1 :
                    o1.trueWindFromDeg == o2.trueWindFromDeg ? 0 : 1;
            }
        });
        result.setComparator(dampenedSpeedInKnotsColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.dampenedTrueWindSpeedInKnots < o2.dampenedTrueWindSpeedInKnots ? -1 :
                    o1.dampenedTrueWindSpeedInKnots == o2.dampenedTrueWindSpeedInKnots ? 0 : 1;
            }
        });
        result.setComparator(dampenedWindDirectionInDegColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.dampenedTrueWindFromDeg < o2.dampenedTrueWindFromDeg ? -1 :
                    o1.dampenedTrueWindFromDeg == o2.dampenedTrueWindFromDeg ? 0 : 1;
            }
        });
        return result;
    }

    private void setWindSource(final Runnable runOnSuccess) {
        List<Triple<EventDTO, RegattaDTO, RaceDTO>> selection = trackedEventsComposite.getSelectedEventAndRace();
        if (selection != null && !selection.isEmpty()) {
            final Triple<EventDTO, RegattaDTO, RaceDTO> selectedRace = selection.get(0);
            final String windSourceName = windSourceSelection.getItemText(windSourceSelection.getSelectedIndex());
            sailingService.setWindSource(new EventNameAndRaceName(selectedRace.getA().name, selectedRace.getC().name),
                    windSourceName, raceIsKnownToStartUpwindBox.getValue(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(WindPanel.this.stringMessages.errorWhileTryingToSetWindSourceForRace()+
                                    " "+selectedRace.getC().name+" "+WindPanel.this.stringMessages.inEvent()+" "+selectedRace.getA().name+
                                    " "+WindPanel.this.stringMessages.to()+" "+
                                    windSourceName+": "+caught.getMessage());
                        }
                        @Override
                        public void onSuccess(Void result) {
                            if (runOnSuccess != null) {
                                runOnSuccess.run();
                            }
                        }
                    });
        }
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDTO, RegattaDTO, RaceDTO>> selectedRaces) {
        clearOrShowWindBasedOnRaceSelection(selectedRaces);
    }
    
    public List<Triple<EventDTO, RegattaDTO, RaceDTO>> getSelectedRaces(){
        return trackedEventsComposite.getSelectedEventAndRace();
    }
}
