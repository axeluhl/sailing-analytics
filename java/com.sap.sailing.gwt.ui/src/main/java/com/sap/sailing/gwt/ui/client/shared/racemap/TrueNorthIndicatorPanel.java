package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

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
    
    private CombinedWindPanelStyle combinedWindPanelStyle;
    private final CoordinateSystem coordinateSystem;
    
    public TrueNorthIndicatorPanel(final RaceMap map, RaceMapImageManager theRaceMapResources, CombinedWindPanelStyle combinedWindPanelStyle,
            final StringMessages stringMessages, CoordinateSystem coordinateSystem) {
        this.stringMessages = stringMessages;
        this.coordinateSystem = coordinateSystem;
        this.raceMapResources = theRaceMapResources;
        this.combinedWindPanelStyle = combinedWindPanelStyle;
        addStyleName(combinedWindPanelStyle.combinedWindPanel());
        transformer = raceMapResources.getTrueNorthIndicatorIconTransformer();
        canvas = transformer.getCanvas();
        canvas.addStyleName(this.combinedWindPanelStyle.combinedWindPanelCanvas());
        add(canvas);
        canvas.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // a bit clumsy, but there is no copy constructor on RaceMapSettings, and the RaceMapSettingsDialogComponent
                // class has all we need to clone a RaceMapSettings object, without showing it
                final RaceMapSettingsDialogComponent settingsCloner = new RaceMapSettingsDialogComponent(map.getSettings(), stringMessages, /* showViewSimulation */true);
                settingsCloner.getAdditionalWidget(new DataEntryDialog<RaceMapSettings>("dummy", "dummy", "OK", "Cancel", /* validator */null, /* callback */null) {
                    @Override
                    protected RaceMapSettings getResult() {
                        return null;
                    }
                });
                final RaceMapSettings newSettings = settingsCloner.getResult();
                newSettings.setWindUp(!newSettings.isWindUp());
                map.updateSettings(newSettings);
            }
        });
        textLabel = new Label("");
        textLabel.addStyleName(this.combinedWindPanelStyle.combinedWindPanelTextLabel());
        add(textLabel);
    }

    protected void redraw() {
        final double mappedTrueNorthDeg = coordinateSystem.mapDegreeBearing(0);
        transformer.drawTransformedImage(mappedTrueNorthDeg, 1.0);
        String title = stringMessages.rotatedFromTrueNorthClickToToggleWindUp(mappedTrueNorthDeg);
        canvas.setTitle(title);
        textLabel.setText(mappedTrueNorthDeg == 0 ? "N" : title);
        if (!isVisible()) {
            setVisible(true);
        }
    }
}
