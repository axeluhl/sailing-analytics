package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

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
    private final String leaderboardName;
    private LabeledAbstractFilterablePanel<CompetitorDTO> filterField;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public CompetitorPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        this(sailingService, null, stringMessages, errorReporter);
    }

    public CompetitorPanel(final SailingServiceAsync sailingService, String leaderboardName, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        super();
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
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
        final Button allowReloadButton = new Button(stringMessages.allowReload());
        allowReloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                allowUpdate(competitorSelectionModel.getSelectedSet());
            }
        });
        buttonPanel.add(allowReloadButton);
        Button addCompetitorButton = new Button(stringMessages.add());
        addCompetitorButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				openAddCompetitorDialog();
			}
		});
        buttonPanel.add(addCompetitorButton);
        competitorsPanel.add(buttonPanel);

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
        
        ImagesBarColumn<CompetitorDTO, CompetitorConfigImagesBarCell> competitorActionColumn = new ImagesBarColumn<CompetitorDTO, CompetitorConfigImagesBarCell>(
                new CompetitorConfigImagesBarCell(stringMessages));
        competitorActionColumn.setFieldUpdater(new FieldUpdater<CompetitorDTO, String>() {
            @Override
            public void update(int index, final CompetitorDTO competitor, String value) {
                if (CompetitorConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    openEditCompetitorDialog(competitor);
                } else if (CompetitorConfigImagesBarCell.ACTION_REFRESH.equals(value)) {
                    allowUpdate(Collections.singleton(competitor));
                }
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
        competitorTable.addColumn(competitorActionColumn, stringMessages.actions());
        competitorSelectionModel = new MultiSelectionModel<CompetitorDTO>();
        competitorTable.setSelectionModel(competitorSelectionModel);
        mainPanel.add(competitorTable);
        competitorSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                allowReloadButton.setEnabled(!competitorSelectionModel.getSelectedSet().isEmpty());
            }
        });
        allowReloadButton.setEnabled(!competitorSelectionModel.getSelectedSet().isEmpty());
        
        if(leaderboardName != null) {
            refreshCompetitorList();
        }
    }
    
    private void allowUpdate(final Iterable<CompetitorDTO> competitors) {
        List<CompetitorDTO> serializableSingletonList = new ArrayList<CompetitorDTO>();
        Util.addAll(competitors, serializableSingletonList);
        sailingService.allowCompetitorResetToDefaults(serializableSingletonList, new AsyncCallback<Void>() {
            @Override public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to allow resetting competitors "+competitors
                        +" to defaults: "+caught.getMessage());
            }
            @Override public void onSuccess(Void result) {
                Window.alert(stringMessages.successfullyAllowedCompetitorReset(competitors.toString()));
            }
        });
    }

    private void openAddCompetitorDialog() {
        openEditCompetitorDialog(new CompetitorDTOImpl());
    }

    private void openEditCompetitorDialog(CompetitorDTO competitor) {
        new CompetitorEditDialog(stringMessages, competitor, new DialogCallback<CompetitorDTO>() {
            @Override
            public void ok(CompetitorDTO competitor) {
                sailingService.addOrUpdateCompetitor(competitor, new AsyncCallback<CompetitorDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to update competitor: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(CompetitorDTO updatedCompetitor) {
                        // replace updated competitor in competitor list
                    	boolean found = false;
                        for (int i=0; i<allCompetitors.size(); i++) {
                            if (allCompetitors.get(i).getIdAsString().equals(updatedCompetitor.getIdAsString())) {
                            	found = true;
                                allCompetitors.set(i, updatedCompetitor);
                                break;
                            }
                        }
                        if (!found) {
                        	allCompetitors.add(updatedCompetitor);
                        }
                        filterField.updateAll(allCompetitors);
                    }
                });
            }

            @Override
            public void cancel() {
            }
        }).show();
    }

    void refreshCompetitorList() {
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
