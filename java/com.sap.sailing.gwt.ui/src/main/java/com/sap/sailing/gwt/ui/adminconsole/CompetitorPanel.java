package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

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
    private final CompetitorTableWrapper<MultiSelectionModel<CompetitorDTO>> competitorTable;
    private final MultiSelectionModel<CompetitorDTO> competitorSelectionModel;
    private final String leaderboardName;

    public CompetitorPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        this(sailingService, null, stringMessages, errorReporter);
    }

    public CompetitorPanel(final SailingServiceAsync sailingService, final String leaderboardName,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        super();
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
        this.competitorTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter,
                new MultiSelectionModel<CompetitorDTO>(), false);
        this.competitorSelectionModel = competitorTable.getSelectionModel();
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

        final Button inviteCompetitorsButton = new Button(stringMessages.inviteCompetitors());
        inviteCompetitorsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Iterable<CompetitorDTO> competitors = competitorSelectionModel.getSelectedSet();
                boolean emailProvidedForAll = isEmailProvidedForAll(competitors);

                if (emailProvidedForAll) {
                    openChooseEventDialogAndSendMails(competitors);
                } else {
                    Window.alert(stringMessages.notAllCompetitorsProvideEmail());
                }
            }

            private boolean isEmailProvidedForAll(Iterable<CompetitorDTO> allCompetitors) {
                for (CompetitorDTO competitor : allCompetitors) {
                    if (!competitor.hasEmail()) {
                        return false;
                    }
                }

                return true;
            }
        });
        buttonPanel.add(inviteCompetitorsButton);

        competitorsPanel.add(buttonPanel);

        // competitor table
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

        competitorTable.getTable().addColumn(competitorActionColumn, stringMessages.actions());
        mainPanel.add(competitorTable);
        competitorSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                allowReloadButton.setEnabled(!competitorSelectionModel.getSelectedSet().isEmpty());
            }
        });
        allowReloadButton.setEnabled(!competitorSelectionModel.getSelectedSet().isEmpty());

        if (leaderboardName != null) {
            refreshCompetitorList();
        }
    }

    private void allowUpdate(final Iterable<CompetitorDTO> competitors) {
        List<CompetitorDTO> serializableSingletonList = new ArrayList<CompetitorDTO>();
        Util.addAll(competitors, serializableSingletonList);
        sailingService.allowCompetitorResetToDefaults(serializableSingletonList, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to allow resetting competitors " + competitors
                        + " to defaults: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
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
                        errorReporter.reportError("Error trying to update competitor: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(CompetitorDTO updatedCompetitor) {
                        refreshCompetitorList();
                    }
                });
            }

            @Override
            public void cancel() {
            }
        }).show();
    }

    private void openChooseEventDialogAndSendMails(final Iterable<CompetitorDTO> competitors) {
        EventListCompositeDialog dialog = new EventListCompositeDialog(sailingService, stringMessages, errorReporter,
                new DialogCallback<List<java.util.UUID>>() {

                    @Override
                    public void ok(List<UUID> eventsToInviteTo) {
                        sailingService.sendInvitationEmailToCompetitors(competitors, leaderboardName, eventsToInviteTo, new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringMessages.sendingMailsFailed()+caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void result) {
                                Window.alert(stringMessages.sendingMailsSuccessfull());
                            }
                        });

                    }

                    @Override
                    public void cancel() {
                    }
                });
        dialog.show();
    }

    public void refreshCompetitorList() {
        competitorTable.refreshCompetitorList(leaderboardName);
    }
}
