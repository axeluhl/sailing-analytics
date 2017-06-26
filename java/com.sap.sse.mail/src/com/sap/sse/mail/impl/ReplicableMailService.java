package com.sap.sse.mail.impl;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.mail.MailService;

public interface ReplicableMailService extends MailService {
    Void internalSendMail(String toAddress, String subject, String body) throws MailException, MessagingException;

    /**
     * @param multipartContentAsByteArray
     *            the result of writing the {@link Multipart} object to a byte array output stream using
     *            {@link Multipart#writeTo(java.io.OutputStream)}. This can then be used to assemble a
     *            {@link MimeMessage} with {@link MimeMessage#MimeMessage(javax.mail.Session, java.io.InputStream)}.
     */
    Void internalSendMail(String toAddress, String subject, byte[] multipartContentAsByteArray) throws MailException, MessagingException;
}
