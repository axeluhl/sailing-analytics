package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.FlagImageRenderer;
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
public class CompactCompetitorTableWrapper<S extends RefreshableSelectionModel<CompetitorDTO>> extends TableWrapper<CompetitorDTO, S> {
    private final LabeledAbstractFilterablePanel<CompetitorDTO> filterField;
    private final Map<CompetitorDTO, BoatDTO> boatsForCompetitors;
    
    public CompactCompetitorTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            boolean multiSelection, boolean enablePager) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager,
                new EntityIdentityComparator<CompetitorDTO>() {
                    @Override
                    public boolean representSameEntity(CompetitorDTO dto1, CompetitorDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                    @Override
                    public int hashCode(CompetitorDTO t) {
                        return t.getIdAsString().hashCode();
                    }
                });
        boatsForCompetitors = new HashMap<>();
        ListHandler<CompetitorDTO> competitorColumnListHandler = getColumnSortHandler();
        
        // competitors table
        TextColumn<CompetitorDTO> competitorNameColumn = new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        };
        competitorNameColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorNameColumn, new Comparator<CompetitorDTO>() {
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        TextColumn<CompetitorDTO> competitorShortNameColumn = new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getShortName();
            }
        };
        competitorShortNameColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorShortNameColumn, new Comparator<CompetitorDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return comparator.compare(o1.getShortName(), o2.getShortName());
            }
        });
        Column<CompetitorDTO, SafeHtml> flagImageColumn = new Column<CompetitorDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                final String twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();
                final String flagImageURL = competitor.getFlagImageURL();
                if (flagImageURL != null && !flagImageURL.isEmpty()) {
                    sb.append(FlagImageRenderer.imageWithTitle(flagImageURL, competitor.getName()));
                    sb.appendHtmlConstant("&nbsp;");
                } else {
                    final ImageResource flagImageResource;
                    if (twoLetterIsoCountryCode==null || twoLetterIsoCountryCode.isEmpty()) {
                        flagImageResource = FlagImageResolverImpl.get().getEmptyFlagImageResource();
                    } else {
                        flagImageResource = FlagImageResolverImpl.get().getFlagImageResource(twoLetterIsoCountryCode);
                    }
                    if (flagImageResource != null) {
                        sb.append(FlagImageRenderer.image(flagImageResource.getSafeUri().asString()));
                        sb.appendHtmlConstant("&nbsp;");
                    }
                }
                return sb.toSafeHtml();
            }
        };
        TextColumn<CompetitorDTO> competitorSearchTagColumn = new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getSearchTag();
            }
        };
        competitorSearchTagColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorSearchTagColumn, new Comparator<CompetitorDTO>() {
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return new NaturalComparator(false).compare(o1.getSearchTag(), o2.getSearchTag());
            }
        });

        TextColumn<CompetitorDTO> isBoatLinkedColumn = new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return boatsForCompetitors.get(competitor) != null ? stringMessages.yes() : stringMessages.no();
            }
        };
        
        filterField = new LabeledAbstractFilterablePanel<CompetitorDTO>(new Label(stringMessages.filterCompetitors()),
                new ArrayList<CompetitorDTO>(), table, dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(CompetitorDTO t) {
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
    
    public Iterable<CompetitorDTO> getAllCompetitors() {
        return filterField.getAll();
    }
    
    public LabeledAbstractFilterablePanel<CompetitorDTO> getFilterField() {
        return filterField;
    }
    
    public void refreshCompetitorList(Map<? extends CompetitorDTO, BoatDTO> result) {
        boatsForCompetitors.clear();
        for (final Entry<? extends CompetitorDTO, BoatDTO> e : result.entrySet()) {
            boatsForCompetitors.put(e.getKey(), e.getValue());
        }
        getFilteredCompetitors(result.keySet());
    }
    
    public void refreshCompetitorListFromRace(String leaderboardName, String raceColumnName, String fleetName) {
        final AsyncCallback<Map<? extends CompetitorDTO, BoatDTO>> myCallback = new AsyncCallback<Map<? extends CompetitorDTO, BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Map<? extends CompetitorDTO, BoatDTO> result) {
                refreshCompetitorList(result);
            }
        };
        sailingService.getCompetitorsAndBoatsOfRace(leaderboardName, raceColumnName, fleetName, myCallback);
    }

    private void getFilteredCompetitors(Iterable<? extends CompetitorDTO> result) {
        filterField.updateAll(result);
    }
}
