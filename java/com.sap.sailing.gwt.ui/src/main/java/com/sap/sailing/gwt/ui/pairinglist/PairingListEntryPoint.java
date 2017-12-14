package com.sap.sailing.gwt.ui.pairinglist;

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
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        ScrollPanel scrollPanel = new ScrollPanel();
        RootLayoutPanel.get().add(mainPanel);
        
        mainPanel.setWidth("100%");
        mainPanel.setHeight("100%");

        pairingListContextDefinition = new SettingsToUrlSerializer()
                .deserializeFromCurrentLocation(new PairingListContextDefinition());

        SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(
                pairingListContextDefinition.getLeaderboardName());
        new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
        mainPanel.addNorth(header, 75);

        Button btn = new Button("Print");

        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        contentPanel.add(btn);
        contentPanel.setWidth("100%");
        contentPanel.getElement().getStyle().setProperty("marginTop", "15px");
        contentPanel.getElement().getStyle().setProperty("marginBottom", "15px");
        scrollPanel.add(contentPanel);

        sailingService.getPairingListFromTemplate(pairingListContextDefinition.getLeaderboardName(),
                pairingListContextDefinition.getFlightMultiplier(), new AsyncCallback<PairingListDTO>() {

                    @Override
                    public void onSuccess(PairingListDTO result) {
                        VerticalPanel pairingListPanel = createPairingListPanel(result);
                        contentPanel.add(pairingListPanel);
                        btn.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                printPairingListGrid("<h2>" + pairingListContextDefinition.getLeaderboardName()
                                        + "</h2>" + pairingListPanel.asWidget().getElement().getInnerHTML());
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
        mainPanel.add(scrollPanel);
    }

    private VerticalPanel createPairingListPanel(PairingListDTO pairingListDTO) {
        final int flightCount = pairingListDTO.getPairingList().size();
        final int groupCount = pairingListDTO.getPairingList().get(0).size();
        final int boatCount = pairingListDTO.getPairingList().get(0).get(0).size();

        Grid pairingListGrid = new Grid(flightCount * groupCount + 1, (boatCount + 2));
        pairingListGrid.getElement().setId("grid");
        pairingListGrid.setCellPadding(15);
        
        int flightCounter = 1;
        int groupCounter = 1;
        int boatCounter = 1;

        for (int i = 1; i <= boatCount; i++) {
            pairingListGrid.setWidget(0, i + 1, new Label(getStringMessages().boat() + " " + String.valueOf(i)));
            pairingListGrid.getCellFormatter().getElement(0, i + 1).getStyle().setTextAlign(TextAlign.CENTER);
            pairingListGrid.getCellFormatter().getElement(0, i + 1).getStyle().setPadding(5, Unit.PX);
            pairingListGrid.getCellFormatter().getElement(0, i + 1).getStyle().setBackgroundColor(
                    pairingListDTO.getPairingList().get(0).get(0).get(i - 1).getB().getColor().getAsHtml());
        }

        String color = "";

        pairingListGrid.getCellFormatter().getElement(0, 0).getStyle().setBackgroundColor("#cecece");
        pairingListGrid.getCellFormatter().getElement(0, 1).getStyle().setBackgroundColor("#cecece");

        for (List<List<Pair<CompetitorDTO, BoatDTO>>> flight : pairingListDTO.getPairingList()) {

            color = (color.equals("none") ? "#cecece" : "none");

            // setting up race
            int currentRaceInGridCells = (((flightCounter - 1) * groupCount) + 1);
            pairingListGrid.setWidget(currentRaceInGridCells, 0,
                    new Label(getStringMessages().race() + " " + String.valueOf(flightCounter)));

            pairingListGrid.getCellFormatter().getElement(currentRaceInGridCells, 0).getStyle().setPadding(5, Unit.PX);
            pairingListGrid.getCellFormatter().getElement(currentRaceInGridCells, 0).getStyle()
                    .setBackgroundColor(color);

            for (List<Pair<CompetitorDTO, BoatDTO>> group : flight) {
                // setting up fleet

                pairingListGrid.getCellFormatter().getElement(groupCounter, 0).getStyle().setPadding(3, Unit.PX);
                pairingListGrid.getCellFormatter().getElement(groupCounter, 0).getStyle().setBackgroundColor(color);

                pairingListGrid.setWidget(groupCounter, 1,
                        new Label(getStringMessages().fleet() + " " + String.valueOf(groupCounter)));

                // setting up fleets style
                pairingListGrid.getCellFormatter().getElement(groupCounter, 1).getStyle().setPadding(3, Unit.PX);
                pairingListGrid.getCellFormatter().getElement(groupCounter, 1).getStyle().setBackgroundColor(color);

                boatCounter = 1;

                for (Pair<CompetitorDTO, BoatDTO> competitorAndBoatPair : group) {
                    if (competitorAndBoatPair.getA().getName() == null) {
                        pairingListGrid.setWidget(groupCounter, boatCounter + 1, new Label(stringmessages.empty()));
                        pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter + 1).getStyle()
                                .setColor(Color.RED.toString());
                    } else {
                        // TODO change competitor sail id to competitor shorthand symbol
                        pairingListGrid.setWidget(groupCounter, boatCounter + 1,
                                new Label(competitorAndBoatPair.getA().getSailID()));
                    }
                    pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter + 1).getStyle()
                            .setFontWeight(Style.FontWeight.BOLD);
                    pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter + 1).getStyle()
                            .setTextAlign(TextAlign.CENTER);
                    pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter + 1).getStyle()
                            .setPadding(5, Unit.PX);
                    pairingListGrid.getCellFormatter().getElement(groupCounter, boatCounter + 1).getStyle()
                            .setBackgroundColor(color);

                    boatCounter++;
                }
                groupCounter++;
            }
            flightCounter++;
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
	    head = doc.head || doc.getElementsByTagName('head')[0], 
	    style = doc.createElement('style');
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
