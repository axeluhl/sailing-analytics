package com.sap.sse.mail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

public class SerializableFileMimeBodyPartSupplier implements SerializableMimeBodyPartSupplier {

    private static final long serialVersionUID = 4259007918955254014L;

    private final byte[] bytes;
    private final String contentType;
    private final String contentId;
    private final String disposition;

    public SerializableFileMimeBodyPartSupplier(byte[] bytes, String contentType, String filename) {
        this(bytes, contentType, null, filename, Part.ATTACHMENT);
    }

    public SerializableFileMimeBodyPartSupplier(byte[] bytes, String contentType, String contentId, String filename) {
        this(bytes, contentType, contentId, filename, Part.INLINE);
    }

    private SerializableFileMimeBodyPartSupplier(final byte[] bytes, final String contentType, final String contentId,
            final String filename, final String presentedAs) {
        this.bytes = bytes;
        this.contentType = contentType;
        this.contentId = contentId;
        this.disposition = presentedAs + ";filename=\"" + filename + "\"";
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
