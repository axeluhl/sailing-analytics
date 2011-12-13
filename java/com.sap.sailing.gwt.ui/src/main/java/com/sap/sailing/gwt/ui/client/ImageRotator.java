package com.sap.sailing.gwt.ui.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
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
    
    private ImageElement imageElement;

    private final Context2d context;
    
    public ImageRotator(String unrotatedImageURL) {
        this.unrotatedImageURL = unrotatedImageURL;
        canvas = Canvas.createIfSupported();
        context = canvas.getContext2d();
        final Image image = new Image(unrotatedImageURL.toString());
        imageElement = (ImageElement) image.getElement().cast();
        if (imageElement == null) {
            image.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(LoadEvent event) {
                    imageElement = (ImageElement) image.getElement().cast();
                    context.translate(imageElement.getWidth()/2, imageElement.getHeight()/2);
                }
            });
        } else {
            context.translate(imageElement.getWidth()/2, imageElement.getHeight()/2);
        }
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
                context.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
                double angleInRadians = angleInDegrees/180.*Math.PI;
                context.rotate(angleInRadians);
                context.drawImage(imageElement, -imageElement.getWidth()/2, -imageElement.getHeight()/2);
                result = canvas.toDataUrl("image/png");
                context.rotate(-angleInRadians);
            }
        }
        return result;
    }
}
