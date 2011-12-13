package com.sap.sailing.gwt.ui.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

/**
 * Loads an image from a URL, draws it to a {@link Canvas}, rotates it by a specified angle and allows clients
 * to obtain a URL for the rotated image contents to be used in other contexts requiring an image URL.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ImageRotator {
    private final String unrotatedImageURL;
    private final Canvas canvas;
    private final int imageWidth;
    private final int imageHeight;
    private final int canvasRadius;
    
    private ImageElement imageElement;

    private final Context2d context;
    
    public ImageRotator(ImageResource unrotatedImage) {
        this.unrotatedImageURL = unrotatedImage.getSafeUri().asString();
        canvas = Canvas.createIfSupported();
        imageWidth = unrotatedImage.getWidth();
        imageHeight = unrotatedImage.getHeight();
        canvasRadius = (int) Math.sqrt(imageWidth*imageWidth/4 + imageHeight*imageHeight/4);
        canvas.setSize(""+2*canvasRadius, ""+2*canvasRadius);
        context = canvas.getContext2d();
        final Image image = new Image(unrotatedImageURL.toString());
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
    
    public Point getAnchor() {
        return Point.newInstance(canvasRadius, canvasRadius);
    }

    private String getUnrotatedImageURL() {
        return unrotatedImageURL;
    }
    
    /**
     * If the platform supports the {@link Canvas} element, returns a view rotated by <code>degrees</code> degrees. Otherwise,
     * the {@link #getUnrotatedImageURL() unrotated image's URL} is returned.
     */
    public String getRotatedImageURL(double angleInDegrees) {
        String result = getUnrotatedImageURL();
        if (canvas != null) {
            if (imageElement != null) {
                double angleInRadians = angleInDegrees/180.*Math.PI;
                double sin = Math.sin(angleInRadians);
                double cos = Math.cos(angleInRadians);
                context.clearRect(0, 0, imageWidth, imageHeight);
                context.save();
                context.rotate(angleInRadians);
                context.translate(sin*imageWidth/2 + cos*imageHeight/2, cos*imageWidth/2 - sin*imageHeight/2);
                context.drawImage(imageElement, -imageWidth/2, -imageHeight/2);
                result = canvas.toDataUrl("image/png");
                context.restore();
            }
        }
        return result;
    }
}
