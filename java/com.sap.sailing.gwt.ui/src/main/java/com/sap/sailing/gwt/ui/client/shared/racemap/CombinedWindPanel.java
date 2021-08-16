package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sse.gwt.client.shared.settings.DummyOnSettingsStoredCallback;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class CombinedWindPanel extends FlowPanel {
    
    private final ImageTransformer transformer;
    private final StringMessages stringMessages;
    
    private final RaceMapImageManager raceMapResources;
    private final Label textLabel;

    private WindTrackInfoDTO windTrackInfoDTO;
    private WindSource windSource;
    
    private Canvas canvas;
    
    private RaceMapStyle raceMapStyle;
    private final CoordinateSystem coordinateSystem;
    
    public CombinedWindPanel(final RaceMap map, RaceMapImageManager theRaceMapResources, RaceMapStyle raceMapStyle,
            StringMessages stringMessages, CoordinateSystem coordinateSystem, PaywallResolver paywallResolver) {
        this.stringMessages = stringMessages;
        this.coordinateSystem = coordinateSystem;
        this.raceMapResources = theRaceMapResources;
        this.raceMapStyle = raceMapStyle;
        addStyleName(raceMapStyle.raceMapIndicatorPanel());
        addStyleName(raceMapStyle.combinedWindPanel());
        transformer = raceMapResources.getCombinedWindIconTransformer();
        canvas = transformer.getCanvas();
        canvas.addStyleName(this.raceMapStyle.raceMapIndicatorPanelCanvas());
        add(canvas);
        canvas.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final boolean hasPermission = paywallResolver.hasPermission(SecuredDomainType.TrackedRaceActions.VIEWSTREAMLETS);
                final RaceMapSettings oldRaceMapSettings = map.getSettings();
                // when off, turn on; when on and no color, turn on color; when on with color, turn off; Only clickable, if permissions granted
                final boolean newShowStreamletsOverlaySetting = (oldRaceMapSettings.isShowWindStreamletOverlay() ?
                        oldRaceMapSettings.isShowWindStreamletColors() ? false : true : true) && hasPermission;
                final boolean newShowWindStreamletColors = (oldRaceMapSettings.isShowWindStreamletOverlay() ? !oldRaceMapSettings.isShowWindStreamletColors() :
                    false) && hasPermission;
                final RaceMapSettings newRaceMapSettings = new RaceMapSettings(oldRaceMapSettings.getZoomSettings(),
                        oldRaceMapSettings.getHelpLinesSettings(), oldRaceMapSettings.getTransparentHoverlines(), 
                        oldRaceMapSettings.getHoverlineStrokeWeight(), oldRaceMapSettings.getTailLengthInMilliseconds(), oldRaceMapSettings.isWindUp(),
                        oldRaceMapSettings.getBuoyZoneRadius(), oldRaceMapSettings.isShowOnlySelectedCompetitors(),
                        oldRaceMapSettings.isShowSelectedCompetitorsInfo(), newShowWindStreamletColors,
                        newShowStreamletsOverlaySetting, oldRaceMapSettings.isShowSimulationOverlay(),
                        oldRaceMapSettings.isShowMapControls(), oldRaceMapSettings.getManeuverTypesToShow(),
                        oldRaceMapSettings.isShowDouglasPeuckerPoints(), oldRaceMapSettings.isShowEstimatedDuration(),
                        oldRaceMapSettings.getStartCountDownFontSizeScaling(), oldRaceMapSettings.isShowManeuverLossVisualization(),
                        oldRaceMapSettings.isShowSatelliteLayer());
                if (map.getComponentContext() != null
                        && map.getComponentContext().isStorageSupported(map)) {
                    map.getComponentContext().storeSettingsForContext(map, newRaceMapSettings,
                            new DummyOnSettingsStoredCallback());
                }
                map.updateSettings(newRaceMapSettings);
                if(!hasPermission) {
                    Notification.notify("Like & Subscribe!", NotificationType.WARNING);
                }
            }
        });
        textLabel = new Label("");
        textLabel.addStyleName(this.raceMapStyle.raceMapIndicatorPanelTextLabel());
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
                        + stringMessages.degreesShort() + " (" + WindSourceTypeFormatter.format(windSource, stringMessages) + ")" +
                        + '\n'+ stringMessages.clickToToggleWindStreamlets();
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
}
