package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.center.CenterChangeMapEvent;
import com.google.gwt.maps.client.events.center.CenterChangeMapHandler;
import com.google.gwt.maps.client.mvc.MVCArray;
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
    private final MapWidget map;
    private final boolean newFix;
    private int polylineFixIndex;
    private MVCArray<LatLng> polylinePath;
    private LatLng startPos;
    private FixOverlay overlay;
    private CoordinateSystem coordinateSystem;
    private FixOverlay moveOverlay;
    private HandlerRegistration centerChangeHandlerRegistration;
    private PopupPanel popup;
    
    /**
     * Use this constructor when there is already a fix with an overlay. 
     * This constructor will automatically assume that an existing fix is moved.
     * @param map
     * @param polylineFixIndex Position of the fix in the polyline path
     * @param polylinePath
     * @param overlay
     * @param callback
     */
    public FixPositionChooser(final MapWidget map, final int polylineFixIndex, final MVCArray<LatLng> polylinePath, final FixOverlay overlay, 
            final Callback<Position, Exception> callback) {
        this(false, map, polylineFixIndex, polylinePath, overlay, overlay.getLatLngPosition(), overlay.getCoordinateSystem(), 
                "Confirm Move", callback);
    }
    
    /**
     * Use this constructor when you want to add a fix and not move one.
     * This constructor will automatically assume that there is no existing fix.
     * @param map
     * @param polylineFixIndex Position of the fix in the polyline path
     * @param polylinePath
     * @param startPos
     * @param coordinateSystem
     * @param callback
     */
    public FixPositionChooser(final MapWidget map, final int polylineFixIndex, final MVCArray<LatLng> polylinePath, final LatLng startPos, 
            final CoordinateSystem coordinateSystem, final Callback<Position, Exception> callback) {
        this(true, map, polylineFixIndex, polylinePath, null, startPos, coordinateSystem, 
                "Confirm New", callback);
    }
    
    // TODO: Differentiate touch and mouse somehow
    // TODO: Disable time slider
    private FixPositionChooser(final boolean newFix, final MapWidget map, final int polylineFixIndex, final MVCArray<LatLng> polylinePath, 
            final FixOverlay overlay, final LatLng startPos, final CoordinateSystem coordinateSystem, final String confirmButtonText, 
            final Callback<Position, Exception> callback) {
        this.callback = callback;
        this.map = map;
        this.newFix = newFix;
        this.polylineFixIndex = polylineFixIndex;
        this.polylinePath = polylinePath;
        this.overlay = overlay;
        this.startPos = startPos;
        this.coordinateSystem = coordinateSystem;
        setupUIOverlay(confirmButtonText);
    }
    
    private void setupUIOverlay(final String confirmButtonText) {
        final GPSFixDTO fix;
        if (overlay != null) {
            final GPSFixDTO oldFix = overlay.getGPSFixDTO();
            fix = new GPSFixDTO(oldFix.timepoint, oldFix.position, oldFix.speedWithBearing, oldFix.degreesBoatToTheWind,
                    oldFix.tack, oldFix.legType, oldFix.extrapolated);
            this.moveOverlay = new FixOverlay(map, overlay.getZIndex(), fix, overlay.getType(), "#f00", coordinateSystem);
        } else {
            fix = new GPSFixDTO(null, coordinateSystem.getPosition(startPos), null, new WindDTO(), null, null, false);
            this.moveOverlay = new FixOverlay(map, 0, fix, FixType.BUOY, "#f00", coordinateSystem);
        }
        map.panTo(startPos);
        polylinePath.insertAt(polylineFixIndex, map.getCenter());
        centerChangeHandlerRegistration = map.addCenterChangeHandler(new CenterChangeMapHandler() {
            @Override
            public void onEvent(CenterChangeMapEvent event) {
                fix.position = coordinateSystem.getPosition(map.getCenter());
                moveOverlay.setGPSFixDTO(fix);
                if (polylinePath != null) {
                    polylinePath.setAt(polylineFixIndex, map.getCenter());
                }
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
                if (newFix) {
                    polylinePath.removeAt(polylineFixIndex);
                } else {
                    polylinePath.setAt(polylineFixIndex, overlay.getLatLngPosition());
                }
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
