package com.sap.sse.qrcode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * QRCode generator based on the ZXing library.
 * 
 * TODO have a look at repackaging <a href="https://github.com/kenglxn/QRGen">QRGen</a> as OSGi bundle.
 * 
 * @author Fredrik Teschke
 *
 */
public class QRCodeGenerationUtil {
    public static InputStream create(String text, int sizeInPixels) throws Exception {
         Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType,
         ErrorCorrectionLevel>();
         hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
         return create(text, sizeInPixels, hintMap);
    }
    
    public static InputStream create(String text, int sizeInPixels,
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap) throws WriterException, IOException {
        //taken from http://crunchify.com/java-simple-qr-code-generator-example/
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, sizeInPixels, sizeInPixels, hintMap);
        int matrixSize = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixSize, matrixSize, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixSize, matrixSize);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        //TODO create InputStream from streamed output of image instead of handling complete byte arrays
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "gif", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        return is;
    }
}
