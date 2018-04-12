package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.PairingListDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class PairingListPreviewDialog extends DataEntryDialog<Void> {
    
    private final StringMessages stringMessages;
    private final PairingListDTO pairingListDTO;
    private final List<String> raceDisplayNames;
    private final Button print;
    private final String leaderboardName;
    
    public PairingListPreviewDialog(PairingListDTO pairingListDTO, List<String> raceDisplayNames, StringMessages stringMessages,String leaderboardName) {
        super(stringMessages.pairingList() + " " + stringMessages.printView(), "", stringMessages.ok(), stringMessages.cancel(), null, null);
        this.stringMessages = stringMessages;
        this.pairingListDTO = pairingListDTO;
        this.raceDisplayNames = raceDisplayNames;
        this.leaderboardName = leaderboardName;
        this.print = new Button(stringMessages.print());
        this.print.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Widget pairingListPanel = getPairingListGrid();
                printPairingListGrid(
                        "<div class='printHeader'><img src='images/home/logo-small@2x.png' />"
                                + "<b class='title'>"
                                + SafeHtmlUtils.fromString(leaderboardName)
                                .asString()
                                + "</b></div>" + pairingListPanel.asWidget()
                                        .getElement().getInnerHTML());
            }
        });
        this.getRightButtonPannel().add(print);
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
    
    public Widget getPairingListGrid() {
        final List<BoatDTO> boats = new ArrayList<>(pairingListDTO.getBoats());
        Collections.sort(boats, getBoatsComparator());
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
            pairingListGrid.setWidget(0, boatIndex + 2, new Label(getBoatDisplayName(boat)));
            pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setTextAlign(TextAlign.CENTER);
            pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setPadding(10, Unit.PX);
            if (boat.getColor() != null) {
                pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setBackgroundColor(
                        boat.getColor().getAsHtml());
                if (isDark(boat.getColor())) {
                    pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setColor(Color.WHITE.getAsHtml());
                }
            } else {
                pairingListGrid.getCellFormatter().getElement(0, boatIndex + 2).getStyle().setBackgroundColor(
                        "#cecece");
            }
            boatIndex++;
        }
        final String BACKGROUND_SHADE = "#cecece";
        String color = BACKGROUND_SHADE;
        pairingListGrid.getCellFormatter().getElement(0, 0).getStyle().setBackgroundColor(BACKGROUND_SHADE);
        pairingListGrid.getCellFormatter().getElement(0, 1).getStyle().setBackgroundColor(BACKGROUND_SHADE);
        for (List<List<Pair<CompetitorDTO, BoatDTO>>> flight : pairingListDTO.getPairingList()) {
            color = (color.equals("none") ? BACKGROUND_SHADE : "none");
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
                pairingListGrid.setWidget(groupIndex, 1,
                        new Label(raceDisplayNames.get(groupIndex-1)));
                // setting up fleets style
                pairingListGrid.getCellFormatter().getElement(groupIndex, 1).getStyle().setPadding(3, Unit.PX);
                pairingListGrid.getCellFormatter().getElement(groupIndex, 1).getStyle().setBackgroundColor(color);
                if (group.size() < boatCount) {
                    List<BoatDTO> unusedBoats = new ArrayList<>(boats);
                    for (Pair<CompetitorDTO, BoatDTO> competitorAndBoatPair : group) {
                        unusedBoats.remove(competitorAndBoatPair.getB());
                    }
                    for (BoatDTO unusedBoat : unusedBoats) {
                        group.add(new Pair<>(null, unusedBoat));
                    }
                }
                for (Pair<CompetitorDTO, BoatDTO> competitorAndBoatPair : group) {
                    int boatIndexInGrid = boats.indexOf(competitorAndBoatPair.getB()) + 2;
                    if (competitorAndBoatPair.getA() == null) {
                        pairingListGrid.setWidget(groupIndex, boatIndexInGrid, new Label(stringMessages.empty()));
                        pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                                .setColor(Color.RED.toString());
                    } else {
                        final String shortName = competitorAndBoatPair.getA().getShortName();
                        pairingListGrid.setWidget(groupIndex, boatIndexInGrid,
                                new Label(shortName == null ? competitorAndBoatPair.getA().getName() : shortName));
                    }
                    pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                            .setFontWeight(Style.FontWeight.BOLD);
                    pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle()
                            .setTextAlign(TextAlign.CENTER);
                    pairingListGrid.getCellFormatter().getElement(groupIndex, boatIndexInGrid).getStyle().setPadding(5,
                            Unit.PX);
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

    private String getBoatDisplayName(BoatDTO boat) {
        return boat.getName()==null?boat.getSailId():boat.getName();
    }

    /**
     * Compare boats such that a natural ordering in the pairing list display is achieved. This
     * is based on the natural comparator principle, using the string that will be displayed for the boats.
     */
    private Comparator<BoatDTO> getBoatsComparator() {
        final Comparator<String> naturalComparator = new NaturalComparator();
        return (b1, b2)->naturalComparator.compare(getBoatDisplayName(b1), getBoatDisplayName(b2));
    }

    private boolean isDark(Color color) {
        final Triple<Integer, Integer, Integer> rgb = color.getAsRGB();
        return rgb.getA()+rgb.getB()+rgb.getC() < 100;
    }

    private native void printPairingListGrid(String pageHTMLContent) /*-{
		var frameID = '__gwt_historyFrame';
		var frame = $doc.getElementById(frameID);
		if (!frame) {
			$wnd.alert("Error: Can not find frame '" + frameID + "'");
			return;
		}
		frame = frame.contentWindow;
		var document = frame.document;
		document.open();
		document.write(pageHTMLContent);

		//adding style to doc
		var css = "body { background: #fff; font-family: 'Open Sans', Arial, Verdana, sans-serif;"
				+ "line-height: 1; font-weight: 400; border: 0 }"
				+ ".title { font-size: 18px; text-align: center; float: right; color: #f6f9fc; margin-bottom: 0.466666666666667em; margin-right: 0.466666666666667em }"
				+ "img { max-height: 2em; float:left; margin-top: 0.466666666666667em; margin-left: 0.466666666666667em }"
				+ ".printHeader { font-size: 1rem; background: #333; border-bottom: 0.333333333333333em solid #f0ab00;"
				+ "height: 3.333333333333333em; line-height: 3em; width: 100%; overflow: hidden;}"
				+ "table { border-collapse: collapse; border: 1px solid black; margin: auto; width: 100%}"
				+ "td { font-size: 13px; }"
		head = document.head || document.getElementsByTagName('head')[0];
		style = document.createElement('style');
		style.type = 'text/css';
		if (style.styleSheet) {
			style.styleSheet.cssText = css;
		} else {
			style.appendChild(document.createTextNode(css));
		}
		head.appendChild(style);

		document.close();

		//Timeout for assets loading
		setTimeout(function() {
			frame.focus();
			frame.print();
		}, 100);
    }-*/;

}
