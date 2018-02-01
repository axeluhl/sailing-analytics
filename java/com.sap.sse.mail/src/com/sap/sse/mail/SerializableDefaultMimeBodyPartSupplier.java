package com.sap.sse.mail;

import java.io.Serializable;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

/**
 * Can construct a {@link MimeBodyPart} out of a serializable object and a content type specification string.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SerializableDefaultMimeBodyPartSupplier implements SerializableMimeBodyPartSupplier {
    private static final long serialVersionUID = -2893291156595020570L;
    private final Serializable serializableContent;
    private final String contentType;
    
    public SerializableDefaultMimeBodyPartSupplier(Serializable serializableContent, String contentType) {
        super();
        this.serializableContent = serializableContent;
        this.contentType = contentType;
    }

    @Override
    public MimeBodyPart get() {
        final MimeBodyPart result = new MimeBodyPart();
        try {
            result.setContent(serializableContent, contentType);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
