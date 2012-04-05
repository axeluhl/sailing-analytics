package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
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
    
    public ImageTransformer(ImageResource imageResource) {
        this.untransformedImageURL = imageResource.getSafeUri().asString();
        canvas = Canvas.createIfSupported();
        imageWidth = imageResource.getWidth();
        imageHeight = imageResource.getHeight();
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

    public ImageData getTransformedImageData(double angleInDegrees, double scaleFactor) {
        ImageData result = null;
        if (canvas != null) {
            if (imageElement != null) {
                if(scaleFactor != 1.0)
                    scale(scaleFactor);
                double angleInRadians = angleInDegrees/180.*Math.PI;
                context.clearRect(0, 0, 2*canvasRadius, 2*canvasRadius);
                context.save();
                context.translate(canvasRadius, canvasRadius);
                context.rotate(angleInRadians);
                context.scale(scaleFactor, scaleFactor);
                context.drawImage(imageElement, (-imageWidth/2), (-imageHeight/2));
                
                result = context.getImageData(0, 0, 2*canvasRadius, 2*canvasRadius);
                context.restore();
            }
        }
        return result;
    }
    
}
