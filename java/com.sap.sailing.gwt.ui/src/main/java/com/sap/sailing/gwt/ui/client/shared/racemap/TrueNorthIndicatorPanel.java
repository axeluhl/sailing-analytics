package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * A true north indicator that can be added as a control to the map. Clicking / tapping the control toggles
 * the {@link RaceMapSettings#isWindUp() wind-up} setting for the map.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TrueNorthIndicatorPanel extends FlowPanel {
    
    private final ImageTransformer transformer;
    private final StringMessages stringMessages;
    
    private final RaceMapImageManager raceMapResources;
    private final Label textLabel;

    private Canvas canvas;
    
    private RaceMapStyle raceMapStyle;
    private final CoordinateSystem coordinateSystem;
    
    public TrueNorthIndicatorPanel(final RaceMap map, RaceMapImageManager theRaceMapResources, RaceMapStyle raceMapStyle,
            final StringMessages stringMessages, CoordinateSystem coordinateSystem) {
        this.stringMessages = stringMessages;
        this.coordinateSystem = coordinateSystem;
        this.raceMapResources = theRaceMapResources;
        this.raceMapStyle = raceMapStyle;
        addStyleName(raceMapStyle.raceMapIndicatorPanel());
        addStyleName(raceMapStyle.trueNorthIndicatorPanel());
        transformer = raceMapResources.getTrueNorthIndicatorIconTransformer();
        canvas = transformer.getCanvas();
        canvas.addStyleName(this.raceMapStyle.raceMapIndicatorPanelCanvas());
        add(canvas);
        canvas.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RaceMapSettings oldRaceMapSettings = map.getSettings();
                boolean newWindUpSettings = !oldRaceMapSettings.isWindUp();
                
                final RaceMapSettings newRaceMapSettings = new RaceMapSettings(oldRaceMapSettings.getZoomSettings(),
                        oldRaceMapSettings.getHelpLinesSettings(), oldRaceMapSettings.getTransparentHoverlines(), 
                        oldRaceMapSettings.getHoverlineStrokeWeight(), oldRaceMapSettings.getTailLengthInMilliseconds(), newWindUpSettings,
                        oldRaceMapSettings.getBuoyZoneRadius(), oldRaceMapSettings.isShowOnlySelectedCompetitors(),
                        oldRaceMapSettings.isShowSelectedCompetitorsInfo(), oldRaceMapSettings.isShowWindStreamletColors(),
                        oldRaceMapSettings.isShowWindStreamletOverlay(), oldRaceMapSettings.isShowSimulationOverlay(),
                        oldRaceMapSettings.isShowMapControls(), oldRaceMapSettings.getManeuverTypesToShow(),
                        oldRaceMapSettings.isShowDouglasPeuckerPoints(), oldRaceMapSettings.isShowEstimatedDuration(),
                        oldRaceMapSettings.getStartCountDownFontSizeScaling(), oldRaceMapSettings.isShowManeuverLossVisualization());
                map.updateSettings(newRaceMapSettings);
            }
        });
        textLabel = new Label("");
        textLabel.addStyleName(this.raceMapStyle.raceMapIndicatorPanelTextLabel());
        add(textLabel);
    }

    protected void redraw() {
        final double mappedTrueNorthDeg = coordinateSystem.mapDegreeBearing(0);
        transformer.drawTransformedImage(mappedTrueNorthDeg, 1.0);
        String title = stringMessages.rotatedFromTrueNorth(Math.round(mappedTrueNorthDeg)) + '\n' +
                stringMessages.clickToToggleWindUp();
        canvas.setTitle(title);
        NumberFormat numberFormat = NumberFormat.getFormat("0.0");
        textLabel.setText(mappedTrueNorthDeg == 0 ? "N" : numberFormat.format(mappedTrueNorthDeg));
        if (!isVisible()) {
            setVisible(true);
        }
    }
}
