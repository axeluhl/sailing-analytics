package com.sap.sse.mail;

import java.io.Serializable;
import java.util.function.Supplier;

import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;

/**
 * Plays together with {@link SerializableMultipartSupplier}. One or more instances of this type can be
 * passed to its constructor so that after de-serialization a {@link Multipart} message can be constructed
 * from the parts.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SerializableMimeBodyPartSupplier extends Supplier<MimeBodyPart>, Serializable {
}
