package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.gwt.ui.adminconsole.AbstractLeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorTemplates;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

public class RaceTableWrapper<S extends RefreshableSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>
extends TableWrapper<RaceColumnDTOAndFleetDTOWithNameBasedEquality, S> {
    private final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    private String selectedLeaderboardName;

    public RaceTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean multiSelection) {
        super(sailingService, stringMessages, errorReporter, multiSelection, /* enablePager */ false, null /*EntityIdentityComparator for RefreshableSelectionModel*/);
        Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, SafeHtml> raceNameColumn =
                new Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, SafeHtml>(new AnchorCell()) {
            @Override
            public SafeHtml getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality raceInLeaderboardDTOAndFleetName) {
                if (raceInLeaderboardDTOAndFleetName.getA().getRaceIdentifier(raceInLeaderboardDTOAndFleetName.getB()) != null) {
                    RegattaNameAndRaceName raceIdentifier = (RegattaNameAndRaceName) raceInLeaderboardDTOAndFleetName
                            .getA().getRaceIdentifier(raceInLeaderboardDTOAndFleetName.getB());
                    Map<String, String> params = new HashMap<>();
                    params.put("leaderboardName", selectedLeaderboardName);
                    params.put("regattaName", raceIdentifier.getRegattaName());
                    params.put("raceName", raceIdentifier.getRaceName());
                    params.put("canReplayDuringLiveRaces", "true");
                    String link = EntryPointLinkFactory.createRaceBoardLink(params);
                    return ANCHORTEMPLATE.cell(link, raceInLeaderboardDTOAndFleetName.getA().getRaceColumnName());
                } else {
                    return SafeHtmlUtils.fromString(raceInLeaderboardDTOAndFleetName.getA().getRaceColumnName());
                }
            }
        };
        raceNameColumn.setSortable(true);
        getColumnSortHandler().setComparator(raceNameColumn, new Comparator<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public int compare(RaceColumnDTOAndFleetDTOWithNameBasedEquality o1, RaceColumnDTOAndFleetDTOWithNameBasedEquality o2) {
                return new NaturalComparator().compare(o1.getA().getName(), o2.getA().getName());
            }
        });
        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> fleetNameColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality object) {
                return object.getB().getName();
            }
        };
        fleetNameColumn.setSortable(true);
        getColumnSortHandler().setComparator(fleetNameColumn, new Comparator<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public int compare(RaceColumnDTOAndFleetDTOWithNameBasedEquality o1, RaceColumnDTOAndFleetDTOWithNameBasedEquality o2) {
                return new NaturalComparator().compare(o1.getB().getName(), o2.getB().getName());
            }
        });
        table.addColumn(raceNameColumn, stringMessages.race());
        table.addColumn(fleetNameColumn, stringMessages.fleet());
    }
    
    public void setSelectedLeaderboardName(String name) {
        this.selectedLeaderboardName = name;
    }
}
