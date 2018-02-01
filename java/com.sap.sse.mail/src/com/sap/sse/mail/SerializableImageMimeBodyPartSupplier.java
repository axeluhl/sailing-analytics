package com.sap.sse.mail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

public class SerializableImageMimeBodyPartSupplier implements SerializableMimeBodyPartSupplier {
    private static final long serialVersionUID = -1540223936240644379L;
    private final byte[] imageBytes;
    private final String contentType;
    private final String contentId;
    private final String imageFilename;

    public SerializableImageMimeBodyPartSupplier(byte[] imageBytes, String contentType, String contentId,
            String imageFilename) {
        super();
        this.imageBytes = imageBytes;
        this.contentType = contentType;
        this.contentId = contentId;
        this.imageFilename = imageFilename;
    }

    @Override
    public MimeBodyPart get() {
        final MimeBodyPart messageImagePart = new MimeBodyPart();
        DataSource imageDs = new ByteArrayDataSource(imageBytes, contentType);
        try {
            messageImagePart.setDataHandler(new DataHandler(imageDs));
            messageImagePart.setHeader("Content-ID", contentId);
            messageImagePart.setHeader("Content-Disposition", "inline;filename=\"" + imageFilename + "\"");
            return messageImagePart;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
