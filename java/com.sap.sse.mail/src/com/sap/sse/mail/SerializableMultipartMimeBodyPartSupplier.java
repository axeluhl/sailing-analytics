package com.sap.sse.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

/**
 * Can construct a {@link MimeBodyPart} with the content provided by a {@link SerializableMultipartSupplier}.
 */
public class SerializableMultipartMimeBodyPartSupplier implements SerializableMimeBodyPartSupplier {

    private static final long serialVersionUID = 8834416573931610355L;

    private final SerializableMultipartSupplier multipartSupplier;

    public SerializableMultipartMimeBodyPartSupplier(final SerializableMultipartSupplier multipartSupplier) {
        this.multipartSupplier = multipartSupplier;
    }

    @Override
    public MimeBodyPart get() {
        final MimeBodyPart result = new MimeBodyPart();
        try {
            result.setContent(multipartSupplier.get());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
