package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;

public class CombinedWindPanel extends FlowPanel {
    
    private final ImageTransformer transformer;
    private final StringMessages stringMessages;
    
    private final RaceMapImageManager raceMapResources;
    private final Label textLabel;

    private WindTrackInfoDTO windTrackInfoDTO;
    private WindSource windSource;
    
    private Canvas canvas;
    
    private CombinedWindPanelStyle combinedWindPanelStyle;
    private final CoordinateSystem coordinateSystem;
    
    public CombinedWindPanel(RaceMapImageManager theRaceMapResources, CombinedWindPanelStyle combinedWindPanelStyle,
            StringMessages stringMessages, CoordinateSystem coordinateSystem) {
        this.stringMessages = stringMessages;
        this.coordinateSystem = coordinateSystem;
        this.raceMapResources = theRaceMapResources;
        this.combinedWindPanelStyle = combinedWindPanelStyle;
        addStyleName(combinedWindPanelStyle.combinedWindPanel());
        transformer = raceMapResources.getCombinedWindIconTransformer();
        canvas = transformer.getCanvas();
        canvas.addStyleName(this.combinedWindPanelStyle.combinedWindPanelCanvas());
        add(canvas);
        textLabel = new Label("");
        textLabel.addStyleName(this.combinedWindPanelStyle.combinedWindPanelTextLabel());
        add(textLabel);
    }

    protected void redraw() {
        if (windTrackInfoDTO != null) {
            if (!windTrackInfoDTO.windFixes.isEmpty()) {
                WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
                double speedInKnots = windDTO.dampenedTrueWindSpeedInKnots;
                double windFromDeg = windDTO.dampenedTrueWindFromDeg;
                NumberFormat numberFormat = NumberFormat.getFormat("0.0");
                double rotationDegOfWindSymbol = windDTO.dampenedTrueWindBearingDeg;
                transformer.drawTransformedImage(coordinateSystem.mapDegreeBearing(rotationDegOfWindSymbol), 1.0);
                String title = stringMessages.wind() + ": " +  Math.round(windFromDeg) + " " 
                        + stringMessages.degreesShort() + " (" + WindSourceTypeFormatter.format(windSource, stringMessages) + ")"; 
                canvas.setTitle(title);
                textLabel.setText(numberFormat.format(speedInKnots) + " " + stringMessages.knotsUnit());
                if (!isVisible()) {
                    setVisible(true);
                }
            } else {
                setVisible(false);
            }
        }
    }
    
    public void setWindInfo(WindTrackInfoDTO windTrackInfoDTO, WindSource windSource) {
        this.windTrackInfoDTO = windTrackInfoDTO;
        this.windSource = windSource;
    }

    public WindSource getWindSource() {
        return windSource;
    }

}
