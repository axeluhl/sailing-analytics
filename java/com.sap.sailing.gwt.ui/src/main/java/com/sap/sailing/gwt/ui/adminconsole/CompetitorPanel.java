package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;

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
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.Util;
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
    private Button removeCompetitorsButton;
    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public CompetitorPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        super();
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        HorizontalPanel competitorsPanel = new HorizontalPanel();
        competitorsPanel.setSpacing(5);
        mainPanel.add(competitorsPanel);

        removeCompetitorsButton = new Button(stringMessages.remove());
        removeCompetitorsButton.setEnabled(false);
        removeCompetitorsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm(stringMessages.doYouReallyWantToRemoveCompetitors())) {
                    removeCompetitors(competitorSelectionModel.getSelectedSet());
                }
            }
        });
        competitorsPanel.add(removeCompetitorsButton);

        // sailing events table
        TextColumn<CompetitorDTO> competitorNameColumn = new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        };

        TextColumn<CompetitorDTO> boatClassColumn = new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getBoatClass() != null ? competitor.getBoatClass().getName() : "";
            }
        };
        
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

        ImagesBarColumn<CompetitorDTO, CompetitorConfigImagesBarCell> competitorActionColumn = new ImagesBarColumn<CompetitorDTO, CompetitorConfigImagesBarCell>(
                new CompetitorConfigImagesBarCell(stringMessages));
        competitorActionColumn.setFieldUpdater(new FieldUpdater<CompetitorDTO, String>() {
            @Override
            public void update(int index, CompetitorDTO competitor, String value) {
                if (EventConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveCompetitor(competitor.getName()))) {
                        removeCompetitors(Collections.singleton(competitor));
                    }
                } else if (EventConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    openEditCompetitorDialog(competitor);
                }
            }
        });

        competitorTable = new CellTable<CompetitorDTO>(10000, tableRes);
        competitorTable.addColumn(competitorNameColumn, stringMessages.event());
        competitorTable.addColumn(boatClassColumn, stringMessages.venue());
        competitorTable.addColumn(competitorActionColumn, stringMessages.actions());

        competitorSelectionModel = new MultiSelectionModel<CompetitorDTO>();
        competitorSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                removeCompetitorsButton.setEnabled(!competitorSelectionModel.getSelectedSet().isEmpty());
            }
        });
        competitorTable.setSelectionModel(competitorSelectionModel);

        competitorProvider = new ListDataProvider<CompetitorDTO>();
        competitorProvider.addDataDisplay(competitorTable);
        mainPanel.add(competitorTable);

    }
    
    private void openEditCompetitorDialog(CompetitorDTO competitor) {
        // TODO Auto-generated method stub
        
    }

    private void removeCompetitors(Collection<CompetitorDTO> competitors) {
        if (!competitors.isEmpty()) {
            sailingService.removeCompetitors(competitors, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to remove the competitors:" + caught.getMessage());
                }
                @Override
                public void onSuccess(Void result) {
                    refreshCompetitorList();
                }
            });
        }
    }

    private void refreshCompetitorList() {
        sailingService.getCompetitors(new AsyncCallback<Iterable<CompetitorDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                competitorProvider.getList().clear();
                Util.addAll(result, competitorProvider.getList());
            }
        });
    }

}
