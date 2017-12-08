package com.sap.sailing.gwt.ui.pairinglist;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.PairingListDTO;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

public class PairingListEntryPoint extends AbstractSailingEntryPoint {

    private PairingListContextDefinition pairingListContextDefinition;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();

        this.createUI();
    }

    private void createUI() {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);

        SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(
                getStringMessages().pairingLists());
        new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
        mainPanel.addNorth(header, 75);

        pairingListContextDefinition = new SettingsToUrlSerializer()
                .deserializeFromCurrentLocation(new PairingListContextDefinition());

        sailingService.getLeaderboard(pairingListContextDefinition.getLeaderboardName(),
                new AsyncCallback<StrippedLeaderboardDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        try {
                            throw caught;
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSuccess(StrippedLeaderboardDTO result) {
                        PairingListTemplateDTO pairingListTemplateDTO = new PairingListTemplateDTO(
                                result.competitorsCount, pairingListContextDefinition.getFlightMultiplier());

                        sailingService.getPairingListFromTemplate(pairingListTemplateDTO, result.getName(),
                                new AsyncCallback<PairingListDTO>() {

                                    @Override
                                    public void onSuccess(PairingListDTO result) {
                                        mainPanel.add(createPairingListPanel(result));
                                    }

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        try {
                                            throw caught;
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                });
    }

    private Widget createPairingListPanel(PairingListDTO pairingListDTO) {
        HorizontalPanel pairingListPanel = new HorizontalPanel();
        pairingListPanel.ensureDebugId("PairingListPanel");
        pairingListPanel.setWidth("100%");
        pairingListPanel.setHeight("100%");
        pairingListPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        pairingListPanel.getElement().getStyle().setProperty("marginTop", "15px");
        pairingListPanel.getElement().getStyle().setProperty("position", "relative");
        pairingListPanel.getElement().getStyle().setProperty("height", "100%");
        pairingListPanel.getElement().getStyle().setProperty("overflow", "scroll");

        final int flightCount = pairingListDTO.getPairingList().size();
        final int groupCount = pairingListDTO.getPairingList().get(0).size();
        final int boatCount = pairingListDTO.getPairingList().get(0).get(0).size();

        Grid pairingListGrid = new Grid(flightCount * groupCount + 1, boatCount + 1);
        pairingListGrid.setCellSpacing(20);

        int groupCounter = 1;
        int boatCounter = 1;

        for (int i = 1; i <= boatCount; i++) {
            pairingListGrid.setWidget(0, i, new Label(getStringMessages().boat() + " " + String.valueOf(i)));
            pairingListGrid.getCellFormatter().getElement(0, i).getStyle().setTextAlign(TextAlign.CENTER);
            pairingListGrid.getCellFormatter().getElement(0, i).getStyle().setPadding(15, Unit.PX);
            pairingListGrid.getCellFormatter().getElement(0, i).getStyle().setBackgroundColor(
                    pairingListDTO.getPairingList().get(0).get(0).get(i - 1).getB().getColor().getAsHtml());
        }

        String color = "";

        for (List<List<Pair<CompetitorDTO, BoatDTO>>> flight : pairingListDTO.getPairingList()) {

            color = (color.equals("none") ? "#cecece" : "none");

            for (List<Pair<CompetitorDTO, BoatDTO>> group : flight) {
                // setting up fleet
                pairingListGrid.setWidget(groupCounter, 0,
                        new Label(getStringMessages().fleet() + " " + String.valueOf(groupCounter)));

                // setting up fleets style
                pairingListGrid.getCellFormatter().getElement(groupCounter, 0).getStyle().setPadding(5, Unit.PX);
                pairingListGrid.getCellFormatter().getElement(groupCounter, 0).getStyle().setBackgroundColor(color);

                boatCounter = 1;
                for (Pair<CompetitorDTO, BoatDTO> competitorDTO : group) {
                    // setting competitor element
                    pairingListGrid.setWidget(groupCounter, boatCounter, new Label(competitorDTO.getA().getSailID()));

                    // setting style of competitor elements
                    pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter).getStyle()
                            .setFontWeight(Style.FontWeight.BOLD);
                    pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter).getStyle()
                            .setTextAlign(TextAlign.CENTER);
                    pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter).getStyle().setPadding(8,
                            Unit.PX);
                    pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter).getStyle()
                            .setBackgroundColor(color);

                    boatCounter++;
                }
                groupCounter++;
            }
        }

        pairingListPanel.add(pairingListGrid);

        return pairingListPanel;
    }
}
