package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.panels.LabeledAbstractFilterablePanel;

public class CompetitorTableWrapper implements IsWidget {
    private final CellTable<CompetitorDTO> competitorTable;
    private final MultiSelectionModel<CompetitorDTO> competitorSelectionModel;
    private final ListDataProvider<CompetitorDTO> competitorProvider;
    private List<CompetitorDTO> allCompetitors;
    private final LabeledAbstractFilterablePanel<CompetitorDTO> filterField;
    private final VerticalPanel mainPanel;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    
    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    public CompetitorTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        mainPanel = new VerticalPanel();
        competitorProvider = new ListDataProvider<CompetitorDTO>();
        ListHandler<CompetitorDTO> competitorColumnListHandler = new ListHandler<CompetitorDTO>(competitorProvider.getList());
        
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
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return new NaturalComparator(false).compare(o1.getBoatClass().getName(), o2.getBoatClass().getName());
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
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return new NaturalComparator(false).compare(o1.getSailID(), o2.getSailID());
            }
        });

        Column<CompetitorDTO, SafeHtml> displayColorColumn = new Column<CompetitorDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (competitor.getColor() != null) {
                    sb.appendHtmlConstant("<span style=\"color: " + competitor.getColor() + ";\">");
                    sb.appendHtmlConstant(competitor.getColor().getAsHtml());
                    sb.appendHtmlConstant("</span>");
                } else {
                    sb.appendHtmlConstant("&nbsp;");
                }
                return sb.toSafeHtml();
            }
        };

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
        competitorTable = new CellTable<CompetitorDTO>(10000, tableRes);
        filterField = new LabeledAbstractFilterablePanel<CompetitorDTO>(new Label(stringMessages.filterCompetitors()),
                allCompetitors, competitorTable, competitorProvider) {

            @Override
            public Iterable<String> getSearchableStrings(CompetitorDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                string.add(t.getSailID());
                string.add(t.getBoatClass().getName());
                return string;
            }
        };

        mainPanel.add(filterField);
        competitorProvider.addDataDisplay(competitorTable);
        competitorTable.addColumnSortHandler(competitorColumnListHandler);
        competitorTable.addColumn(sailIdColumn, stringMessages.sailNumber());
        competitorTable.addColumn(competitorNameColumn, stringMessages.name());
        competitorTable.addColumn(boatClassColumn, stringMessages.boatClass());
        competitorTable.addColumn(displayColorColumn, stringMessages.color());
        competitorTable.addColumn(competitorIdColumn, stringMessages.id());
        competitorSelectionModel = new MultiSelectionModel<CompetitorDTO>();
        competitorTable.setSelectionModel(competitorSelectionModel);
        mainPanel.add(competitorTable);
    }
    
    public CellTable<CompetitorDTO> getTable() {
        return competitorTable;
    }
    
    public MultiSelectionModel<CompetitorDTO> getSelectionModel() {
        return competitorSelectionModel;
    }
    
    public List<CompetitorDTO> getAllCompetitors() {
        return allCompetitors;
    }
    
    public LabeledAbstractFilterablePanel<CompetitorDTO> getFilterField() {
        return filterField;
    }
    
    /**
     * @param leaderboardName If null, all existing competitors are loaded
     */
    public void refreshCompetitorList(String leaderboardName) {
        if(leaderboardName != null) {
            sailingService.getCompetitorsOfLeaderboard(leaderboardName, new AsyncCallback<Iterable<CompetitorDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Iterable<CompetitorDTO> result) {
                    getFilteredCompetitors(result);
                }
            });
        } else {
            sailingService.getCompetitors(new AsyncCallback<Iterable<CompetitorDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Iterable<CompetitorDTO> result) {
                    getFilteredCompetitors(result);
                }
            });
        }
    }

    private void getFilteredCompetitors(Iterable<CompetitorDTO> result) {
        allCompetitors = new ArrayList<CompetitorDTO>();
        for (CompetitorDTO c : result) {
            allCompetitors.add(c);
        }
        filterField.updateAll(allCompetitors);
    }
}
