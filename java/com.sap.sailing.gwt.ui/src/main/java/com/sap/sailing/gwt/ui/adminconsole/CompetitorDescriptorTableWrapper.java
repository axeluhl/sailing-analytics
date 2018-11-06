package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.FlagImageRenderer;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class CompetitorDescriptorTableWrapper<S extends RefreshableSelectionModel<CompetitorDescriptor>>
        extends TableWrapper<CompetitorDescriptor, S> {

    private final LabeledAbstractFilterablePanel<CompetitorDescriptor> filterablePanelCompetitorDescriptor;

    private final CompetitorImportMatcher competitorImportMatcher;
    
    private static class CompetitorImportTableActionIcons extends ImagesBarCell {
        static final String ACTION_UNLINK = "ACTION_UNLINK";
        private final StringMessages stringMessages;
        private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

        public CompetitorImportTableActionIcons(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        protected Iterable<ImageSpec> getImageSpecs() {
            return Arrays.asList(
                    new ImageSpec(ACTION_UNLINK, stringMessages.actionEdit(), makeImagePrototype(resources.unlinkIcon()))
                    );
        }

        /**
         * An unlink button is rendered if an only if the {@code data} value is not an empty string.
         */
        @Override
        protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
            if (!data.asString().isEmpty()) {
                super.render(context, data, sb);
            }
        }
    }
    
    /**
     * An instance implementing this interface may be passed to the constructor of the table wrapper. It will then receive
     * a callback whenever the unlink action has been triggered. This way the table of importable competitors doesn't need
     * to know about how the selection models are used outside of the table itself to establish the link between the
     * importable and existing competitor.<p>
     * 
     * Furthermore, the {@link #getExigetExistingCompetitorToUseInsteadOf} method tells the existing competitor to which
     * a competitor that can be imported is mapped. If {@code null} is returned, no such mapping exists for an importable
     * competitor, and calling {@link #unlinkCompetitor} will not have any effect.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    public static interface CompetitorsToImportToExistingLinking {
        void unlinkCompetitor(CompetitorDescriptor competitor);
        CompetitorDTO getExistingCompetitorToUseInsteadOf(CompetitorDescriptor competitor);
    }

    public CompetitorDescriptorTableWrapper(CompetitorImportMatcher competitorImportMatcherParam,
            SailingServiceAsync sailingService, final StringMessages stringMessages, ErrorReporter errorReporter,
            boolean multiSelection, boolean enablePager, final CompetitorsToImportToExistingLinking unlinkCallback) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager,
                new EntityIdentityComparator<CompetitorDescriptor>() {
                    @Override
                    public boolean representSameEntity(CompetitorDescriptor dto1, CompetitorDescriptor dto2) {
                        return dto1.equals(dto2);
                    }

                    @Override
                    public int hashCode(CompetitorDescriptor dto) {
                        return dto.hashCode();
                    }
                });
        this.competitorImportMatcher = competitorImportMatcherParam;
        filterablePanelCompetitorDescriptor = new LabeledAbstractFilterablePanel<CompetitorDescriptor>(
                new Label(stringMessages.filterImportedCompetitorsByNameSailRaceFleet()),
                new ArrayList<CompetitorDescriptor>(), dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(CompetitorDescriptor competitorDescriptor) {
                List<String> string = new ArrayList<String>();
                string.add(competitorDescriptor.getName());
                string.add(competitorDescriptor.getSailNumber());
                string.add(competitorDescriptor.getBoatClassName());
                string.add(competitorDescriptor.getRaceName());
                string.add(competitorDescriptor.getFleetName());
                return string;
            }

            @Override
            public AbstractCellTable<CompetitorDescriptor> getCellTable() {
                return CompetitorDescriptorTableWrapper.this.getTable();
            }
        };
        registerSelectionModelOnNewDataProvider(filterablePanelCompetitorDescriptor.getAllListDataProvider());

        TextColumn<CompetitorDescriptor> competitorNameColumn = new TextColumn<CompetitorDescriptor>() {
            @Override
            public String getValue(CompetitorDescriptor competitor) {
                return competitor.getName();
            }
        };
        competitorNameColumn.setSortable(true);

        Column<CompetitorDescriptor, SafeHtml> sailIdColumn = new Column<CompetitorDescriptor, SafeHtml>(
                new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorDescriptor competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                final CountryCode countryCode = competitor.getCountryCode();
                final String twoLetterIsoCountryCode = countryCode == null ? null : countryCode.getTwoLetterISOCode();
                final ImageResource flagImageResource;
                if (twoLetterIsoCountryCode == null || twoLetterIsoCountryCode.isEmpty()) {
                    flagImageResource = FlagImageResolverImpl.get().getEmptyFlagImageResource();
                } else {
                    flagImageResource = FlagImageResolverImpl.get().getFlagImageResource(twoLetterIsoCountryCode);
                }
                if (flagImageResource != null) {
                    sb.append(FlagImageRenderer.image(flagImageResource.getSafeUri().asString()));
                    sb.appendHtmlConstant("&nbsp;");
                }

                sb.appendEscaped(competitor.getSailNumber());
                return sb.toSafeHtml();
            }
        };
        sailIdColumn.setSortable(true);

        TextColumn<CompetitorDescriptor> boatClassNameColumn = new TextColumn<CompetitorDescriptor>() {
            @Override
            public String getValue(CompetitorDescriptor competitorDescriptor) {
                return competitorDescriptor.getBoatClassName();
            }
        };
        boatClassNameColumn.setSortable(true);
        
        TextColumn<CompetitorDescriptor> raceNameColumn = new TextColumn<CompetitorDescriptor>() {
            @Override
            public String getValue(CompetitorDescriptor competitorDescriptor) {
                return competitorDescriptor.getRaceName();
            }
        };
        raceNameColumn.setSortable(true);

        TextColumn<CompetitorDescriptor> fleetNameColumn = new TextColumn<CompetitorDescriptor>() {
            @Override
            public String getValue(CompetitorDescriptor competitorDescriptor) {
                return competitorDescriptor.getFleetName();
            }
        };
        fleetNameColumn.setSortable(true);

        TextColumn<CompetitorDescriptor> isHasMatchesColumn = new TextColumn<CompetitorDescriptor>() {
            @Override
            public String getValue(CompetitorDescriptor competitorDescriptor) {
                return competitorImportMatcher.getMatchesCompetitors(competitorDescriptor).isEmpty()
                        ? stringMessages.no() : stringMessages.yes();
            }
        };
        isHasMatchesColumn.setSortable(true);
        // alters the getValue method such that it returns an empty string if the competitor to import is not linked to an existing one, non-null otherwise
        ImagesBarColumn<CompetitorDescriptor, CompetitorImportTableActionIcons> unlinkColumn =
                new ImagesBarColumn<CompetitorDescriptor, CompetitorDescriptorTableWrapper.CompetitorImportTableActionIcons>(new CompetitorImportTableActionIcons(stringMessages)) {
                    @Override
                    public String getValue(CompetitorDescriptor competitor) {
                        return unlinkCallback.getExistingCompetitorToUseInsteadOf(competitor)==null?"":"linked";
                    }
        };
        unlinkColumn.setFieldUpdater(new FieldUpdater<CompetitorDescriptor, String>() {
            @Override
            public void update(int index, final CompetitorDescriptor competitor, String value) {
                if (CompetitorImportTableActionIcons.ACTION_UNLINK.equals(value)) {
                    if (unlinkCallback != null) {
                        unlinkCallback.unlinkCompetitor(competitor);
                    }
                }
            }
        });
        unlinkColumn.setSortable(true);
        mainPanel.insert(filterablePanelCompetitorDescriptor, 0);
        table.addColumn(sailIdColumn, stringMessages.sailNumber());
        table.addColumn(competitorNameColumn, stringMessages.name());
        table.addColumn(boatClassNameColumn, stringMessages.boatClass());
        table.addColumn(raceNameColumn, stringMessages.race());
        table.addColumn(fleetNameColumn, stringMessages.fleet());
        table.addColumn(isHasMatchesColumn, stringMessages.hasMatches());
        table.addColumn(unlinkColumn, stringMessages.unlink());
        table.addColumnSortHandler(getCompetitorDescriptorTableColumnListSortHandler(competitorNameColumn, boatClassNameColumn,
                sailIdColumn, raceNameColumn, fleetNameColumn, isHasMatchesColumn, unlinkColumn, unlinkCallback));
    }

    private ListHandler<CompetitorDescriptor> getCompetitorDescriptorTableColumnListSortHandler(
            TextColumn<CompetitorDescriptor> competitorNameColumn,
            TextColumn<CompetitorDescriptor> boatClassNameColumn, Column<CompetitorDescriptor, SafeHtml> sailIdColumn,
            TextColumn<CompetitorDescriptor> raceNameColumn,
            TextColumn<CompetitorDescriptor> fleetNameColumn, TextColumn<CompetitorDescriptor> isHasMatchesColumn,
            ImagesBarColumn<CompetitorDescriptor, CompetitorImportTableActionIcons> unlinkColumn, CompetitorsToImportToExistingLinking unlinkCallback) {
        ListHandler<CompetitorDescriptor> competitorColumnListHandler = getColumnSortHandler();
        final NaturalComparator caseInsensitiveNaturalComparator = new NaturalComparator(/* case sensitive */ false);
        final Comparator<CompetitorDTO> nullFirstComparator = Comparator.nullsFirst(/* real comparator */ null);
        competitorColumnListHandler.setComparator(competitorNameColumn, (cd1, cd2)->caseInsensitiveNaturalComparator.compare(cd1.getName(), cd2.getName()));
        competitorColumnListHandler.setComparator(boatClassNameColumn, (cd1, cd2)->caseInsensitiveNaturalComparator.compare(cd1.getBoatClassName(), cd2.getBoatClassName()));
        competitorColumnListHandler.setComparator(sailIdColumn, (cd1, cd2)->caseInsensitiveNaturalComparator.compare(cd1.getSailNumber(), cd2.getSailNumber()));
        competitorColumnListHandler.setComparator(raceNameColumn, (cd1, cd2)->caseInsensitiveNaturalComparator.compare(cd1.getRaceName(), cd2.getRaceName()));
        competitorColumnListHandler.setComparator(fleetNameColumn, (cd1, cd2)->caseInsensitiveNaturalComparator.compare(cd1.getFleetName(), cd2.getFleetName()));
        competitorColumnListHandler.setComparator(isHasMatchesColumn, (cd1, cd2)->((Boolean) competitorImportMatcher.getMatchesCompetitors(cd1).isEmpty()).compareTo(
                competitorImportMatcher.getMatchesCompetitors(cd2).isEmpty()));
        competitorColumnListHandler.setComparator(unlinkColumn, (cd1, cd2)->(nullFirstComparator.compare(
                unlinkCallback.getExistingCompetitorToUseInsteadOf(cd1), unlinkCallback.getExistingCompetitorToUseInsteadOf(cd2))));
        return competitorColumnListHandler;
    }

    public Iterable<CompetitorDescriptor> getAllCompetitorDescriptors() {
        return filterablePanelCompetitorDescriptor.getAll();
    }

    public void refreshCompetitorDescriptorList(Iterable<CompetitorDescriptor> competitors) {
        filterablePanelCompetitorDescriptor.updateAll(competitors);
    }

}
