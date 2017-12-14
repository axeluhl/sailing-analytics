package com.sap.sailing.gwt.ui.pairinglist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.dto.PairingListDTO;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

public class PairingListEntryPoint extends AbstractSailingEntryPoint {

    private PairingListContextDefinition pairingListContextDefinition;

    private StringMessages stringmessages = StringMessages.INSTANCE;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();

        this.createUI();
    }

    private void createUI() {
        VerticalPanel mainPanel = new VerticalPanel();
        RootLayoutPanel.get().add(new ScrollPanel(mainPanel));

        mainPanel.setWidth("100%");
        mainPanel.setHeight("100%");

        pairingListContextDefinition = new SettingsToUrlSerializer()
                .deserializeFromCurrentLocation(new PairingListContextDefinition());

        SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(
                pairingListContextDefinition.getLeaderboardName());
        new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
        // mainPanel.addNorth(header, 75);
        mainPanel.add(header);

        Button btn = new Button("Print");

        HorizontalPanel btnPanel = new HorizontalPanel();
        btnPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        btnPanel.add(btn);
        btnPanel.setWidth("100%");
        btnPanel.getElement().getStyle().setProperty("marginTop", "15px");
        btnPanel.getElement().getStyle().setProperty("marginBottom", "15px");
        mainPanel.add(btnPanel);

        sailingService.getPairingListFromRaceLogs(pairingListContextDefinition.getLeaderboardName(), new AsyncCallback<PairingListDTO>() {

                    @Override
                    public void onSuccess(PairingListDTO result) {
                        VerticalPanel pairingListPanel = createPairingListPanel(result);
                        mainPanel.add(pairingListPanel);
                        btn.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                printPairingListGrid(/* header.asWidget().getElement().getInnerHTML() + */
                                        pairingListPanel.asWidget().getElement().getInnerHTML());
                            }
                        });
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

    private VerticalPanel createPairingListPanel(PairingListDTO pairingListDTO) {
        VerticalPanel pairingListPanel = new VerticalPanel();
        pairingListPanel.ensureDebugId("PairingListPanel");
        pairingListPanel.setWidth("100%");
        pairingListPanel.setHeight("100%");
        pairingListPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        pairingListPanel.getElement().getStyle().setProperty("marginBottom", "15px");
        
        final List<BoatDTO> boats = pairingListDTO.getBoats();

        final int flightCount = pairingListDTO.getPairingList().size();
        final int groupCount = pairingListDTO.getPairingList().get(0).size();
        final int boatCount = boats.size();
        
        Grid pairingListGrid = new Grid(flightCount * groupCount + 1, (boatCount + 2));
        pairingListGrid.getElement().setId("grid");
        pairingListGrid.setCellPadding(15);

        int flightIndexInGrid = 1;
        int groupIndex = 1;
        int boatIndex = 0;
        
        for (BoatDTO boat : boats) {
            pairingListGrid.setWidget(0, boatIndex + 2, new Label(boat.getName()));
            pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setTextAlign(TextAlign.CENTER);
            pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setPadding(10, Unit.PX);
            pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setBackgroundColor(
                    boat.getColor().getAsHtml());
            boatIndex++;
        }
        
        String color = "";

        pairingListGrid.getCellFormatter().getElement(0, 0).getStyle().setBackgroundColor("#cecece");
        pairingListGrid.getCellFormatter().getElement(0, 1).getStyle().setBackgroundColor("#cecece");

        for (List<List<Pair<CompetitorDTO, BoatDTO>>> flight : pairingListDTO.getPairingList()) {

            color = (color.equals("none") ? "#cecece" : "none");

            // setting up race
            int currentRaceInGridCells = (((flightIndexInGrid - 1) * groupCount) + 1);
            pairingListGrid.setWidget(currentRaceInGridCells, 0,
                    new Label(getStringMessages().race() + " " + String.valueOf(flightIndexInGrid)));

            pairingListGrid.getCellFormatter().getElement(currentRaceInGridCells, 0).getStyle().setPadding(5, Unit.PX);
            pairingListGrid.getCellFormatter().getElement(currentRaceInGridCells, 0).getStyle()
                    .setBackgroundColor(color);

            for (List<Pair<CompetitorDTO, BoatDTO>> group : flight) {
                // setting up fleet
                pairingListGrid.getCellFormatter().getElement(groupIndex, 0).getStyle().setPadding(3, Unit.PX);
                pairingListGrid.getCellFormatter().getElement(groupIndex, 0).getStyle().setBackgroundColor(color);

                pairingListGrid.setWidget(groupIndex, 1,
                        new Label(getStringMessages().fleet() + " " + String.valueOf(groupIndex)));

                // setting up fleets style
                pairingListGrid.getCellFormatter().getElement(groupIndex, 1).getStyle().setPadding(3, Unit.PX);
                pairingListGrid.getCellFormatter().getElement(groupIndex, 1).getStyle().setBackgroundColor(color);
                
                if (group.size() < boatCount) {
                    List<BoatDTO> boatsToRemove = new ArrayList<>(boats);
                    for (Pair<CompetitorDTO, BoatDTO> competitorAndBoatPair : group) {
                        boatsToRemove.remove(competitorAndBoatPair.getB());
                    }
                    for (BoatDTO boat : boatsToRemove) {
                        group.add(new Pair<CompetitorDTO, BoatDTO>(new CompetitorDTOImpl(), boat));
                    }
                }
                
                
                for (Pair<CompetitorDTO, BoatDTO> competitorAndBoatPair : group) {
                    int boatIndexInGrid = boats.indexOf(competitorAndBoatPair.getB()) + 2;
                    if (competitorAndBoatPair.getA().getName() == null) {
                        // TODO set empty elements
                        pairingListGrid.setWidget(groupIndex, boatIndexInGrid, new Label(stringmessages.empty()));
                        pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                                .setColor(Color.RED.toString());
                    } else {
                        // TODO change competitor name to competitor shorthand symbol
                        pairingListGrid.setWidget(groupIndex, boatIndexInGrid,
                                new Label(competitorAndBoatPair.getA().getName()));
                    }
                    pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                            .setFontWeight(Style.FontWeight.BOLD);
                    pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                            .setTextAlign(TextAlign.CENTER);
                    pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                            .setPadding(5, Unit.PX);
                    pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                            .setBackgroundColor(color);
                }
                
                groupIndex++;
            }
            flightIndexInGrid++;
        }

        ScrollPanel result = new ScrollPanel();
        result.add(pairingListPanel);
        pairingListPanel.add(pairingListGrid);

        return pairingListPanel;
    }

    public native void printPairingListGrid(String html) /*-{
		var frame = $doc.getElementById('__gwt_historyFrame');
		frame = frame.contentWindow;
		var doc = frame.document;
		doc.open();
		doc.write(html);

		//adding style to doc
		var css = "body { background: #fff; font-family: 'Open Sans', Arial, Verdana, sans-serif;"
				+ "line-height: 1; font-weight: 400; margin: 0; }"
				+ "table { border-collapse: collapse; border: 0}"
				+ "td { font-size: 15px; }", head = doc.head
				|| doc.getElementsByTagName('head')[0], style = doc
				.createElement('style');
		style.type = 'text/css';
		if (style.styleSheet) {
			style.styleSheet.cssText = css;
		} else {
			style.appendChild(doc.createTextNode(css));
		}
		head.appendChild(style);

		doc.close();
		frame.focus();
		frame.print();
    }-*/;

}
