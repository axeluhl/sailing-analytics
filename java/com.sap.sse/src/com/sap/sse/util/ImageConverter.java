package com.sap.sse.util;

import static java.lang.Math.toIntExact;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private static int[] calculateActualDimensions(double width, double height, double minWidth, double maxWidth,
            double minHeight, double maxHeight, boolean upsize) {
        if (maxWidth > 0 && maxHeight > 0 && maxHeight > minHeight && maxWidth > minWidth) {
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
            } else if (upsize) {
                if (width / maxWidth > height / maxHeight) {
                    minHeight = height / width * maxWidth;
                    if (maxHeight >= minHeight) {
                        return new int[] { (int) maxWidth, (int) minHeight };
                    }

                } else {
                    minWidth = width / height * maxHeight;
                    if (maxWidth >= minWidth) {
                        return new int[] { (int) minWidth, (int) maxHeight };
                    }
                }
            }
        }
        return null;
    }

    public static BufferedImage resize(BufferedImage img, int demandWidth, int demandHeight) {
        return resizeScale(img, demandWidth, demandHeight);
    }

    public static BufferedImage resize(BufferedImage img, int minWidth, int maxWidth, int minHeight, int maxHeight,
            String imageFormat, boolean upsize) {
        int[] dimensions = calculateActualDimensions(img.getWidth(), img.getHeight(), minWidth, maxWidth, minHeight,
                maxHeight, upsize);
        if (dimensions != null) {
            return resize(img, dimensions[0], dimensions[1]);
        }
        return null;
    }
    
    public static InputStream biToIs(BufferedImage img, String fileType) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, fileType, bos);
            byte[] arr = bos.toByteArray();
            return new ByteArrayInputStream(arr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertToBase64(InputStream is, String imageFormat) {
        return convertToBase64(biToBy(isToBi(is), imageFormat));
    }
    
    public static String convertToBase64(BufferedImage img, String imageFormat) {
        return convertToBase64(biToBy(img, imageFormat));
    }

    private static String convertToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static BufferedImage isToBi(InputStream is) {
        try {
            return ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] biToBy(BufferedImage img, String imageFormat) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, imageFormat, baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private static BufferedImage resizeScale(BufferedImage img, int demandedWidth, int demandedHeight) {
        BufferedImage resizedImage = new BufferedImage((int) demandedWidth, (int) demandedHeight, img.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(img, 0, 0, (int) demandedWidth, (int) demandedHeight, null);
        g.dispose();
        return resizedImage;
    }

    public static void splitSizeTags(Map<String, Boolean> sizeTags, List<String> resizeTags,
            List<String> notResizeSizeTags) {
        for (Object tagKey : sizeTags.keySet()) {
            if (sizeTags.get(tagKey)) {
                resizeTags.add((String) tagKey);// size tags, that have the resize checkBox checked
            } else {
                notResizeSizeTags.add((String) tagKey);// size tags, that not have the resize checkBox checked
            }
        }
    }
    
    public static InputStream storeImage(BufferedImage resizedBufferedImage, String fileType, IIOMetadata metaData, Logger logger) throws Exception {
        byte[] bytes = null;
        // trying to obtain OutputStream of the image with EXIF data
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(new ByteArrayOutputStream())) {
            // the following should write the exif data of the image to all copies of the image//it should already work,
            // but due to a bug the data array stays empty
            Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(fileType);
            while (writers.hasNext() && bytes == null) {
                ImageWriter writer = writers.next();
                if (writer != null) {
                    writer.setOutput(ios);
                    IIOImage iioImage = new IIOImage(resizedBufferedImage, null, metaData);
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
        }// if obtaining an OutputStream if the image with EXIF data did not work, then write it without
        InputStream toReturn;
        if (bytes == null) {
            toReturn = ImageConverter.biToIs(resizedBufferedImage, fileType);
        } else {// if it did work, then write the OutputStream to the FileStorageService
            toReturn =  new ByteArrayInputStream(bytes);
        }
        return toReturn;
    }

    private static boolean isZeroByteArray(byte[] bytes) {
        boolean toReturn = true;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != 0) {
                toReturn = false;
            }
        }
        return toReturn;
    }

    public static class BufferedImageWithMetadataDTO {
        private BufferedImage image;
        private IIOMetadata metadata;
        
        public BufferedImageWithMetadataDTO(BufferedImage image, IIOMetadata metadata) {
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
    
    public static BufferedImageWithMetadataDTO getImageAndMetadata(String fileType, InputStream is, Logger logger) throws Exception {
        // trying to receive the EXIF data and loading the image. If this does not work only the image is loaded
        boolean loaded = false;
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        IIOMetadata metadata = null;
        BufferedImage image = null;
        try {
            Iterator<ImageReader> readerIterator = ImageIO.getImageReadersBySuffix(fileType);
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
                image = ImageConverter.isToBi(new ByteArrayInputStream(bytes));
        }
        is.close();
        return new BufferedImageWithMetadataDTO(image, metadata);
    }
}
