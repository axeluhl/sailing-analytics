package com.sap.sailing.dashboards.gwt.client.windchart.compass;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.dashboards.gwt.client.RibDashboardImageResources;
import com.sap.sailing.domain.common.dto.PositionDTO;

/**
 * The class is an UI component that should be used to display the direction and the distance from the users device to a
 * certain pointed GPS location. The class needs an {@link LocationPointerCompassAngleDistance} instance and registers
 * as an listener to it. It furthermore notifies its {@link LocationPointerCompassAngleDistance} instance about wind bot
 * GPS location changes.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class LocationPointerCompass extends AbsolutePanel implements HasWidgets,
        LocationPointerCompassAngleDistanceListener {

    private double angleOffset;
    
    private Image compassNeedle;

    /**
     * The Label shows the distance the users device is away from the location it points to.
     * */
    private Label distanceToPointetLocationLabel;

    /**
     * The Label shows the direction in degrees from north the users device is away from the location it points to.
     * */
    private Label angleToPointetLocationLabel;

    /**
     * The {@link LocationPointerCompassAngleDistance} notifies the class about updates about angle and distance
     * changes.
     * */
    private LocationPointerCompassAngleDistance locationPointerCompassAngleDistance;

    public LocationPointerCompass() {
        LocationPointerCompassRessources.INSTANCE.css().ensureInjected();
        this.getElement().addClassName(LocationPointerCompassRessources.INSTANCE.css().locationPointerCompass());
        initCompassNeedle();
        initAngleToPointetLocationLabel();
        initDistanceToPointedLocationLabel();
        locationPointerCompassAngleDistance = new LocationPointerCompassAngleDistance();
        locationPointerCompassAngleDistance.addListener(this);
        locationPointerCompassAngleDistance.triggerOrientationRead();
    }

    private void initCompassNeedle() {
        compassNeedle = new Image();
        compassNeedle.setResource(RibDashboardImageResources.INSTANCE.compass());
        compassNeedle.getElement().addClassName(LocationPointerCompassRessources.INSTANCE.css().locationPointerCompass_needle());
        LocationPointerCompass.this.add(compassNeedle);
    }

    private void initAngleToPointetLocationLabel() {
        angleToPointetLocationLabel = new Label();
        angleToPointetLocationLabel.getElement().addClassName(LocationPointerCompassRessources.INSTANCE.css().locationPointerCompass_angleToLocationLabel());
        this.add(angleToPointetLocationLabel);
    }

    private void initDistanceToPointedLocationLabel() {
        distanceToPointetLocationLabel = new Label();
        distanceToPointetLocationLabel.getElement().addClassName(LocationPointerCompassRessources.INSTANCE.css().locationPointerCompass_distanceToLocationLabel());
        distanceToPointetLocationLabel.setText("m");
        this.add(distanceToPointetLocationLabel);
    }

    private void updateNeedleAngle(double newDirection) {
        // Adapt degrees to image angle
        synchronized (this) {
            String winddirectionformatted = NumberFormat.getFormat("#0").format(newDirection-angleOffset);
            compassNeedle.getElement().getStyle().setProperty("transform", "rotate(" + winddirectionformatted + "deg)");
            compassNeedle.getElement().getStyle()
                    .setProperty("webkitTransform", "rotate(" + winddirectionformatted + "deg)");
            angleToPointetLocationLabel.setText(winddirectionformatted + "°");
        }
    }

    private void updateDistanceToPointedLocationLabel(double distance, double angle) {
        String distanceFormatted = NumberFormat.getFormat("#0").format(distance);
        distanceToPointetLocationLabel.setText(distanceFormatted + " m");
    }

    public void windBotPositionChanged(PositionDTO positionDTO) {
        locationPointerCompassAngleDistance.windBotPositionChanged(positionDTO);
    }

    @Override
    public void angleChanged(double angle) {
        updateNeedleAngle(angle);
    }

    @Override
    public void angleAndDistanceChanged(double angle, double distance) {
        updateNeedleAngle(angle);
        updateDistanceToPointedLocationLabel(distance, angle);
    }

    @Override
    public void setAngleOffset(double offset) {
        angleOffset = offset;
    }
}
