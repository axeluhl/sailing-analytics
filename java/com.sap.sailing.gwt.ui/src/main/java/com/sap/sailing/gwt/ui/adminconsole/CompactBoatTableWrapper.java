package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.adminconsole.ColorColumn.ColorRetriever;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.panels.CustomizableFilterablePanel;

public class CompactBoatTableWrapper<S extends RefreshableSelectionModel<BoatDTO>> extends TableWrapper<BoatDTO, S> {
    private final CustomizableFilterablePanel<BoatDTO> filterField;
    private final Set<BoatDTO> boatsRegisteredWithRegatta;
    
    public CompactBoatTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean multiSelection, boolean enablePager) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager,
                new EntityIdentityComparator<BoatDTO>() {
                    @Override
                    public boolean representSameEntity(BoatDTO dto1, BoatDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                    @Override
                    public int hashCode(BoatDTO t) {
                        return t.getIdAsString().hashCode();
                    }
                });
        boatsRegisteredWithRegatta = new HashSet<>();
        ListHandler<BoatDTO> boatColumnListHandler = getColumnSortHandler();
        // boats table
        TextColumn<BoatDTO> boatNameColumn = new TextColumn<BoatDTO>() {
            @Override
            public String getValue(BoatDTO boat) {
                return boat.getName();
            }
        };
        boatNameColumn.setSortable(true);
        boatColumnListHandler.setComparator(boatNameColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return comparator.compare(o1.getName(), o2.getName());
            }
        });
        TextColumn<BoatDTO> boatClassColumn = new TextColumn<BoatDTO>() {
            @Override
            public String getValue(BoatDTO competitor) {
                return competitor.getBoatClass() != null ? competitor.getBoatClass().getName() : "";
            }
        };
        boatClassColumn.setSortable(true);
        boatColumnListHandler.setComparator(boatClassColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return comparator.compare(o1.getBoatClass().getName(), o2.getBoatClass().getName());
            }
        });
        Column<BoatDTO, SafeHtml> sailIdColumn = new Column<BoatDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(BoatDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendEscaped(competitor.getSailId());
                return sb.toSafeHtml();
            }
        };
        sailIdColumn.setSortable(true);
        boatColumnListHandler.setComparator(sailIdColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return comparator.compare(o1.getSailId(), o2.getSailId());
            }
        });
        Column<BoatDTO, SafeHtml> boatColorColumn = new ColorColumn<>(new ColorRetriever<BoatDTO>() {
            @Override
            public Color getColor(BoatDTO t) {
                return t.getColor();
            }
        });
        boatColorColumn.setSortable(true);
        boatColumnListHandler.setComparator(boatColorColumn, new Comparator<BoatDTO>() {
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                if (o1.getColor() == null) {
                    if (o2.getColor() == null) {
                        return 0;
                    }
                    return -1;
                } else if (o2.getColor() == null) {
                    return 1;
                }
                return o1.getColor().getAsHtml().compareTo(o2.getColor().getAsHtml());
            }
        });
        Column<BoatDTO, SafeHtml> boatIdColumn = new Column<BoatDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(BoatDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendEscaped(competitor.getIdAsString());
                return sb.toSafeHtml();
            }
        };
        boatIdColumn.setSortable(true);
        boatColumnListHandler.setComparator(boatIdColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return comparator.compare(o1.getIdAsString(), o2.getIdAsString());
            }
        });
        filterField = new CustomizableFilterablePanel<BoatDTO>(new ArrayList<>(), getDataProvider()) {
            @Override
            public Iterable<String> getSearchableStrings(BoatDTO boat) {
                List<String> string = new ArrayList<String>();
                string.add(boat.getName());
                string.add(boat.getSailId());
                string.add(boat.getIdAsString());
                string.add(boat.getBoatClass().getName());
                return string;
            }

            @Override
            public AbstractCellTable<BoatDTO> getCellTable() {
                return table;
            }  
        };
        final CheckBox filterToBoatsRegisteredOnRegatta = new CheckBox(stringMessages.filterToBoatsRegisteredOnRegatta());
        filterToBoatsRegisteredOnRegatta.addValueChangeHandler(checked->filterField.filter());
        filterField.add(new Label(""), filterToBoatsRegisteredOnRegatta, new Filter<BoatDTO>() {
            @Override
            public boolean matches(BoatDTO boat) {
                return !filterToBoatsRegisteredOnRegatta.getValue() || boatsRegisteredWithRegatta.contains(boat);
            }

            @Override
            public String getName() {
                return "BoatsRegisteredWithRegatta";
            }
        });
        filterField.addDefaultTextBox();
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());
        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(boatColumnListHandler);
        table.addColumn(boatNameColumn, stringMessages.name());
        table.addColumn(sailIdColumn, stringMessages.sailNumber());
        table.addColumn(boatClassColumn, stringMessages.boatClass());
        table.addColumn(boatColorColumn, stringMessages.color());
        table.addColumn(boatIdColumn, stringMessages.id());
        table.ensureDebugId("BoatsTable");
    }
    
    public Iterable<BoatDTO> getAllBoats() {
        return filterField.getAll();
    }
    
    public void filterBoats(Iterable<BoatDTO> boats) {
        getFilteredBoats(boats);
    }

    public void selectBoat(BoatDTO boatToSelect) {
        for (BoatDTO boat : getAllBoats()) {
            if (boat.getIdAsString().equals(boatToSelect.getIdAsString())) {
                getSelectionModel().setSelected(boat, true);
                break;
            }
        }
    }

    public void clearSelection() {
        getSelectionModel().clear();
    }

    public void refreshBoatListFromRace(String leaderboardName, String raceColumnName, String fleetName) {
        final AsyncCallback<Map<? extends CompetitorDTO, BoatDTO>> myCallback = new AsyncCallback<Map<? extends CompetitorDTO, BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.unableToObtainCompetitorsAndBoatsForRaceInFleetInLeaderboard(raceColumnName, fleetName, leaderboardName, caught.getMessage()));
            }

            @Override
            public void onSuccess(Map<? extends CompetitorDTO, BoatDTO> result) {
                filterBoats(result.values());
            }
        };
        sailingService.getCompetitorsAndBoatsOfRace(leaderboardName, raceColumnName, fleetName, myCallback);
        updateBoatRegistrationsForLeaderboard(leaderboardName);
    }

    private void updateBoatRegistrationsForLeaderboard(String leaderboardName) {
        sailingService.getBoatRegistrationsForLeaderboard(leaderboardName, new AsyncCallback<Collection<BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.unableToObtainBoatsRegisteredWith(leaderboardName, caught.getMessage()));
            }

            @Override
            public void onSuccess(Collection<BoatDTO> result) {
                boatsRegisteredWithRegatta.clear();
                boatsRegisteredWithRegatta.addAll(result);
            }
        });
    }
    
    /**
     * Updates the list of all boats and the set of boats registered with or used by the leaderboard named {@code leaderboardName}.
     * The latter will be used when the corresponding filter is activated.
     */
    public void refreshBoatList(String leaderboardName) {
        sailingService.getAllBoats(new AsyncCallback<Iterable<BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getAllBoats() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Iterable<BoatDTO> result) {
                filterBoats(result);
            }
        });
        updateBoatRegistrationsForLeaderboard(leaderboardName);
    }

    private void getFilteredBoats(Iterable<BoatDTO> result) {
        filterField.updateAll(result);
    }
}
