package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.dto.PairingListDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class PairingListPreviewDialog extends DataEntryDialog<Void> {
    
    private final StringMessages stringMessages;
    private final PairingListDTO pairingListDTO;
    private final List<String> fleetNames;
    
    public PairingListPreviewDialog(PairingListDTO pairingListDTO, List<String> fleetNames, StringMessages stringMessages) {
        super(stringMessages.pairingList() + " " + stringMessages.printView(), "", stringMessages.ok(), stringMessages.cancel(), null, null);
        this.stringMessages = stringMessages;
        this.pairingListDTO = pairingListDTO;
        this.fleetNames = fleetNames;
    }

    @Override
    protected Void getResult() {
        return null;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        ScrollPanel scrollPanel = new ScrollPanel(this.getPairingListGrid());
        scrollPanel.setHeight(((int) Window.getClientHeight() * 0.8) + "px");
        scrollPanel.getElement().getStyle().setPadding(15, Unit.PX);
        return scrollPanel;
    }
    
    private Widget getPairingListGrid() {
        final List<BoatDTO> boats = pairingListDTO.getBoats();

        final int flightCount = pairingListDTO.getPairingList().size();
        final int groupCount = pairingListDTO.getPairingList().get(0).size();
        final int boatCount = boats.size();
        
        Grid pairingListGrid = new Grid(flightCount * groupCount + 1, (boatCount + 2));
        pairingListGrid.getElement().setId("grid");
        pairingListGrid.setCellPadding(15);
        pairingListGrid.getElement().setAttribute("style", "border-collapse: collapse");

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
                        new Label(fleetNames.get(groupIndex-1)));
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
                        pairingListGrid.setWidget(groupIndex, boatIndexInGrid, new Label(stringMessages.empty()));
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

        ScrollPanel result = new ScrollPanel();
        result.add(pairingListPanel);

        return pairingListPanel;
    }

}
