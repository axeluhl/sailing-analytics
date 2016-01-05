package com.sap.sse.shared.media;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.MimeType;

public class MediaUtils {
    private static final Logger logger = Logger.getLogger(MediaUtils.class.getName());

    /**
     * Youtube regex detection from:
     * http://stackoverflow.com/questions/3452546/javascript-regex-how-to-get-youtube-video-id-from-url, mantish Mar 4
     * at 15:33
     */
    private static final Pattern YOUTUBE_ID_REGEX = Pattern
            .compile("^.*(youtu.be/|v/|u/\\w/|embed/|watch\\?v=|\\&v=)([^#\\&\\?]+).*$");

    private static final Pattern VIMEO_REGEX = Pattern.compile("^.*(vimeo\\.com\\/).*");

    private static final Pattern MP4_REGEX = Pattern.compile(".*\\.mp4$");

    /**
     * Detect mimetype for given url.
     * 
     * @param url
     *            the source pointing to the video mediafile
     * @return mimetype detected or MimeType.unknown
     */
    public static MimeType detectMimeTypeFromUrl(String url) {

        if (YOUTUBE_ID_REGEX.matcher(url).matches()) {
            return MimeType.youtube;
        } else if (VIMEO_REGEX.matcher(url).matches()) {
            return MimeType.vimeo;
        } else if (MP4_REGEX.matcher(url).matches()) {
            return MimeType.mp4;
        } else {
            return MimeType.unknown;
        }
    }
    
    public static Pair<Integer, Integer> getImageDimensions(URL imageURL) {
        Future<Pair<Integer, Integer>> imageSizeCalculator = getOrCreateImageSizeCalculator(imageURL);
        try {
            return imageSizeCalculator.get();
        } catch (Exception e) {
            return null;
        }
    }
    
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private static Future<Pair<Integer, Integer>> getOrCreateImageSizeCalculator(final URL imageURL) {
            Future<Pair<Integer, Integer>> imageSizeFetcher = executor.submit(new Callable<Pair<Integer, Integer>>() {
                @Override
                public Pair<Integer, Integer> call() throws IOException {
                    Pair<Integer, Integer> result = null;
                    ImageInputStream in = null;
                    try {
                        URLConnection conn = imageURL.openConnection();
                        in = ImageIO.createImageInputStream(conn.getInputStream());
                        final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                        if (readers.hasNext()) {
                            ImageReader reader = readers.next();
                            try {
                                reader.setInput(in);
                                result = new Pair<>(reader.getWidth(0), reader.getHeight(0));
                            } finally {
                                reader.dispose();
                            }
                        }
                    } catch (IOException ioe) {
                        logger.log(Level.SEVERE, "Stale image URL: "+imageURL, ioe);
                        throw ioe;
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                    return result;
                }
            });
        return imageSizeFetcher;
    }
    
    public static Util.Pair<Integer, Integer> fitImageSizeToBox(int boxWidth, int boxHeight, int imageWidth, int imageHeight, boolean neverScaleUp) {
        double scale = Math.min((double) boxWidth / (double) imageWidth, (double) boxHeight / (double) imageHeight);

        int h = (int) (!neverScaleUp || scale < 1.0 ? scale * imageHeight : imageHeight);
        int w = (int) (!neverScaleUp || scale < 1.0 ? scale * imageWidth : imageWidth);
        
        return new Util.Pair<Integer, Integer>(w,h);
    }
}
