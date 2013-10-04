package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.base.Size;
import com.google.gwt.maps.client.overlays.MapCanvasProjection;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewMethods;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;

/**
 * A google map overlay based on a HTML5 canvas for drawing course marks (images) and the buoy zone if the mark is a buoy.
 */
public class CourseMarkOverlay extends CanvasOverlayV3 {

    /**
     * The course mark to draw
     */
    private MarkDTO mark;

    private PositionDTO position;

    private double buoyZoneRadiusInMeter;
    
    private boolean showBuoyZone;

    private final MarkImageDescriptor markImageDescriptor;
    
    private final ImageCanvas markImageCanvas;
    
    private final int MIN_BUOYZONE_RADIUS_IN_PX = 25;
    
    public CourseMarkOverlay(MapWidget map, int zIndex, final RaceMapImageManager raceMapImageManager, MarkDTO markDTO) {
        super(map, zIndex);
        this.mark = markDTO;
        this.position = markDTO.position;
        this.buoyZoneRadiusInMeter = 0.0;
        this.showBuoyZone = false;
    
        markImageDescriptor = raceMapImageManager.resolveMarkImage(markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
        markImageCanvas = new ImageCanvas(markImageDescriptor.getImgageResource());
    }


    @Override
    protected void draw(OverlayViewMethods methods) {
        MapCanvasProjection projection = methods.getProjection();
        if (mark != null && position != null && markImageCanvas.isImageLoaded()) {
            getCanvas().setTitle(getTitle());
            
            LatLng latLngPosition = LatLng.newInstance(position.latDeg, position.lngDeg);

            // calculate canvas size
            Size imageSize = markImageCanvas.getImageSize();
            double canvasWidth = imageSize.getWidth();
            double canvasHeight = imageSize.getHeight();
            double buoyZoneRadiusInPixel = -1;
            if(showBuoyZone && mark.type == MarkType.BUOY) {
                buoyZoneRadiusInPixel = calculateRadiusOfBoundingBox(projection, latLngPosition, buoyZoneRadiusInMeter);
                if(buoyZoneRadiusInPixel > MIN_BUOYZONE_RADIUS_IN_PX) {
                    canvasWidth = buoyZoneRadiusInPixel * 2;
                    canvasHeight = buoyZoneRadiusInPixel * 2;
                }
            }
            setCanvasSize((int) canvasWidth, (int) canvasHeight);
            
            Context2d context2d = getCanvas().getContext2d();
            context2d.clearRect(0, 0, canvasWidth, canvasHeight);

            // draw the course mark
            markImageCanvas.drawImage();
            ImageData imageData = markImageCanvas.getImageData();

            Point buoyPositionInPx = projection.fromLatLngToDivPixel(latLngPosition);
            Point markAnchorPoint = markImageDescriptor.getAnchorPoint();

            // draw the buoy zone 
            if(showBuoyZone && mark.type == MarkType.BUOY && buoyZoneRadiusInPixel > MIN_BUOYZONE_RADIUS_IN_PX) {
                context2d.putImageData(imageData, buoyZoneRadiusInPixel  - markAnchorPoint.getX(),
                        buoyZoneRadiusInPixel - markAnchorPoint.getY());

                CssColor grayTransparentColor = CssColor.make("rgba(50,90,135,0.75)");

                // this translation is important for drawing lines with a real line width of 1 pixel
                context2d.setStrokeStyle(grayTransparentColor);
                context2d.setLineWidth(1.0);
                context2d.beginPath();
                context2d.arc(buoyZoneRadiusInPixel, buoyZoneRadiusInPixel, buoyZoneRadiusInPixel, 0, Math.PI*2, true); 
                context2d.closePath();
                context2d.stroke();
                
                setCanvasPosition(buoyPositionInPx.getX() - buoyZoneRadiusInPixel, buoyPositionInPx.getY() - buoyZoneRadiusInPixel);
            } else {
                context2d.putImageData(imageData, 0, 0);

                setCanvasPosition(buoyPositionInPx.getX() - markAnchorPoint.getX(), buoyPositionInPx.getY() - markAnchorPoint.getY());
            }
        }
    }

    private String getTitle() {
        return mark.getName();
    }
    
    public boolean isShowBuoyZone() {
        return showBuoyZone;
    }

    public void setShowBuoyZone(boolean showBuoyZone) {
        this.showBuoyZone = showBuoyZone;
    }

    public MarkDTO getMark() {
        return mark;
    }

    public void setMarkPosition(PositionDTO position) {
        this.position = position;
    }

    public LatLng getMarkPosition() {
        return LatLng.newInstance(position.latDeg, position.lngDeg);
    }

    public double getBuoyZoneRadiusInMeter() {
        return buoyZoneRadiusInMeter;
    }

    public void setBuoyZoneRadiusInMeter(double buoyZoneRadiusInMeter) {
        this.buoyZoneRadiusInMeter = buoyZoneRadiusInMeter;
    }
}
