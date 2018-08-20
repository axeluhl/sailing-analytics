package com.sap.sse.mail;

import java.io.IOException;

import javax.mail.MessagingException;

import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.mail.impl.ReplicableMailService;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.impl.ReplicableWithObjectInputStream;
import com.sap.sse.util.ClearStateTestSupport;


/**
 * Announced via OSGi service registry. Requires {@code mail.properties} file in {@code configuration} directory
 * to be correctly configured.
 * 
 * A mail should only be sent out once by one instance. This is similar to only adding an item once to a store
 * (database). Therefore - while a sendMail() operation is replicated - it is only executed by one instance,
 * for now the master instance. All replicas receive an empty ({@code null}) mail configuration during
 * {@link Replicable#initiallyFillFrom initial replication}, and therefore do not try to send mails themselves.
 * 
 * @author Axel Uhl
 * @author Fredrik Teschke
 *
 */
public interface MailService extends ReplicableWithObjectInputStream<ReplicableMailService, MailOperation<?>>, ClearStateTestSupport, IsManagedByCache<MailServiceResolver> {
    void sendMail(String toAddress, String subject, String body) throws MailException;
    
    /**
     * Send mail with multipart content (e.g. inline image).
     */
    void sendMail(String toAddress, String subject, SerializableMultipartSupplier multipartContent) throws MailException, IOException, MessagingException;
}
