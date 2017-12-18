package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class PairingListCreationDialog extends DataEntryDialog<PairingListTemplateDTO> {

    private final PairingListTemplateDTO template;
    private final SailingServiceAsync sailingService;
    private final StrippedLeaderboardDTO leaderboardDTO;
    private final StringMessages stringMessages;

    private final Button applyToRacelogButton;
    private final Button cSVExportButton;
    private final Button printViewButton;

    public PairingListCreationDialog(StrippedLeaderboardDTO leaderboardDTO, final StringMessages stringMessages,
            PairingListTemplateDTO template, SailingServiceAsync sailingService) {
        super(stringMessages.pairingList(), null, stringMessages.close(), null, null, null);
        this.stringMessages = stringMessages;
        this.template = template;
        this.sailingService = sailingService;
        this.leaderboardDTO = leaderboardDTO;
        this.ensureDebugId("PairingListCreationDialog");

        applyToRacelogButton = new Button(stringMessages.applyToRacelog());
        cSVExportButton = new Button(stringMessages.csvExport());
        printViewButton = new Button(stringMessages.printView());

        if (template.getCompetitorCount() != leaderboardDTO.competitorsCount) {
            this.disableApplyToRacelogs();
        }
    }

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel panel = new HorizontalPanel();

        /* DATA PANEL */

        CaptionPanel dataPanel = new CaptionPanel();
        dataPanel.setCaptionText(stringMessages.dataOfPairingList());

        Grid formGrid = new Grid(5, 2);
        dataPanel.add(formGrid);
        Label flights = new Label(String.valueOf(this.template.getFlightCount()));
        flights.ensureDebugId("FlightCountLabel");
        Label groups = new Label(String.valueOf(this.template.getGroupCount()));
        groups.ensureDebugId("GroupCountLabel");
        Label competitors = new Label(String.valueOf(this.template.getCompetitorCount()));
        competitors.ensureDebugId("CompetitorCountLabel");
        formGrid.setWidget(0, 0, new Label(stringMessages.numberOfFlights()));
        formGrid.setWidget(0, 1, flights);
        formGrid.setWidget(1, 0, new Label(stringMessages.numberOfGroups()));
        formGrid.setWidget(1, 1, groups);
        formGrid.setWidget(2, 0, new Label(stringMessages.numberOfCompetitors()));
        formGrid.setWidget(2, 1, competitors);
        formGrid.setWidget(3, 0, new Label(stringMessages.quality()));
        formGrid.setWidget(3, 1, new Label(String.valueOf(Math.floor(this.template.getQuality() * 1000) / 1000)));
        if (this.template.getFlightMultiplier() > 1) {
            Label flightMultiplierLabel = new Label(String.valueOf(this.template.getFlightMultiplier()));
            formGrid.setWidget(4, 0, new Label(stringMessages.flightMultiplier()));
            formGrid.setWidget(4, 1, flightMultiplierLabel);
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
        cSVExportButton.getElement().getStyle().setMargin(3, Unit.PX);
        cSVExportButton.ensureDebugId("CSVExportButton");
        printViewButton.getElement().getStyle().setMargin(3, Unit.PX);
        printViewButton.ensureDebugId("printViewButton");
        getRightButtonPannel().add(applyToRacelogButton);
        getRightButtonPannel().add(cSVExportButton);
        getRightButtonPannel().add(printViewButton);
        getRightButtonPannel().add(new HTML(stringMessages.printHint()));
        if (!applyToRacelogButton.isEnabled()) {
            Label label = new Label(stringMessages.blockedApplyButton());
            label.getElement().getStyle().setColor("red");
            getRightButtonPannel().add(label);
        }
        applyToRacelogButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                sailingService.fillRaceLogsFromPairingListTemplate(leaderboardDTO.getName(),
                        template.getFlightMultiplier(), template.getSelectedFlightNames(), new AsyncCallback<Void>() {

                            @Override
                            public void onSuccess(Void result) {
                                System.out.println("it worked ;-)");
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                caught.printStackTrace();
                            }
                        });
            }

        });
        cSVExportButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                downloadPairingListTemplate(getCSVFromPairingListTemplate(getResult().getPairingListTemplate()));
            }

        });
        printViewButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                String link = EntryPointLinkFactory.createPairingListLink(createLinkParameters());
                Window.open(link, "", "");
            }
        });
    }

    private native void downloadPairingListTemplate(String pairingListCSV)/*-{
		var dummy = document.createElement('a');
		dummy.setAttribute('href', 'data:text/plain;charset=utf-8,'
				+ encodeURIComponent(pairingListCSV));
		dummy.setAttribute('download', "pairingListTemplate.csv");
		document.body.appendChild(dummy);
		dummy.click();
		document.body.removeChild(dummy);
    }-*/;

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

    private void disableApplyToRacelogs() {
        this.applyToRacelogButton.setEnabled(false);
    }

    private Map<String, String> createLinkParameters() {
        Map<String, String> result = new HashMap<>();
        result.put("leaderboardName", leaderboardDTO.getName());
        result.put("flightMultiplier", String.valueOf(template.getFlightMultiplier()));
        result.put("selectedFlights", String.join(",", template.getSelectedFlightNames()));
        return result;
    }

}
