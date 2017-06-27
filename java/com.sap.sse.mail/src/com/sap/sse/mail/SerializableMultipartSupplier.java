package com.sap.sse.mail;

import java.util.function.Supplier;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;

import com.sap.sse.common.Named;
import com.sap.sse.common.impl.NamedImpl;

public class SerializableMultipartSupplier extends NamedImpl implements Supplier<Multipart>, Named {
    private static final long serialVersionUID = 4316486727705670702L;
    private final SerializableMimeBodyPartSupplier[] bodyPartSuppliers;

    public SerializableMultipartSupplier(String name, SerializableMimeBodyPartSupplier... partSuppliers) {
        super(name);
        this.bodyPartSuppliers = partSuppliers;
    }

    @Override
    public Multipart get() {
        final Multipart result = new MimeMultipart(getName());
        for (final SerializableMimeBodyPartSupplier bodyPartSupplier : bodyPartSuppliers) {
            try {
                result.addBodyPart(bodyPartSupplier.get());
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
