package com.sap.sse.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageConverter {

    private static int[] calculateActualDimensions(double width, double height, double minWidth, double maxWidth, double minHeight,
            double maxHeight, boolean upsize) {
        if (maxWidth > 0 && maxHeight > 0 && maxHeight > minHeight && maxWidth > minWidth) {
            if(maxWidth <= width || maxHeight <= height) {
                if (width/maxWidth>height/maxHeight) {
                    maxHeight = height / width * maxWidth;
                    if(maxHeight >= minHeight) {
                        return new int[]{(int)maxWidth,(int)maxHeight};
                    }
                } else {
                    maxWidth = width / height * maxHeight;
                    if(maxWidth >= minWidth) {
                        return new int[]{(int)maxWidth,(int)maxHeight};
                    }
                }
            }else if(upsize) {
                if (width/maxWidth>height/maxHeight) {
                    minHeight = height / width * maxWidth;
                    if(maxHeight >= minHeight) {
                        return new int[]{(int)maxWidth,(int)minHeight};
                    }
                    
                } else {
                    minWidth = width / height * maxHeight;
                    if(maxWidth >= minWidth) {
                        return new int[]{(int)minWidth,(int)maxHeight};
                    }
                }
            }
        }
        return null;
    }

    public static BufferedImage resize(BufferedImage img, int demandWidth, int demandHeight) {
        return resizeScale(img, demandWidth, demandHeight);
    }
    
    public static BufferedImage resize(BufferedImage img, int minWidth, int maxWidth, int minHeight,
            int maxHeight, String imageFormat, boolean upsize) {
        int[] dimensions = calculateActualDimensions(img.getWidth(), img.getHeight(), minWidth, maxWidth, minHeight,
                maxHeight, upsize);
        if(dimensions != null) {
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
    
    public static void splitSizeTags(Map<String,Boolean> sizeTags, List<String> resizeTags, List<String> notResizeSizeTags) {
        for(Object tagKey : sizeTags.keySet()) {
            if(sizeTags.get(tagKey)) {
                resizeTags.add((String)tagKey);//size tags, that have the resize checkBox checked
            }else{
                notResizeSizeTags.add((String)tagKey);//size tags, that not have the resize checkBox checked
            }
        }
    }
}
