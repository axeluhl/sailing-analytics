package com.sap.sse.mail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

public class SerializableFileMimeBodyPartSupplier implements SerializableMimeBodyPartSupplier {

    private static final long serialVersionUID = 5830871801282568701L;

    private final byte[] bytes;
    private final String contentType;
    private final String contentId;
    private final String disposition;

    public SerializableFileMimeBodyPartSupplier(final byte[] bytes, final String contentType, final String contentId,
            final String filename, final boolean isAttachment) {
        this.bytes = bytes;
        this.contentType = contentType;
        this.contentId = contentId;
        this.disposition = (isAttachment ? Part.ATTACHMENT : Part.INLINE) + ";filename=\"" + filename + "\"";
    }

    @Override
    public MimeBodyPart get() {
        final MimeBodyPart messageImagePart = new MimeBodyPart();
        final DataSource imageDs = new ByteArrayDataSource(bytes, contentType);
        try {
            messageImagePart.setDataHandler(new DataHandler(imageDs));
            messageImagePart.setContentID(contentId);
            messageImagePart.setDisposition(disposition);
            return messageImagePart;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
