package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.maps.client.base.Size;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

/**
 * An image transformer which takes in ImageResource, rotates (specified angle) and scales it and draws it to a {@link Canvas}.
 * @author Axel Uhl (d043530)
 *
 */
public class ImageTransformer {
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
        canvas = Canvas.createIfSupported();
        imageWidth = imageResource.getWidth();
        imageHeight = imageResource.getHeight();
        scale(1.0);
        final Image image = new Image(imageResource.getSafeUri().asString());
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
    
    /**
     * Returns the canvas that contains the rotated / scaled image after calls to {@link #getTransformedImageData(double, double)} and
     * {@link #drawTransformedImage(double, double)}. Note that the canvas returned will change size if the image is drawn using
     * a different scale.
     */
    public Canvas getCanvas() {
        return canvas;
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
    
    /**
     * Obtains the image's "radius" for the last {@link #scale(double)} call or for scale 1.0 if {@link #scale(double)} hasn't been
     * called explicitly yet.
     */
    public int getRadius() {
        return canvasRadius;
    }
    
    public Size getImageSize() {
        return Size.newInstance(imageWidth, imageHeight);
    }
    
    public ImageData getTransformedImageData(double angleInDegrees, double scaleFactor) {
        ImageData result = null;
        if (canvas != null) {
            if (imageElement != null) {
                drawTransformedImage(angleInDegrees, scaleFactor);
                result = context.getImageData(0, 0, 2*canvasRadius, 2*canvasRadius);
            }
        }
        return result;
    }
    
    public void drawTransformedImage(double angleInDegrees, double scaleFactor) {
        if (canvas != null) {
            if (imageElement != null) {
                // scale 1.0 already done in constructor
                if (scaleFactor != 1.0) {
                    scale(scaleFactor);
                }
                double angleInRadians = angleInDegrees/180.*Math.PI;
                context.clearRect(0, 0, 2*canvasRadius, 2*canvasRadius);
                context.save();
                context.translate(canvasRadius, canvasRadius);
                context.rotate(angleInRadians);
                context.scale(scaleFactor, scaleFactor);
                context.drawImage(imageElement, (-imageWidth/2), (-imageHeight/2));
                context.restore();
            }
        }
    }

    public void drawToCanvas(Canvas canvas, double angleInDegrees, double scaleFactor) {
        if (imageElement != null) {
            int canvasWidth = (int) Math.round(imageWidth * scaleFactor);
            int canvasHeight = (int) Math.round(imageHeight * scaleFactor);
            int canvasRadius = (int) Math.sqrt(canvasWidth * canvasWidth / 4 + canvasHeight * canvasHeight / 4);
            canvas.setSize("" + 2 * canvasRadius + "px", "" + 2 * canvasRadius + "px");
            canvas.setCoordinateSpaceWidth(2 * canvasRadius);
            canvas.setCoordinateSpaceHeight(2 * canvasRadius);
            Context2d context = canvas.getContext2d();
            double angleInRadians = angleInDegrees/180.*Math.PI;
            context.clearRect(0, 0, 2*canvasRadius, 2*canvasRadius);
            context.save();
            context.translate(canvasRadius, canvasRadius);
            context.rotate(angleInRadians);
            context.scale(scaleFactor, scaleFactor);
            context.drawImage(imageElement, (-imageWidth/2), (-imageHeight/2));
            context.restore();
        }
    }
}
