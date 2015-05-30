package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.junit.Test;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.common.impl.ImageSizeImpl;
import com.sap.sse.common.media.ImageSize;

public class CalculateImageSizeFromUrlTest {

    @Test
    public void calculateImageSizeFromUrl() throws IOException {
        int width = 350;
        int height = 150;
        ImageSize size = calculate("http://placehold.it/" + width + "x" + height);
        assertTrue(size.getWidth() == width);
        assertTrue(size.getHeight() == height);
    }

    public ImageSize calculate(String urlS) throws IOException {
        ImageInputStream in = null;
        try {
            URL url = new URL(urlS);
            URLConnection conn = url.openConnection();
            in = ImageIO.createImageInputStream(conn.getInputStream());
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new ImageSizeImpl(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return null;
    }
    
    @Test
    public void testEventImageSize() throws MalformedURLException, InterruptedException, ExecutionException {
        Event e = new EventImpl("Event Name", /* startDate */ null, /* endDate */ null, "Kiel", /* isPublic */ true, UUID.randomUUID());
        int width = Math.max(10, (int) (200. * Math.random()));
        int height = Math.max(10, (int) (100. * Math.random()));
        URL imageURL = new URL("http://placehold.it/" + width + "x" + height);
        e.addImageURL(imageURL);
        ImageSize expectedSize = new ImageSizeImpl(width, height);
        assertEquals(expectedSize, e.getImageSize(imageURL));
    }
}
