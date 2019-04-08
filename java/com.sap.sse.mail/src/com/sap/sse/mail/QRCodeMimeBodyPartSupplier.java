package com.sap.sse.mail;

import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import com.sap.sse.qrcode.QRCodeGenerationUtil;

/**
 * Constructs a MIME body part containing a QR code for a {@link #text} provided to this object
 * at construction time.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class QRCodeMimeBodyPartSupplier implements SerializableMimeBodyPartSupplier {
    private static final long serialVersionUID = 6073707813859319317L;
    private final String text;
    
    public QRCodeMimeBodyPartSupplier(String text) {
        super();
        this.text = text;
    }

    @Override
    public MimeBodyPart get() {
        final MimeBodyPart messageImagePart = new MimeBodyPart();
        InputStream imageIs;
        try {
            imageIs = QRCodeGenerationUtil.create(text, 250);
            final DataSource imageDs = new ByteArrayDataSource(imageIs, "image/png");
            messageImagePart.setDataHandler(new DataHandler(imageDs));
            messageImagePart.setHeader("Content-ID", "<image>");
            messageImagePart.setHeader("Content-Disposition", "inline;filename=\"qr.png\"");
            return messageImagePart;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
