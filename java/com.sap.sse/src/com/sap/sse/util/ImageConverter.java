package com.sap.sse.util;

import static java.lang.Math.toIntExact;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;


public class ImageConverter {
    private final BufferedImage image;
    private IIOMetadata metadata;
    private final String imageFormat;
    private final List<String> resizeTags;
    private final List<String> notResizeSizeTags;
    private static final Logger logger = Logger.getLogger(ImageConverter.class.getName());

    public ImageConverter(InputStream is, String imageFormat, Map<String,Boolean> sizeTags) {
        this.imageFormat = imageFormat;
        this.image = calculateImageAndMetadata(is);
        // splitting the size-tags in size-tags that need a resize and size-tags that do not need a resize
        resizeTags = new ArrayList<>();
        notResizeSizeTags = new ArrayList<>();
        splitSizeTags(sizeTags, resizeTags, notResizeSizeTags);
    }

    private BufferedImage calculateImageAndMetadata(InputStream is) {
        // trying to receive the EXIF data and loading the image. If this does not work only the image is loaded
        BufferedImage image = null;
        boolean loaded = false;
        byte[] bytes = inputStreamToByteArray(is);
        try {
            Iterator<ImageReader> readerIterator = ImageIO.getImageReadersBySuffix(imageFormat);
            while (readerIterator.hasNext() && !loaded) {
                ImageReader reader = readerIterator.next();
                reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(bytes)));
                metadata = reader.getImageMetadata(0);
                image = reader.read(0);
                loaded = metadata != null;
            }
            if (!loaded) {
                throw new Exception(
                        "Loading file via ImageReader did not work, not able to load metadata, retrying to load with ImageIO.read()");
            }
        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }
        if (!loaded) {
            image = loadBufferedImageFromInputStream(new ByteArrayInputStream(bytes));
        }
        try {
            is.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Potential ressource leak");
        }
        return image;
    }

    private void splitSizeTags(Map<String, Boolean> sizeTags, List<String> resizeTags, List<String> notResizeSizeTags) {
        for (Object tagKey : sizeTags.keySet()) {
            if (sizeTags.get(tagKey)) {
                resizeTags.add((String) tagKey);// size tags, that have the resize checkBox checked
            } else {
                notResizeSizeTags.add((String) tagKey);// size tags, that not have the resize checkBox checked
            }
        }
    }

    public InputStream imageToInputStream(BufferedImage image) {//public for testing
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, imageFormat, bos);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        byte[] arr = bos.toByteArray();
        return new ByteArrayInputStream(arr);
    }

    public BufferedImage resize(int minWidth, int maxWidth, int minHeight, int maxHeight) {
        int[] dimensions = calculateDimensions(image.getWidth(), image.getHeight(), minWidth, maxWidth, minHeight,
                maxHeight);
        if (dimensions != null) {
            return resize(dimensions[0], dimensions[1]);
        }
        return null;
    }

    public static int[] calculateDimensions(double width, double height, double minWidth, double maxWidth,
            double minHeight, double maxHeight) {//Public for testing, static because it is completely independend
        if (maxWidth >= 0 && maxHeight >= 0 && maxHeight > minHeight && maxWidth > minWidth && width > minWidth && height > minHeight) {
            if (maxWidth <= width || maxHeight <= height) {
                if (width / maxWidth > height / maxHeight) {
                    maxHeight = height / width * maxWidth;
                    if (maxHeight >= minHeight) {
                        return new int[] { (int) maxWidth, (int) maxHeight };
                    }
                } else {
                    maxWidth = width / height * maxHeight;
                    if (maxWidth >= minWidth) {
                        return new int[] { (int) maxWidth, (int) maxHeight };
                    }
                }
            } else {
                return new int[] {(int) width, (int) height};
            }
        }
        return null;
    }

    private BufferedImage resize(int demandWidth, int demandHeight) {
        return resizeScale(demandWidth, demandHeight);
    }

    private BufferedImage resizeScale(int demandedWidth, int demandedHeight) {
        BufferedImage resizedImage = new BufferedImage((int) demandedWidth, (int) demandedHeight, image.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, (int) demandedWidth, (int) demandedHeight, null);
        g.dispose();
        return resizedImage;
    }

    public InputStream resizedImageToInputStream(BufferedImage resizedBufferedImage) {
        byte[] bytes = null;
        // trying to obtain OutputStream of the image with EXIF data
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(new ByteArrayOutputStream())) {
            // the following should write the exif data of the image to all copies of the image//it should already work,
            // but due to a bug the data array stays empty
            Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(imageFormat);
            while (writers.hasNext() && bytes == null) {
                ImageWriter writer = writers.next();
                if (writer != null) {
                    writer.setOutput(ios);
                    IIOImage iioImage = new IIOImage(resizedBufferedImage, null, metadata);
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(param);
                    writer.write(streamMetadata, iioImage, param);
                    writer.dispose();
                    bytes = new byte[toIntExact(ios.length())];
                    ios.read(bytes);
                    if (isZeroByteArray(bytes)) {
                        bytes = null;
                    }
                }
            }
            if (bytes == null) {
                throw new Exception(
                        "Saving file via FileWriter did not work, not able to write file with metadata, retrying with ImageIO.write()");
            }

        } catch (Exception e) {
            bytes = null;
            logger.log(Level.INFO, e.getMessage());
        } // if obtaining an OutputStream if the image with EXIF data did not work, then write it without
        InputStream toReturn;
        if (bytes == null) {
            toReturn = imageToInputStream(resizedBufferedImage);
        } else {// if it did work, then write the OutputStream to the FileStorageService
            toReturn = new ByteArrayInputStream(bytes);
        }
        return toReturn;
    }

    public byte[] inputStreamToByteArray(InputStream is) {//public for testing
        byte[] byteArray = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byteArray = buffer.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return byteArray;
    }

    private boolean isZeroByteArray(byte[] bytes) {
        boolean toReturn = true;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != 0) {
                toReturn = false;
            }
        }
        return toReturn;
    }

    public BufferedImage getImage() {
        return image;
    }

    public IIOMetadata getMetadata() {
        return metadata;
    }
    
    public String getImageFormat() {
        return imageFormat;
    }

    public List<String> getResizeTags() {
        return resizeTags;
    }

    public List<String> getNotResizeSizeTags() {
        return notResizeSizeTags;
    }
    //these methods are static, so it is not necessary to create an ImageConverter object for the conversion to Base64
    public static String convertToBase64(InputStream is, String imageFormat) throws IOException {
        return convertToBase64(bufferedImageToByteArray(loadBufferedImageFromInputStream(is), imageFormat));
    }

    public static String convertToBase64(BufferedImage img, String imageFormat) throws IOException {
        return convertToBase64(bufferedImageToByteArray(img, imageFormat));
    }

    private static String convertToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static BufferedImage loadBufferedImageFromInputStream(InputStream is) {
        try {
            return ImageIO.read(is);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    public static byte[] bufferedImageToByteArray(BufferedImage img, String imageFormat) {//public for testing
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, imageFormat, baos);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return baos.toByteArray();
    }
}
