package com.sap.sailing.gwt.ui.client.shared.racemap.windladder;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlays.MapCanvasProjection;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;

public class LaylineClipper {
    protected final List<Position> markPositions = new ArrayList<>(1);
    protected PassingInstruction passingInstruction;

    public void update(WaypointDTO waypointDto) {
        markPositions.clear();
        if (waypointDto != null) {
            for (MarkDTO mark : waypointDto.controlPoint.getMarks()) {
                markPositions.add(mark.position);
            }
            passingInstruction = waypointDto.passingInstructions;
        }
    }

    public void clip(int width, int height, Context2d ctx, MapCanvasProjection projection) {
        if (!markPositions.isEmpty()) {
            final int size = markPositions.size();
            Point[] points = new Point[size];
            for (int i = 0; i < size; i++) {
                final Position pos = markPositions.get(i);
                points[i] = projection.fromLatLngToDivPixel(LatLng.newInstance(pos.getLatDeg(), pos.getLngDeg()));
            }
            
        }
    }
}
