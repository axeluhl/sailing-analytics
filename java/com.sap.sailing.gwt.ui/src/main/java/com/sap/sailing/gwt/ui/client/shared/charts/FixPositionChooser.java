package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.center.CenterChangeMapEvent;
import com.google.gwt.maps.client.events.center.CenterChangeMapHandler;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.domain.common.FixType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.client.shared.racemap.FixOverlay;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;

public class FixPositionChooser {
    private final Callback<Position, Exception> callback;
    private LatLng startPos;
    private final MapWidget map;
    private FixOverlay overlay;
    private CoordinateSystem coordinateSystem;
    private FixOverlay moveOverlay;
    private HandlerRegistration centerChangeHandlerRegistration;
    private PopupPanel popup;
    
    public FixPositionChooser(final MapWidget map, final FixOverlay overlay, final String confirmButtonText, final Callback<Position, Exception> callback) {
        this.callback = callback;
        this.map = map;
        this.overlay = overlay;
        this.startPos = overlay.getLatLngPosition();
        this.coordinateSystem = overlay.getCoordinateSystem();
        setupUIOverlay(confirmButtonText);
    }
    
    public FixPositionChooser(final MapWidget map, final LatLng startPos, final CoordinateSystem coordinateSystem, final String confirmButtonText, final Callback<Position, Exception> callback) {
        this.callback = callback;
        this.startPos = startPos;
        this.coordinateSystem = coordinateSystem;
        this.map = map;
        setupUIOverlay(confirmButtonText);
    }
    
    private void setupUIOverlay(final String confirmButtonText) {
        final GPSFixDTO fix;
        if (overlay != null) {
            final GPSFixDTO oldFix = overlay.getGPSFixDTO();
            fix = new GPSFixDTO(oldFix.timepoint, oldFix.position, oldFix.speedWithBearing, oldFix.degreesBoatToTheWind,
                    oldFix.tack, oldFix.legType, oldFix.extrapolated);
            this.moveOverlay = new FixOverlay(map, overlay.getZIndex(), fix, overlay.getType(), overlay.getColor(), coordinateSystem);
        } else {
            fix = new GPSFixDTO(null, coordinateSystem.getPosition(startPos), null, new WindDTO(), null, null, false);
            this.moveOverlay = new FixOverlay(map, 0, fix, FixType.BUOY, "#fff", coordinateSystem);
        }
        map.panTo(startPos);
        centerChangeHandlerRegistration = map.addCenterChangeHandler(new CenterChangeMapHandler() {
            @Override
            public void onEvent(CenterChangeMapEvent event) {
                fix.position = coordinateSystem.getPosition(map.getCenter());
                moveOverlay.setGPSFixDTO(fix);
            }
        });
        popup = new PopupPanel(false);
        popup.setStyleName("EditMarkPositionPopup");
        MenuBar menu = new MenuBar(false);
        MenuItem confirm = new MenuItem(confirmButtonText, new ScheduledCommand() {
            @Override
            public void execute() {
                destroyUIOverlay();
                callback.onSuccess(coordinateSystem.getPosition(map.getCenter()));
            }
        });
        MenuItem cancel = new MenuItem("Cancel", new ScheduledCommand() {
            @Override
            public void execute() {
                destroyUIOverlay();
                callback.onFailure(null);
            }
        });
        menu.addItem(confirm);
        menu.addItem(cancel);
        popup.setWidget(menu);
        popup.show();
        popup.setPopupPosition(map.getAbsoluteLeft() + map.getOffsetWidth() - popup.getOffsetWidth() - 20, 
                map.getAbsoluteTop() + map.getOffsetHeight() - popup.getOffsetHeight() - 20);
    }
    
    private void destroyUIOverlay() {
        moveOverlay.removeFromMap();
        centerChangeHandlerRegistration.removeHandler();
        popup.hide();
    }
}
