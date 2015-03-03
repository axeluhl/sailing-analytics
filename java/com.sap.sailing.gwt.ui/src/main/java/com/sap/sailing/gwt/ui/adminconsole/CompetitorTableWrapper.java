package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.adminconsole.ColorColumn.ColorRetriever;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class CompetitorTableWrapper<S extends SelectionModel<CompetitorDTO>> extends TableWrapper<CompetitorDTO, S> {
    private final LabeledAbstractFilterablePanel<CompetitorDTO> filterField;

    public CompetitorTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages,ErrorReporter errorReporter,
            S selectionModel, boolean enablePager) {
        super(sailingService, stringMessages, errorReporter, selectionModel, enablePager);
        
        ListHandler<CompetitorDTO> competitorColumnListHandler = new ListHandler<CompetitorDTO>(dataProvider.getList());
        
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

        TextColumn<CompetitorDTO> boatClassColumn = new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getBoatClass() != null ? competitor.getBoatClass().getName() : "";
            }
        };
        boatClassColumn.setSortable(true);
        competitorColumnListHandler.setComparator(boatClassColumn, new Comparator<CompetitorDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return comparator.compare(o1.getBoatClass().getName(), o2.getBoatClass().getName());
            }
        });
        
        Column<CompetitorDTO, SafeHtml> sailIdColumn = new Column<CompetitorDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                ImageResourceRenderer renderer = new ImageResourceRenderer();
                final String twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();
                final ImageResource flagImageResource;
                if (twoLetterIsoCountryCode==null || twoLetterIsoCountryCode.isEmpty()) {
                    flagImageResource = FlagImageResolver.getEmptyFlagImageResource();
                } else {
                    flagImageResource = FlagImageResolver.getFlagImageResource(twoLetterIsoCountryCode);
                }
                if (flagImageResource != null) {
                    sb.append(renderer.render(flagImageResource));
                    sb.appendHtmlConstant("&nbsp;");
                }
                sb.appendEscaped(competitor.getSailID());
                return sb.toSafeHtml();
            }
        };
        sailIdColumn.setSortable(true);
        competitorColumnListHandler.setComparator(sailIdColumn, new Comparator<CompetitorDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return comparator.compare(o1.getSailID(), o2.getSailID());
            }
        });

        Column<CompetitorDTO, SafeHtml> displayColorColumn = new ColorColumn<>(new ColorRetriever<CompetitorDTO>() {
            @Override
            public Color getColor(CompetitorDTO t) {
                return t.getColor();
            }
        });

        Column<CompetitorDTO, SafeHtml> imageColumn = new Column<CompetitorDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (competitor.getImageURL() != null && !competitor.getImageURL().isEmpty()) {
                    sb.appendHtmlConstant("<img src=\"/sailingserver/api/v1/file?uri=" + competitor.getImageURL()
                            + "\" height=\"40px\" title=\"" + competitor.getImageURL() + "\"/>");
                }
                return sb.toSafeHtml();
            }
        };
        imageColumn.setSortable(true);
        competitorColumnListHandler.setComparator(imageColumn, new Comparator<CompetitorDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return comparator.compare(o1.getImageURL(), o2.getImageURL());
            }
        });

        TextColumn<CompetitorDTO> competitorIdColumn = new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getIdAsString();
            }
        };

        competitorIdColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorIdColumn, new Comparator<CompetitorDTO>() {
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return new NaturalComparator(false).compare(o1.getIdAsString(), o2.getIdAsString());
            }
        });

        filterField = new LabeledAbstractFilterablePanel<CompetitorDTO>(new Label(stringMessages.filterCompetitors()),
                new ArrayList<CompetitorDTO>(), table, dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(CompetitorDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                string.add(t.getSailID());
                string.add(t.getBoatClass().getName());
                return string;
            }
        };

        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(competitorColumnListHandler);
        table.addColumn(sailIdColumn, stringMessages.sailNumber());
        table.addColumn(competitorNameColumn, stringMessages.name());
        table.addColumn(boatClassColumn, stringMessages.boatClass());
        table.addColumn(displayColorColumn, stringMessages.color());
        table.addColumn(imageColumn, stringMessages.imageURL());
        table.addColumn(competitorIdColumn, stringMessages.id());
    }
    
    public Iterable<CompetitorDTO> getAllCompetitors() {
        return filterField.getAll();
    }
    
    public LabeledAbstractFilterablePanel<CompetitorDTO> getFilterField() {
        return filterField;
    }
    
    public void refreshCompetitorList(Iterable<CompetitorDTO> competitors) {
        getFilteredCompetitors(competitors);
    }
    
    public void refreshCompetitorList(String leaderboardName) {
        refreshCompetitorList(leaderboardName, false, null);
    }
    
    public void refreshCompetitorList(String leaderboardName, boolean lookInRaceLogs) {
        refreshCompetitorList(leaderboardName, lookInRaceLogs, null);
    }
    
    /**
     * @param leaderboardName If null, all existing competitors are loaded
     */
    public void refreshCompetitorList(String leaderboardName, boolean lookInRaceLogs, final Callback<Iterable<CompetitorDTO>,
            Throwable> callback) {
        if(leaderboardName != null) {
            sailingService.getCompetitorsOfLeaderboard(leaderboardName, lookInRaceLogs, new AsyncCallback<Iterable<CompetitorDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
                    if (callback != null) callback.onFailure(caught);
                }

                @Override
                public void onSuccess(Iterable<CompetitorDTO> result) {
                    refreshCompetitorList(result);
                    if (callback != null) callback.onSuccess(result);
                }
            });
        } else {
            sailingService.getCompetitors(new AsyncCallback<Iterable<CompetitorDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
                    if (callback != null) callback.onFailure(caught);
                }

                @Override
                public void onSuccess(Iterable<CompetitorDTO> result) {
                    getFilteredCompetitors(result);
                    refreshCompetitorList(result);
                    if (callback != null) callback.onSuccess(result);
                }
            });
        }
    }

    private void getFilteredCompetitors(Iterable<CompetitorDTO> result) {
        filterField.updateAll(result);
    }
}
