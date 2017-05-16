package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.gwt.settings.client.EntryPointWithSettingsLinkFactory;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.ui.adminconsole.AbstractLeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorTemplates;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class RaceTableWrapper<S extends RefreshableSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>
extends TableWrapper<RaceColumnDTOAndFleetDTOWithNameBasedEquality, S> {
    private final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    private String selectedLeaderboardName;

    public RaceTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean multiSelection) {
        super(sailingService, stringMessages, errorReporter, multiSelection, /* enablePager */ false,
                new EntityIdentityComparator<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public boolean representSameEntity(RaceColumnDTOAndFleetDTOWithNameBasedEquality dto1,
                    RaceColumnDTOAndFleetDTOWithNameBasedEquality dto2) {
                return dto1.getC().name.equals(dto2.getC().name) &&
                        dto1.getA().getName().equals(dto2.getA().getName()) &&
                        dto1.getB().getName().equals(dto2.getB().getName());
            }

            @Override
            public int hashCode(RaceColumnDTOAndFleetDTOWithNameBasedEquality t) {
                return t.getA().getName().concat(t.getB().getName()).concat(t.getC().name).hashCode();
            }
        });
        Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, SafeHtml> raceNameColumn =
                new Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, SafeHtml>(new AnchorCell()) {
            @Override
            public SafeHtml getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality raceInLeaderboardDTOAndFleetName) {
                if (raceInLeaderboardDTOAndFleetName.getA().getRaceIdentifier(raceInLeaderboardDTOAndFleetName.getB()) != null) {
                    RegattaNameAndRaceName raceIdentifier = (RegattaNameAndRaceName) raceInLeaderboardDTOAndFleetName
                            .getA().getRaceIdentifier(raceInLeaderboardDTOAndFleetName.getB());
                            RaceboardContextDefinition raceboardContext = new RaceboardContextDefinition(
                                    raceIdentifier.getRegattaName(), raceIdentifier.getRaceName(),
                                    selectedLeaderboardName, null, null, null);
                            RaceBoardPerspectiveOwnSettings perspectiveOwnSettings = RaceBoardPerspectiveOwnSettings
                                    .createDefaultWithCanReplayDuringLiveRaces(true);
                            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings = new PerspectiveCompositeSettings<>(
                                    perspectiveOwnSettings, Collections.emptyMap());

                            String link = EntryPointWithSettingsLinkFactory.createRaceBoardLink(raceboardContext,
                                    settings);

                            return ANCHORTEMPLATE.cell(UriUtils.fromString(link),
                                    raceInLeaderboardDTOAndFleetName.getA().getRaceColumnName());
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
