package com.sap.sse.common.media;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;

public class ImageConverter {

    public static void main(String[] args) {
        try {
            BufferedImage image = ImageIO.read(new File("C:\\Users\\D067799\\Desktop\\img3.jpg"));
            ImageIO.write(image, "jpg", new File("C:\\Users\\D067799\\Desktop\\img2.jpg"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static String resizeAndConvertToBase64(InputStream is, int minWidth, int maxWidth, int minHeight,
            int maxHeight, String imageFormat, boolean upsize) {
        BufferedImage img = isToBi(is);
        int[] dimensions = calculateActualDimensions(img.getWidth(), img.getHeight(), minWidth, maxWidth, minHeight,
                maxHeight, upsize);
        if(dimensions != null) {
            img = resize(img, dimensions[0], dimensions[1]);
            return convertToBase64(biToBy(img, imageFormat));
        }else {
            return null;
        }
    }

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

    private static BufferedImage resize(BufferedImage img, int demandWidth, int demandHeight) {
        return resizeScale(img, demandWidth, demandHeight);
    }

    public static String convertToBase64(InputStream is, String imageFormat) {
        return convertToBase64(biToBy(isToBi(is), imageFormat));
    }

    private static String convertToBase64(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    private static BufferedImage isToBi(InputStream is) {
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
        try {
            ImageIO.write(resizedImage, "jpg", new File("C:\\Users\\D067799\\Desktop\\img2.jpg"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resizedImage;
    }

}
