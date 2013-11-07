package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Allows an administrator to view and edit the set of competitors currently maintained by the server.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorPanel extends SimplePanel {
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private CellTable<CompetitorDTO> competitorTable;
    private MultiSelectionModel<CompetitorDTO> competitorSelectionModel;
    private ListDataProvider<CompetitorDTO> competitorProvider;
    private List<CompetitorDTO> allCompetitors;
    private TextBox filterField;
    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public CompetitorPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        super();
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        competitorProvider = new ListDataProvider<CompetitorDTO>();
        ListHandler<CompetitorDTO> competitorColumnListHandler = new ListHandler<CompetitorDTO>(competitorProvider.getList());
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        HorizontalPanel competitorsPanel = new HorizontalPanel();
        competitorsPanel.setSpacing(5);
        mainPanel.add(competitorsPanel);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshCompetitorList();
            }
        });
        buttonPanel.add(refreshButton);
        competitorsPanel.add(buttonPanel);

        // sailing events table
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
                return o1.getBoatClass().getName().compareTo(o2.getBoatClass().getName());
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
                return o1.getSailID().compareTo(o2.getSailID());
            }
        });

        ImagesBarColumn<CompetitorDTO, CompetitorConfigImagesBarCell> competitorActionColumn = new ImagesBarColumn<CompetitorDTO, CompetitorConfigImagesBarCell>(
                new CompetitorConfigImagesBarCell(stringMessages));
        competitorActionColumn.setFieldUpdater(new FieldUpdater<CompetitorDTO, String>() {
            @Override
            public void update(int index, final CompetitorDTO competitor, String value) {
                if (CompetitorConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    openEditCompetitorDialog(competitor);
                } else if (CompetitorConfigImagesBarCell.ACTION_REFRESH.equals(value)) {
                    sailingService.allowCompetitorResetToDefaults(competitor, new AsyncCallback<Void>() {
                        @Override public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error trying to allow resetting competitor "+competitor.getName()
                                    +" to defaults: "+caught.getMessage());
                        }
                        @Override public void onSuccess(Void result) {}
                    });
                }
            }
        });

        HorizontalPanel filterBoxWithLabel = new HorizontalPanel();
        filterBoxWithLabel.setSpacing(5);
        filterBoxWithLabel.add(new Label(stringMessages.filterCompetitors()));
        filterField = new TextBox();
        filterField.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                applyFilter();
            }
        });
        filterBoxWithLabel.add(filterField);
        mainPanel.add(filterBoxWithLabel);
        competitorTable = new CellTable<CompetitorDTO>(10000, tableRes);
        competitorProvider.addDataDisplay(competitorTable);
        competitorTable.addColumnSortHandler(competitorColumnListHandler);
        competitorTable.addColumn(sailIdColumn, stringMessages.sailNumber());
        competitorTable.addColumn(competitorNameColumn, stringMessages.name());
        competitorTable.addColumn(boatClassColumn, stringMessages.boatClass());
        competitorTable.addColumn(competitorActionColumn, stringMessages.actions());
        competitorSelectionModel = new MultiSelectionModel<CompetitorDTO>();
        competitorTable.setSelectionModel(competitorSelectionModel);
        mainPanel.add(competitorTable);

    }
    
    private void openEditCompetitorDialog(CompetitorDTO competitor) {
        new CompetitorEditDialog(stringMessages, competitor, new DialogCallback<CompetitorDTO>() {
            @Override
            public void ok(CompetitorDTO competitor) {
                sailingService.updateCompetitor(competitor, new AsyncCallback<CompetitorDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to update competitor: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(CompetitorDTO updatedCompetitor) {
                        // replace updated competitor in competitor list
                        for (int i=0; i<allCompetitors.size(); i++) {
                            if (allCompetitors.get(i).getIdAsString().equals(updatedCompetitor.getIdAsString())) {
                                allCompetitors.set(i, updatedCompetitor);
                                applyFilter();
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public void cancel() {
            }
        }).show();
    }

    void refreshCompetitorList() {
        sailingService.getCompetitors(new AsyncCallback<Iterable<CompetitorDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                allCompetitors = new ArrayList<CompetitorDTO>();
                for (CompetitorDTO c : result) {
                    allCompetitors.add(c);
                }
                applyFilter();
            }
        });
    }

    private void applyFilter() {
        competitorProvider.getList().clear();
        for (CompetitorDTO competitor : allCompetitors) {
            if (filterMatches(competitor)) {
                competitorProvider.getList().add(competitor);
            }
        }
    }

    private boolean filterMatches(CompetitorDTO competitor) {
        if (!getFilterText().trim().isEmpty()) {
            String[] filterWords = getFilterText().split(" ");
            String[] matchAgainsts = new String[] { competitor.getName(), competitor.getBoatClass().getName(), competitor.getSailID() };
            outer: for (String filterWord : filterWords) {
                for (String matchAgainst : matchAgainsts) {
                    if (matchAgainst.toLowerCase().indexOf(filterWord.trim().toLowerCase()) >= 0) {
                        continue outer; // found a match
                    }
                }
                return false; // no match found for filterWord; filter does not match
            }
        }
        return true; // found a match for all filter words
    }

    private String getFilterText() {
        return filterField.getText();
    }

}
