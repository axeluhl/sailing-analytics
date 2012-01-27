package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

/**
 * Loads an image from a URL, draws it to a {@link Canvas}, rotates (specified angle) and scales it and allows clients
 * to obtain a URL for the rotated image contents to be used in other contexts requiring an image URL.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ImageTransformer {
    private final String untransformedImageURL;
    private final Canvas canvas;
    private final int imageWidth;
    private final int imageHeight;
    private int canvasRadius;
    private int canvasWidth;
    private int canvasHeight;
    
    private double currentScale;
    
    private ImageElement imageElement;

    private Context2d context;
    
    public ImageTransformer(ImageResource untransformedImage) {
        this.untransformedImageURL = untransformedImage.getSafeUri().asString();
        canvas = Canvas.createIfSupported();
        imageWidth = untransformedImage.getWidth();
        imageHeight = untransformedImage.getHeight();
        scale(1.0);
        final Image image = new Image(untransformedImageURL.toString());
        imageElement = (ImageElement) image.getElement().cast();
        if (imageElement == null) {
            image.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(LoadEvent event) {
                    imageElement = (ImageElement) image.getElement().cast();
                }
            });
        }
    }
    
    private void scale(double scaleFactor) {
        if (scaleFactor != currentScale) {
            canvasWidth = (int) Math.round(imageWidth * scaleFactor);
            canvasHeight = (int) Math.round(imageHeight * scaleFactor);
            canvasRadius = (int) Math.sqrt(canvasWidth * canvasWidth / 4 + canvasHeight * canvasHeight / 4);
            canvas.setSize("" + 2 * canvasRadius + "px", "" + 2 * canvasRadius + "px");
            canvas.setCoordinateSpaceWidth(2 * canvasRadius);
            canvas.setCoordinateSpaceHeight(2 * canvasRadius);
            context = canvas.getContext2d();
            currentScale = scaleFactor;
        }
    }
    
    public Size getImageSize() {
        return Size.newInstance(imageWidth, imageHeight);
    }
    
    public Point getAnchor(double scaleFactor) {
        scale(scaleFactor);
        return Point.newInstance(canvasRadius, canvasRadius);
    }

    private String getUntransformedImageURL() {
        return untransformedImageURL;
    }
    
    /**
     * If the platform supports the {@link Canvas} element, returns a view rotated by <code>degrees</code> degrees. Otherwise,
     * the {@link #getUntransformedImageURL() untransformed image's URL} is returned.
     */
    public String getTransformedImageURL(double angleInDegrees, double scaleFactor) {
        String result = getUntransformedImageURL();
        if (canvas != null) {
            if (imageElement != null) {
                scale(scaleFactor);
                double angleInRadians = angleInDegrees/180.*Math.PI;
                context.clearRect(0, 0, 2*canvasRadius, 2*canvasRadius);
                context.save();
                context.translate(canvasRadius, canvasRadius);
                context.rotate(angleInRadians);
                context.scale(scaleFactor, scaleFactor);
                context.drawImage(imageElement, (-imageWidth/2), (-imageHeight/2));
                result = canvas.toDataUrl("image/png");
                context.restore();
            }
        }
        return result;
    }
}
