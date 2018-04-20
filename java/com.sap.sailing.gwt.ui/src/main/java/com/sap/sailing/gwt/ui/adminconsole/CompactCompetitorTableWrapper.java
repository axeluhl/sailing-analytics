package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

/**
 * A compact filterable competitor table. The data model is managed by the {@link #getFilterField() filter field}. In
 * order to set an initial set of competitors to display by this table, use {@link #refreshCompetitorList(Iterable)}.
 * The selected competitors can be obtained from the {@link #getSelectionModel() selection model}. 
 * The competitors currently in the table (regardless of the current filter settings) are returned by {@link #getAllCompetitorsOfRace()}.
 * 
 * @author Frank Mittag
 *
 * @param <S>
 */
public class CompactCompetitorTableWrapper<S extends RefreshableSelectionModel<CompetitorWithBoatDTO>> extends TableWrapper<CompetitorWithBoatDTO, S> {
    private final LabeledAbstractFilterablePanel<CompetitorWithBoatDTO> filterField;
    
    public CompactCompetitorTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            boolean multiSelection, boolean enablePager) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager,
                new EntityIdentityComparator<CompetitorWithBoatDTO>() {
                    @Override
                    public boolean representSameEntity(CompetitorWithBoatDTO dto1, CompetitorWithBoatDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                    @Override
                    public int hashCode(CompetitorWithBoatDTO t) {
                        return t.getIdAsString().hashCode();
                    }
                });
        ListHandler<CompetitorWithBoatDTO> competitorColumnListHandler = getColumnSortHandler();
        
        // competitors table
        TextColumn<CompetitorWithBoatDTO> competitorNameColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getName();
            }
        };
        competitorNameColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorNameColumn, new Comparator<CompetitorWithBoatDTO>() {
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        TextColumn<CompetitorWithBoatDTO> competitorShortNameColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getShortName();
            }
        };
        competitorShortNameColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorShortNameColumn, new Comparator<CompetitorWithBoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return comparator.compare(o1.getShortName(), o2.getShortName());
            }
        });

        Column<CompetitorWithBoatDTO, SafeHtml> flagImageColumn = new Column<CompetitorWithBoatDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorWithBoatDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                ImageResourceRenderer renderer = new ImageResourceRenderer();
                final String twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();
                final String flagImageURL = competitor.getFlagImageURL();
                if (flagImageURL != null && !flagImageURL.isEmpty()) {
                    sb.appendHtmlConstant("<img src=\"" + flagImageURL + "\" width=\"18px\" height=\"12px\" title=\"" + competitor.getName() + "\"/>");
                    sb.appendHtmlConstant("&nbsp;");
                } else {
                    final ImageResource flagImageResource;
                    if (twoLetterIsoCountryCode==null || twoLetterIsoCountryCode.isEmpty()) {
                        flagImageResource = FlagImageResolverImpl.get().getEmptyFlagImageResource();
                    } else {
                        flagImageResource = FlagImageResolverImpl.get().getFlagImageResource(twoLetterIsoCountryCode);
                    }
                    if (flagImageResource != null) {
                        sb.append(renderer.render(flagImageResource));
                        sb.appendHtmlConstant("&nbsp;");
                    }
                }
                return sb.toSafeHtml();
            }
        };

        TextColumn<CompetitorWithBoatDTO> competitorSearchTagColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getSearchTag();
            }
        };
        competitorSearchTagColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorSearchTagColumn, new Comparator<CompetitorWithBoatDTO>() {
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return new NaturalComparator(false).compare(o1.getSearchTag(), o2.getSearchTag());
            }
        });

        TextColumn<CompetitorWithBoatDTO> isBoatLinkedColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                boolean hasBoat = competitor.getBoat() != null;
                return hasBoat ? stringMessages.yes() : stringMessages.no();
            }
        };
        
        filterField = new LabeledAbstractFilterablePanel<CompetitorWithBoatDTO>(new Label(stringMessages.filterCompetitors()),
                new ArrayList<CompetitorWithBoatDTO>(), table, dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(CompetitorWithBoatDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                string.add(t.getShortName());
                string.add(t.getSearchTag());
                return string;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());
                
        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(competitorColumnListHandler);
        table.addColumn(competitorNameColumn, stringMessages.name());
        table.addColumn(competitorShortNameColumn, stringMessages.shortName());
        table.addColumn(flagImageColumn, stringMessages.flags());
        table.addColumn(competitorSearchTagColumn, stringMessages.searchTag());
        table.addColumn(isBoatLinkedColumn, stringMessages.islinked());
        table.ensureDebugId("CompactCompetitorsTable");
    }
    
    public Iterable<CompetitorWithBoatDTO> getAllCompetitors() {
        return filterField.getAll();
    }
    
    public LabeledAbstractFilterablePanel<CompetitorWithBoatDTO> getFilterField() {
        return filterField;
    }
    
    public void refreshCompetitorList(Iterable<CompetitorWithBoatDTO> competitors) {
        getFilteredCompetitors(competitors);
    }
    
    public void refreshCompetitorListFromRace(String leaderboardName, String raceColumnName, String fleetName) {
        final AsyncCallback<Map<CompetitorWithBoatDTO, BoatDTO>> myCallback = new AsyncCallback<Map<CompetitorWithBoatDTO, BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Map<CompetitorWithBoatDTO, BoatDTO> result) {
                refreshCompetitorList(result.keySet());
            }
        };
        sailingService.getCompetitorsAndBoatsOfRace(leaderboardName, raceColumnName, fleetName, myCallback);
    }

    private void getFilteredCompetitors(Iterable<CompetitorWithBoatDTO> result) {
        filterField.updateAll(result);
    }
}
