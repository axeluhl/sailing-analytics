package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.dto.CompetitorDescriptorDTO;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class CompetitorDescriptorTableWrapper<S extends RefreshableSelectionModel<CompetitorDescriptorDTO>>
        extends TableWrapper<CompetitorDescriptorDTO, S> {

    private final LabeledAbstractFilterablePanel<CompetitorDescriptorDTO> filterablePanelCompetitorDescriptor;

    private final CompetitorImportMatcher competitorImportMatcher;

    public CompetitorDescriptorTableWrapper(CompetitorImportMatcher competitorImportMatcherParam,
            SailingServiceAsync sailingService, final StringMessages stringMessages, ErrorReporter errorReporter,
            boolean multiSelection, boolean enablePager) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager,
                new EntityIdentityComparator<CompetitorDescriptorDTO>() {
                    @Override
                    public boolean representSameEntity(CompetitorDescriptorDTO dto1, CompetitorDescriptorDTO dto2) {
                        return dto1.equals(dto2);
                    }

                    @Override
                    public int hashCode(CompetitorDescriptorDTO dto) {
                        return dto.hashCode();
                    }
                });

        this.competitorImportMatcher = competitorImportMatcherParam;

        filterablePanelCompetitorDescriptor = new LabeledAbstractFilterablePanel<CompetitorDescriptorDTO>(
                new Label(stringMessages.filterImportedCompetitorsByNameSailRaceFleet()),
                new ArrayList<CompetitorDescriptorDTO>(), table, dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(CompetitorDescriptorDTO competitorDescriptor) {
                List<String> string = new ArrayList<String>();
                string.add(competitorDescriptor.getName());
                string.add(competitorDescriptor.getSailNumber());
                string.add(competitorDescriptor.getRaceName());
                string.add(competitorDescriptor.getFleetName());
                return string;
            }
        };
        registerSelectionModelOnNewDataProvider(filterablePanelCompetitorDescriptor.getAllListDataProvider());

        TextColumn<CompetitorDescriptorDTO> competitorNameColumn = new TextColumn<CompetitorDescriptorDTO>() {
            @Override
            public String getValue(CompetitorDescriptorDTO competitor) {
                return competitor.getName();
            }
        };
        competitorNameColumn.setSortable(true);

        Column<CompetitorDescriptorDTO, SafeHtml> sailIdColumn = new Column<CompetitorDescriptorDTO, SafeHtml>(
                new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorDescriptorDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                ImageResourceRenderer renderer = new ImageResourceRenderer();
                final String twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();

                final ImageResource flagImageResource;
                if (twoLetterIsoCountryCode == null || twoLetterIsoCountryCode.isEmpty()) {
                    flagImageResource = FlagImageResolver.getEmptyFlagImageResource();
                } else {
                    flagImageResource = FlagImageResolver.getFlagImageResource(twoLetterIsoCountryCode);
                }
                if (flagImageResource != null) {
                    sb.append(renderer.render(flagImageResource));
                    sb.appendHtmlConstant("&nbsp;");
                }

                sb.appendEscaped(competitor.getSailNumber());
                return sb.toSafeHtml();
            }
        };
        sailIdColumn.setSortable(true);

        TextColumn<CompetitorDescriptorDTO> raceNameColumn = new TextColumn<CompetitorDescriptorDTO>() {
            @Override
            public String getValue(CompetitorDescriptorDTO competitorDescriptor) {
                return competitorDescriptor.getRaceName();
            }
        };
        raceNameColumn.setSortable(true);

        TextColumn<CompetitorDescriptorDTO> fleetNameColumn = new TextColumn<CompetitorDescriptorDTO>() {
            @Override
            public String getValue(CompetitorDescriptorDTO competitorDescriptor) {
                return competitorDescriptor.getFleetName();
            }
        };
        fleetNameColumn.setSortable(true);

        TextColumn<CompetitorDescriptorDTO> isHasMatchesColumn = new TextColumn<CompetitorDescriptorDTO>() {
            @Override
            public String getValue(CompetitorDescriptorDTO competitorDescriptor) {
                return competitorImportMatcher.getMatchesCompetitors(competitorDescriptor).isEmpty()
                        ? stringMessages.no() : stringMessages.yes();
            }
        };
        isHasMatchesColumn.setSortable(true);

        mainPanel.insert(filterablePanelCompetitorDescriptor, 0);
        table.addColumn(sailIdColumn, stringMessages.sailNumber());
        table.addColumn(competitorNameColumn, stringMessages.name());
        table.addColumn(raceNameColumn, stringMessages.race());
        table.addColumn(fleetNameColumn, stringMessages.fleet());
        table.addColumn(isHasMatchesColumn, stringMessages.hasMatches());

        table.addColumnSortHandler(getCompetitorDescriptorTableColumnListSortHandler(competitorNameColumn, sailIdColumn,
                raceNameColumn, fleetNameColumn, isHasMatchesColumn));
    }

    private ListHandler<CompetitorDescriptorDTO> getCompetitorDescriptorTableColumnListSortHandler(
            TextColumn<CompetitorDescriptorDTO> competitorNameColumn,
            Column<CompetitorDescriptorDTO, SafeHtml> sailIdColumn, TextColumn<CompetitorDescriptorDTO> raceNameColumn,
            TextColumn<CompetitorDescriptorDTO> fleetNameColumn,
            TextColumn<CompetitorDescriptorDTO> isHasMatchesColumn) {
        ListHandler<CompetitorDescriptorDTO> competitorColumnListHandler = getColumnSortHandler();

        competitorColumnListHandler.setComparator(competitorNameColumn, new Comparator<CompetitorDescriptorDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);

            @Override
            public int compare(CompetitorDescriptorDTO cd1, CompetitorDescriptorDTO cd2) {
                return comparator.compare(cd1.getName(), cd2.getName());
            }
        });

        competitorColumnListHandler.setComparator(sailIdColumn, new Comparator<CompetitorDescriptorDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);

            @Override
            public int compare(CompetitorDescriptorDTO cd1, CompetitorDescriptorDTO cd2) {
                return comparator.compare(cd1.getSailNumber(), cd2.getSailNumber());
            }
        });

        competitorColumnListHandler.setComparator(raceNameColumn, new Comparator<CompetitorDescriptorDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);

            @Override
            public int compare(CompetitorDescriptorDTO cd1, CompetitorDescriptorDTO cd2) {
                return comparator.compare(cd1.getRaceName(), cd2.getRaceName());
            }
        });

        competitorColumnListHandler.setComparator(fleetNameColumn, new Comparator<CompetitorDescriptorDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);

            @Override
            public int compare(CompetitorDescriptorDTO cd1, CompetitorDescriptorDTO cd2) {
                return comparator.compare(cd1.getFleetName(), cd2.getFleetName());
            }
        });

        competitorColumnListHandler.setComparator(isHasMatchesColumn, new Comparator<CompetitorDescriptorDTO>() {

            @Override
            public int compare(CompetitorDescriptorDTO cd1, CompetitorDescriptorDTO cd2) {
                Boolean hasMathes1 = competitorImportMatcher.getMatchesCompetitors(cd1).isEmpty();
                Boolean hasMathes2 = competitorImportMatcher.getMatchesCompetitors(cd2).isEmpty();
                return hasMathes1 == hasMathes2 ? 0 : hasMathes1 ? 1 : -1;
            }
        });
        return competitorColumnListHandler;
    }

    public Iterable<CompetitorDescriptorDTO> getAllCompetitorDescriptors() {
        return filterablePanelCompetitorDescriptor.getAll();
    }

    public void refreshCompetitorDescriptorList(Iterable<CompetitorDescriptorDTO> competitors) {
        filterablePanelCompetitorDescriptor.updateAll(competitors);
    }

}
