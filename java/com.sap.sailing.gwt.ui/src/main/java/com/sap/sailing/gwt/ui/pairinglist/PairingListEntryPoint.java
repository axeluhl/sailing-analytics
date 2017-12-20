package com.sap.sailing.gwt.ui.pairinglist;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
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
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

public class PairingListEntryPoint extends AbstractSailingEntryPoint {

    private PairingListContextDefinition pairingListContextDefinition;
    private StrippedLeaderboardDTO strippedLeaderboardDTO;

    private StringMessages stringmessages = StringMessages.INSTANCE;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        pairingListContextDefinition = new SettingsToUrlSerializer()
                .deserializeFromCurrentLocation(new PairingListContextDefinition());
        this.sailingService.getLeaderboard(pairingListContextDefinition.getLeaderboardName(), new AsyncCallback<StrippedLeaderboardDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                strippedLeaderboardDTO=null;
            }
            @Override
            public void onSuccess(StrippedLeaderboardDTO result) {
                strippedLeaderboardDTO=result; 
                createUI();
            }
        });
    }

    private void createUI() {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        ScrollPanel scrollPanel = new ScrollPanel();
        RootLayoutPanel.get().add(mainPanel);
        mainPanel.setWidth("100%");
        mainPanel.setHeight("100%");
        SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(
                pairingListContextDefinition.getLeaderboardName());
        new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
        mainPanel.addNorth(header, 75);
        Button btn = new Button(getStringMessages().print());
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        contentPanel.add(btn);
        contentPanel.setWidth("100%");
        contentPanel.getElement().getStyle().setProperty("marginTop", "15px");
        contentPanel.getElement().getStyle().setProperty("marginBottom", "15px");
        scrollPanel.add(contentPanel);
        sailingService.getPairingListFromRaceLogs(pairingListContextDefinition.getLeaderboardName(),
                new AsyncCallback<PairingListDTO>() {

                    @Override
                    public void onSuccess(PairingListDTO result) {
                        if (strippedLeaderboardDTO != null) {
                            sailingService.getRaceDisplayNamesFromLeaderboard(strippedLeaderboardDTO.getName(),
                                    result.getRaceColumnNames(), new AsyncCallback<List<String>>() {
                                        @Override
                                        public void onFailure(Throwable caught) {
                                        }

                                        @Override
                                        public void onSuccess(List<String> names) {
                                            VerticalPanel pairingListPanel = createPairingListPanel(result, names);
                                            contentPanel.add(pairingListPanel);
                                            btn.addClickHandler(new ClickHandler() {
                                                @Override
                                                public void onClick(ClickEvent event) {
                                                    // TODO use safe html encoding
                                                    printPairingListGrid("<h2>"
                                                            + pairingListContextDefinition.getLeaderboardName()
                                                            + "</h2>"
                                                            + pairingListPanel.asWidget().getElement().getInnerHTML());
                                                }
                                            });

                                        }
                                    });
                        }
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
        mainPanel.add(scrollPanel);
    }

    private VerticalPanel createPairingListPanel(PairingListDTO pairingListDTO, final List<String> fleetnames) {
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
            if (boat.getColor() != null) {
                pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setBackgroundColor(
                        boat.getColor().getAsHtml());
            } else {
                pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setBackgroundColor(
                        "#cecece");
            }
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
                    new Label(pairingListDTO.getRaceColumnNames().get(flightIndexInGrid - 1)));
            pairingListGrid.getCellFormatter().getElement(currentRaceInGridCells, 0).getStyle().setPadding(5, Unit.PX);
            pairingListGrid.getCellFormatter().getElement(currentRaceInGridCells, 0).getStyle()
                    .setBackgroundColor(color);
            for (List<Pair<CompetitorDTO, BoatDTO>> group : flight) {
                // setting up fleet
                pairingListGrid.getCellFormatter().getElement(groupIndex, 0).getStyle().setPadding(3, Unit.PX);
                pairingListGrid.getCellFormatter().getElement(groupIndex, 0).getStyle().setBackgroundColor(color);
                //TODO add column for race 1-45 (default)
                pairingListGrid.setWidget(groupIndex, 1,
                        new Label(fleetnames.get(groupIndex-1)));
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
                        pairingListGrid.setWidget(groupIndex, boatIndexInGrid, new Label(stringmessages.empty()));
                        pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                                .setColor(Color.RED.toString());
                    } else {
                        // TODO change competitor name to competitor shorthand symbol
                        pairingListGrid.setWidget(groupIndex, boatIndexInGrid,
                                new Label(competitorAndBoatPair.getA().getSailID()));
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

        VerticalPanel pairingListPanel = new VerticalPanel();
        pairingListPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        pairingListPanel.add(pairingListGrid);
        pairingListPanel.setWidth("100%");
        pairingListPanel.ensureDebugId("PairingListPanel");
        pairingListPanel.getElement().getStyle().setProperty("marginTop", "15px");

        ScrollPanel result = new ScrollPanel();
        result.add(pairingListPanel);

        return pairingListPanel;
    }
    
    //TODO search for gwt API (Window, Document)
    private native void printPairingListGrid(String pageHTMLContent) /*-{
		
		var frame = $doc.getElementById('__gwt_historyFrame');
		frame = frame.contentWindow;
		var doc = frame.document;
		doc.open();
		doc.write(pageHTMLContent);

		//adding style to doc
		var css = "body { background: #fff; font-family: 'Open Sans', Arial, Verdana, sans-serif;"
				+ "line-height: 1; font-weight: 400; border: 0}"
				+ "h2 { text-align: center }"
				+ "table { border-collapse: collapse; border: 1px solid black; margin: auto}"
				+ "td { font-size: 15px; }"
		head = doc.head || doc.getElementsByTagName('head')[0], style = doc
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
