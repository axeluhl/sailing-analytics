package com.sap.sailing.domain.test;

import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.junit.Test;

public class CalculateImageSizeFromUrlTest {

    @Test
    public void calculateImageSizeFromUrl() throws IOException {
        int width = 350;
        int height = 150;
        Dimension size = calculate("http://placehold.it/" + width + "x" + height);
        assertTrue(size.getWidth() == width);
        assertTrue(size.getHeight() == height);
    }

    public Dimension calculate(String urlS) throws IOException {
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
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
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
}
