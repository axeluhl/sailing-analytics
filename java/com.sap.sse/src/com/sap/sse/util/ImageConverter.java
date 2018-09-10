package com.sap.sse.util;

import static java.lang.Math.toIntExact;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import com.sap.sse.common.Base64Utils;
import com.sap.sse.common.media.MediaTagConstants;

/**
 * /** An Utility Class to help resizing an image
 * 
 * @author Robin Fleige (D067799)
 */
public class ImageConverter {
    private static final Logger logger = Logger.getLogger(ImageConverter.class.getName());

    /**
     * Empty constructor
     */
    public ImageConverter() {
    }

    private InputStream imageToInputStream(BufferedImage image, String imageFormat) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, imageFormat, bos);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        byte[] arr = bos.toByteArray();
        return new ByteArrayInputStream(arr);
    }

    /**
     * Resizes an BufferedImage, so it fits into the defined bounds without changing the width-height-ratio. The image
     * is kept as large as possible inside this bounds.
     * 
     * @param image
     *            The BufferedImage that should be resized
     * @param minWidth
     *            the minimum width the image should have after resizing
     * @param maxWidth
     *            the maximum width the image should have after resizing
     * @param minHeight
     *            the minimum height the image should have after resizing
     * @param maxHeight
     *            the maximum height the image should have after resizing
     * @returns the resized BufferedImage. Returns null, if BufferedImage is null, or if the defined bounds do not fit
     *          the image
     */
    public BufferedImage resize(BufferedImage image, int minWidth, int maxWidth, int minHeight, int maxHeight) {
        int[] dimensions = calculateDimensions(image.getWidth(), image.getHeight(), minWidth, maxWidth, minHeight,
                maxHeight);
        if (dimensions != null) {
            return resize(image, dimensions[0], dimensions[1]);
        }
        return null;
    }

    /**
     * Calculates the dimensions of an image, so it fits inside the defined bounds without changing the
     * width-height-ratio. Keeps it as big as possible
     * 
     * @param width
     *            the current width of the image that should be resized
     * @param height
     *            the current height of the image that should be resized
     * @param minWidth
     *            the minimum width the image should have after resizing
     * @param maxWidth
     *            the maximum width the image should have after resizing
     * @param minHeight
     *            the minimum height the image should have after resizing
     * @param maxHeight
     *            the maximum height the image should have after resizing
     * @returns an array of two integers, where the first entry is the fitting width and the second is the fitting
     *          height. returns null, if the defined bounds do not fit the current size
     */
    public int[] calculateDimensions(double width, double height, double minWidth, double maxWidth, double minHeight,
            double maxHeight) {
        if (maxWidth >= 0 && maxHeight >= 0 && maxHeight > minHeight && maxWidth > minWidth && width > minWidth
                && height > minHeight) {
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
                return new int[] { (int) width, (int) height };
            }
        }
        return null;
    }

    private BufferedImage resize(BufferedImage image, int demandedWidth, int demandedHeight) {
        BufferedImage resizedImage = new BufferedImage((int) demandedWidth, (int) demandedHeight, image.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, (int) demandedWidth, (int) demandedHeight, null);
        g.dispose();
        return resizedImage;
    }

    /**
     * Creates an InputStream from a BufferedImage, the IIOMetadata of the image and the imageFormat
     * 
     * @param bufferdImage
     *            the image that has to be written to an InputStream
     * @param metadata
     *            the IIOMetadata that should be stored in the InputStream with the BufferedImage
     * @param imageFormat
     *            the format of the image, for example "png", "jpeg" or "jpg"
     * @returns an InputStream (ByteArrayInputStream) with the data of the BufferedImage and if possible with the
     *          IIOMetadata. Returnds null if the image is null or the imageFormat is incorrect
     */
    public InputStream imageWithMetadataToInputStream(BufferedImage bufferdImage, IIOMetadata metadata,
            String imageFormat) {
        byte[] bytes = null;
        if (metadata != null) {
            // trying to obtain OutputStream of the image with EXIF data
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(new ByteArrayOutputStream())) {
                // the following should write the exif data of the image to all copies of the image, it should already
                // work, but due to a bug the data array stays empty
                Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(imageFormat);
                while (writers.hasNext() && bytes == null) {
                    ImageWriter writer = writers.next();
                    if (writer != null) {
                        writer.setOutput(ios);
                        IIOImage iioImage = new IIOImage(bufferdImage, null, metadata);
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
        }
        InputStream toReturn;
        if (bytes == null) {
            toReturn = imageToInputStream(bufferdImage, imageFormat);
        } else {// if it did work, then write the OutputStream to the FileStorageService
            toReturn = new ByteArrayInputStream(bytes);
        }
        return toReturn;
    }

    /**
     * Writes an inputstream into a ByteArray
     * 
     * @param inputStream
     *            the inputstream that should be stored in the ByteArray
     * @returns an Array of Bytes with the data of the inputstream
     */
    public byte[] inputStreamToByteArray(InputStream inputStream) {
        byte[] byteArray = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
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

    /**
     * Converts an inputstream that contains an image to a Base64 String
     * 
     * @param inputStream
     *            the InputStream that should be converted
     * @param imageFormat
     *            the format of the image, for example "png", "jpeg" or "jpg"
     * @returns the Base64 representation of the InputStream
     * @throws IOException
     */
    public String convertToBase64(InputStream inputStream, String imageFormat) throws IOException {
        return convertToBase64(bufferedImageToByteArray(loadBufferedImageFromInputStream(inputStream), imageFormat));
    }

    /**
     * Converts an BufferedImage to a Base64 String
     * 
     * @param image
     *            the image that should be converted as a BufferedImage
     * @param imageFormat
     *            the format of the image, for example "png", "jpeg" or "jpg"
     * @returns the Base64 representation of the image
     * @throws IOException
     */
    public String convertToBase64(BufferedImage image, String imageFormat) throws IOException {
        return convertToBase64(bufferedImageToByteArray(image, imageFormat));
    }

    private String convertToBase64(byte[] bytes) {
        return Base64Utils.toBase64(bytes);
    }

    private BufferedImage loadBufferedImageFromInputStream(InputStream is) {
        try {
            return ImageIO.read(is);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    // looses metadata, so should not be used for storing an image, but only for showing an image
    /**
     * Converts an BufferedImage to a ByteArray that contains all of its information This Method does not use the
     * IIOMetadata of the original image, so it should not be used to store images, but only for showing them. To also
     * use the IIOMetadata of the image use the @link
     * {@link ImageConverter#imageWithMetadataToInputStream(BufferedImage, IIOMetadata, String)} and then the @link
     * {@link ImageConverter#inputStreamToByteArray(InputStream)} method
     * 
     * @param image
     *            the image that should be converted to a ByteArray
     * @param imageFormat
     *            the format of the image, for example "png", "jpeg" or "jpg"
     * @returns an ByteArray with all the information of the image
     */
    public byte[] bufferedImageToByteArray(BufferedImage image, String imageFormat) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, imageFormat, baos);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return baos.toByteArray();
    }

    /**
     * Converts one BufferedImage to a list of at least one BufferedImage with different sizes as described in the given
     * {@link MediaTagConstants}
     * 
     * @param image
     *            the image that should be converted
     * @param resizingTasks
     *            a List of {@link MediaTagConstants} that define to which size the image should be resized
     * @returns a list of BufferedImages, that contains as many BufferedImages as resizingTask contains
     *          {@link MediaTagConstants}
     */
    public List<BufferedImage> convertImage(BufferedImage image, List<MediaTagConstants> resizingTasks) {
        List<BufferedImage> resizedImages = new ArrayList<>();
        for (MediaTagConstants tag : resizingTasks) {
            resizedImages
                    .add(resize(image, tag.getMinWidth(), tag.getMaxWidth(), tag.getMinHeight(), tag.getMaxHeight()));
        }
        return resizedImages;
    }

    /**
     * Loads an BufferedImage and the IIOMetadata from an InputStream that contains an image.
     * 
     * @param inputStream
     *            the InputStream that contains the image and the metadata
     * @param imageFormat
     *            the format of the image, for example "png", "jpeg" or "jpg"
     * @returns an {@link ImageWithMetadata} that contains the BufferedImage and the IIOMetadata of the image If the
     *          loading of the IIOMetadata does not work, returns an {@link ImageWithMetadata} where metadata is null
     */
    public ImageWithMetadata loadImage(InputStream inputStream, String imageFormat) {
        // trying to receive the EXIF data and loading the image. If this does not work only the image is loaded
        ImageConverter converter = new ImageConverter();
        BufferedImage image = null;
        IIOMetadata metadata = null;
        boolean loaded = false;
        byte[] bytes = converter.inputStreamToByteArray(inputStream);
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
            image = converter.loadBufferedImageFromInputStream(new ByteArrayInputStream(bytes));
            metadata = null;
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Potential ressource leak");
        }
        return new ImageWithMetadata(image, metadata);
    }

    /**
     * A Data Storage class to store an BufferedImage and IIOMetadata
     * 
     * @author Robin Fleige (D067799)
     *
     */
    public class ImageWithMetadata {
        private final BufferedImage image;
        private final IIOMetadata metadata;

        public ImageWithMetadata(BufferedImage image, IIOMetadata metadata) {
            this.image = image;
            this.metadata = metadata;
        }

        public BufferedImage getImage() {
            return image;
        }

        public IIOMetadata getMetadata() {
            return metadata;
        }
    }
}
