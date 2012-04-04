package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;

public class CombinedWindOverlay extends CanvasOverlay {

    private final RaceMapResources raceMapResources;

    private WindTrackInfoDTO windTrackInfoDTO;

    private WindSource windSource;

    private final Label textLabel;
    
    private final StringMessages stringMessages;
    
    private final ImageTransformer transformer;

    private int canvasWidth;
    private int canvasHeight;

    public CombinedWindOverlay(RaceMapResources raceMapResources, StringMessages stringMessages) {
        super();
        this.raceMapResources = raceMapResources;
        this.stringMessages = stringMessages;
        canvasWidth = 44;
        canvasHeight = 44;

        if(canvas != null) {
            canvas.setWidth(String.valueOf(canvasWidth));
            canvas.setHeight(String.valueOf(canvasHeight));
            canvas.setCoordinateSpaceWidth(canvasWidth);
            canvas.setCoordinateSpaceHeight(canvasHeight);
        }
        transformer = raceMapResources.getCombinedWindIconTransformer();

        textLabel = new Label();
        textLabel.setSize("44px", "12px");
        textLabel.getElement().getStyle().setFontSize(12, Unit.PX);
        textLabel.setWordWrap(false);
        textLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    }
    
    @Override
    protected Overlay copy() {
      return new CombinedWindOverlay(raceMapResources, stringMessages);
    }

    @Override
   protected void initialize(MapWidget map) {
        super.initialize(map);
        pane.add(textLabel);
        pane.setWidgetPosition(canvas, 10, 10);
        pane.setWidgetPosition(textLabel, 10, 10 + canvasHeight);
   }

   @Override
   protected void remove() {
       super.remove();
       textLabel.removeFromParent();
   }

    @Override
    protected void redraw(boolean force) {
        if(windTrackInfoDTO != null) {
            if(windTrackInfoDTO.windFixes.size() > 0) {
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
                
                if(!isVisible())
                    setVisible(true);
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
