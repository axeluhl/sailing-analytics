package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;

public class CombinedWindPanel extends FlowPanel {
    private static final int LABEL_HEIGHT = 12;
    
    private final ImageTransformer transformer;
    private final StringMessages stringMessages;
    
    private final RaceMapImageManager raceMapResources;
    private final Label textLabel;

    private WindTrackInfoDTO windTrackInfoDTO;
    private WindSource windSource;
    
    private Canvas canvas;

    /**
     */
    public CombinedWindPanel(RaceMapImageManager theRaceMapResources, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.raceMapResources = theRaceMapResources;
        canvas = Canvas.createIfSupported();
        transformer = raceMapResources.getCombinedWindIconTransformer();
        final int canvasSize = 2*transformer.getRadius();
        this.setSize(canvasSize + "px", canvasSize + LABEL_HEIGHT +"px");
        if (canvas != null) {
            canvas.setWidth(String.valueOf(canvasSize));
            canvas.setHeight(String.valueOf(canvasSize));
            canvas.setCoordinateSpaceWidth(canvasSize);
            canvas.setCoordinateSpaceHeight(canvasSize);
        }
        textLabel = new Label("");
        textLabel.setSize(""+canvasSize+"px", ""+LABEL_HEIGHT+"px");
        textLabel.getElement().getStyle().setFontSize(LABEL_HEIGHT, Unit.PX);
        textLabel.setWordWrap(false);
        textLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        add(canvas);
        add(textLabel);
    }

    protected void redraw() {
        if (windTrackInfoDTO != null) {
            if (windTrackInfoDTO.windFixes.size() > 0) {
                WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
                double speedInKnots = windDTO.dampenedTrueWindSpeedInKnots;
                double windFromDeg = windDTO.dampenedTrueWindFromDeg;
                NumberFormat numberFormat = NumberFormat.getFormat("0.0");
                double rotationDegOfWindSymbol = windDTO.dampenedTrueWindBearingDeg;
                ImageData imageData = transformer.getTransformedImageData(rotationDegOfWindSymbol, 1.0);
                canvas.getContext2d().putImageData(imageData, 0, 0);
                String title = stringMessages.wind() + ": " +  Math.round(windFromDeg) + " " 
                        + stringMessages.degreesShort() + " (" + WindSourceTypeFormatter.format(windSource, stringMessages) + ")"; 
                canvas.setTitle(title);
                textLabel.setText(numberFormat.format(speedInKnots) + " " + stringMessages.averageSpeedInKnotsUnit());
                if (!isVisible()) {
                    setVisible(true);
                }
            } else {
                setVisible(false);
            }
        }
    }
    
    public WindTrackInfoDTO getWindTrackInfoDTO() {
        return windTrackInfoDTO;
    }

    public void setWindInfo(WindTrackInfoDTO windTrackInfoDTO, WindSource windSource) {
        this.windTrackInfoDTO = windTrackInfoDTO;
        this.windSource = windSource;
    }

    public WindSource getWindSource() {
        return windSource;
    }

}
