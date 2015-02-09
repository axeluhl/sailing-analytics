package com.sap.sse.mail.impl;

import javax.mail.Multipart;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.mail.MailService;

public interface ReplicableMailService extends MailService {
    Void internalSendMail(String toAddress, String subject, String body) throws MailException;

    Void internalSendMail(String toAddress, String subject, Multipart multipartContent) throws MailException;
}
