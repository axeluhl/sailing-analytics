package com.sap.sse.mail.impl;

import javax.mail.MessagingException;
import javax.mail.Multipart;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.mail.MailService;
import com.sap.sse.mail.SerializableMultipartSupplier;

public interface ReplicableMailService extends MailService {
    Void internalSendMail(String toAddress, String subject, String body) throws MailException, MessagingException;

    /**
     * @param multipartSupplier
     *            can produce a {@link Multipart} object and must be serializable
     */
    Void internalSendMail(String toAddress, String subject, SerializableMultipartSupplier multipartSupplier) throws MailException, MessagingException;
}
