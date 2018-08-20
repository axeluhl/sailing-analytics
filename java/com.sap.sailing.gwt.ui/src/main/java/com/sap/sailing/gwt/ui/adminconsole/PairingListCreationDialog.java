package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.PairingListDTO;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class PairingListCreationDialog extends DataEntryDialog<PairingListTemplateDTO> {

    private final PairingListTemplateDTO template;
    private final SailingServiceAsync sailingService;
    private final StrippedLeaderboardDTO leaderboardDTO;
    private final StringMessages stringMessages;

    private PairingListDTO pairingListDTO;

    private final AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private final Button applyToRacelogButton, printPreViewButton, refreshButton;
    private final Anchor cSVExportAnchor;
    private final ErrorReporter errorReporter;

    public PairingListCreationDialog(StrippedLeaderboardDTO leaderboardDTO, final StringMessages stringMessages,
            PairingListTemplateDTO template, SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        super(/* title */ stringMessages.pairingList(), /* message */ null, stringMessages.close(), /* cancel button name */ null,
                /* validator */ null, /* callback */ null);
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.template = template;
        this.sailingService = sailingService;
        this.leaderboardDTO = leaderboardDTO;
        this.ensureDebugId("PairingListCreationDialog");
        applyToRacelogButton = new Button(stringMessages.insertIntoRegatta());
        printPreViewButton = new Button(stringMessages.printView());
        refreshButton = new Button(stringMessages.recalculate());
        cSVExportAnchor = new Anchor(stringMessages.csvExport());
        cSVExportAnchor.ensureDebugId("CSVExportAnchor");
        cSVExportAnchor.getElement().setAttribute("href",
                "data:text/plain;charset=utf-8," + getCSVFromPairingListTemplate(getResult().getPairingListTemplate()));
        cSVExportAnchor.getElement().setAttribute("download", "pairingListTemplate.csv");
        if (template.getCompetitorCount() != leaderboardDTO.competitorsCount) {
            this.disableApplyToRacelogsAndPrintPreview();
        }
        sailingService.getPairingListFromTemplate(this.leaderboardDTO.getName(), this.template.getFlightMultiplier(),
                this.template.getSelectedFlightNames(), this.template, new AsyncCallback<PairingListDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        PairingListCreationDialog.this.errorReporter.reportError(stringMessages.errorCreatingPairingList(caught.getMessage()), /* silent */ true);
                        pairingListDTO = null;
                        applyToRacelogButton.setEnabled(false);
                        printPreViewButton.setEnabled(false);
                    }

                    @Override
                    public void onSuccess(PairingListDTO result) {
                        pairingListDTO = result;
                    }
                });
    }

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel panel = new HorizontalPanel();

        /* DATA PANEL */

        CaptionPanel dataPanel = new CaptionPanel();
        dataPanel.setCaptionText(stringMessages.parameters());
        Grid formGrid = new Grid(8, 2);
        dataPanel.add(formGrid);
        Label flights = new Label(String.valueOf(this.template.getFlightCount()));
        flights.ensureDebugId("FlightCountLabel");
        Label groups = new Label(String.valueOf(this.template.getGroupCount()));
        groups.ensureDebugId("GroupCountLabel");
        Label competitors = new Label(String.valueOf(this.template.getCompetitorCount()));
        competitors.ensureDebugId("CompetitorCountLabel");
        formGrid.setWidget(0, 0, new Label(stringMessages.numberOfFlights()));
        formGrid.setWidget(0, 1, flights);
        formGrid.setWidget(1, 0, new Label(stringMessages.numberOfFleets()));
        formGrid.setWidget(1, 1, groups);
        formGrid.setWidget(2, 0, new Label(stringMessages.numberOfRaces()));
        formGrid.setWidget(2, 1, new Label(String.valueOf((template.getFlightCount() * template.getGroupCount()))));
        formGrid.setWidget(3, 0, new Label(stringMessages.numberOfCompetitors()));
        formGrid.setWidget(3, 1, competitors);
        HorizontalPanel qualityPanel = new HorizontalPanel();
        qualityPanel.add(new Label(stringMessages.quality()));
        Image qualityHelpImage = new Image(resources.help());
        qualityPanel.add(qualityHelpImage);
        qualityHelpImage.getElement().getStyle().setMarginLeft(10, Unit.PX);
        qualityHelpImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://wiki.sapsailing.com/wiki/howto/eventmanagers/Pairing-Lists#pairing-list_general_quality", "", "");
            }
        });
        formGrid.setWidget(4, 0, qualityPanel);
        formGrid.setWidget(4, 1, new Label(String.valueOf(Math.floor(this.template.getQuality() * 1000) / 1000)));
        //TODO Stringmessages
        formGrid.setWidget(5, 0, new Label("Boat Assignment Quality"));
        formGrid.setWidget(5, 1, new Label(String.valueOf(Math.floor(this.template.getBoatAssignmentQuality()*1000) / 1000)));
        formGrid.setWidget(6, 0, new Label("Boat Changes"));
        formGrid.setWidget(6, 1, new Label(String.valueOf(template.getBoatChanges())));
        if (this.template.getFlightMultiplier() > 1) {
            Label flightMultiplierLabel = new Label(String.valueOf(this.template.getFlightMultiplier()));
            formGrid.setWidget(7, 0, new Label(stringMessages.amountOfFlightRepeats()));
            formGrid.setWidget(7, 1, flightMultiplierLabel);
            flightMultiplierLabel.ensureDebugId("FlightMultiplierCountLabel");
        }
        formGrid.setCellSpacing(10);
        panel.add(dataPanel);

        /* PAIRING LIST TEMPLATE PANEL */
        CaptionPanel pairingListTemplatePanel = new CaptionPanel();
        pairingListTemplatePanel.setCaptionText(stringMessages.pairingListTemplate());
        Grid pairingListGrid = new Grid(this.template.getPairingListTemplate().length,
                this.template.getPairingListTemplate()[0].length);
        pairingListGrid.setCellSpacing(5);
        ScrollPanel scrollPanel = new ScrollPanel(pairingListGrid);
        scrollPanel.setPixelSize((Window.getClientWidth() / 4), (Window.getClientHeight() / 3));
        pairingListTemplatePanel.add(scrollPanel);
        for (int groupIndex = 0; groupIndex < this.template.getPairingListTemplate().length; groupIndex++) {
            for (int boatIndex = 0; boatIndex < this.template.getPairingListTemplate()[0].length; boatIndex++) {
                pairingListGrid.setWidget(groupIndex, boatIndex,
                        new Label(String.valueOf(this.template.getPairingListTemplate()[groupIndex][boatIndex] + 1)));
                pairingListGrid.getCellFormatter().setWidth(groupIndex, boatIndex, "50px");
            }
        }
        panel.add(pairingListTemplatePanel);
        configButtons();

        return panel;
    }

    protected PairingListTemplateDTO getResult() {
        return this.template;
    }

    private void configButtons() {
        getRightButtonPannel().remove(getCancelButton());
        applyToRacelogButton.getElement().getStyle().setMargin(3, Unit.PX);
        applyToRacelogButton.ensureDebugId("ApplyToRacelogButton");
        printPreViewButton.getElement().getStyle().setMargin(3, Unit.PX);
        printPreViewButton.ensureDebugId("printViewButton");
        refreshButton.getElement().getStyle().setMargin(3, Unit.PX);
        refreshButton.ensureDebugId("printViewButton");
        getRightButtonPannel().add(applyToRacelogButton);
        getRightButtonPannel().add(printPreViewButton);
        getRightButtonPannel().add(refreshButton);
        getRightButtonPannel().add(cSVExportAnchor);
        if (!applyToRacelogButton.isEnabled()) {
            Label label = new Label(stringMessages.blockedApplyButton());
            label.getElement().getStyle().setColor("red");
            getRightButtonPannel().add(label);
        }
        applyToRacelogButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sailingService.fillRaceLogsFromPairingListTemplate(leaderboardDTO.getName(),
                        template.getFlightMultiplier(), template.getSelectedFlightNames(), pairingListDTO,
                        new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Notification.notify(stringMessages.successfullyFilledRaceLogsFromPairingList(), NotificationType.SUCCESS);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorFillingRaceLogsFromPairingList(caught.getMessage()));
                            }
                        });
            }
        });
        printPreViewButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sailingService.getRaceDisplayNamesFromLeaderboard(leaderboardDTO.getName(),
                        Util.asList(template.getSelectedFlightNames()), new AsyncCallback<List<String>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorFetchingRaceDisplayNamedFromLeaderboard(caught.getMessage()));
                            }

                            public void onSuccess(List<String> raceDisplayNames) {
                                PairingListPreviewDialog dialog = new PairingListPreviewDialog(pairingListDTO, raceDisplayNames, stringMessages, leaderboardDTO.getName());
                                dialog.show();
                            };
                        });
            }
        });
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getDialogBox().hide();
                BusyDialog busyDialog = new BusyDialog();
                busyDialog.show();
                try {
                    sailingService.calculatePairingListTemplate(template.getFlightCount(), template.getGroupCount(),
                            template.getCompetitorCount(), template.getFlightMultiplier(), template.getTolerance(),
                            new AsyncCallback<PairingListTemplateDTO>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    busyDialog.hide();
                                    System.out.println(caught);
                                }

                                @Override
                                public void onSuccess(PairingListTemplateDTO result) {
                                    busyDialog.hide();
                                    result.setSelectedFlightNames(template.getSelectedFlightNames());
                                    PairingListCreationDialog dialog = new PairingListCreationDialog(leaderboardDTO,
                                            stringMessages, result, sailingService, errorReporter);
                                    dialog.show();
                                }
                            });
                } catch (Exception exception) {
                    errorReporter.reportError(exception.getMessage());
                }
            }
        });
    }

    private String getCSVFromPairingListTemplate(int[][] pairingListTemplate) {
        StringBuilder result = new StringBuilder();
        for (int[] row : pairingListTemplate) {
            for (int column : row) {
                result.append((column + 1) + ",");
            }
            result.append("\n");
        }
        return result.toString();
    }

    private void disableApplyToRacelogsAndPrintPreview() {
        this.applyToRacelogButton.setEnabled(false);
        printPreViewButton.setEnabled(false);
    }

}
