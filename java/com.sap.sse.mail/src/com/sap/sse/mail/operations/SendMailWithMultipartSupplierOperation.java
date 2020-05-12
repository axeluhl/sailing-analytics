package com.sap.sse.mail.operations;

import com.sap.sse.mail.SerializableMultipartSupplier;
import com.sap.sse.mail.impl.ReplicableMailService;

public class SendMailWithMultipartSupplierOperation implements MailServiceOperation<Void> {
    private static final long serialVersionUID = 3860668154718530111L;
    protected final String toAddress;
    protected final String subject;
    protected final SerializableMultipartSupplier multipartSupplier;

    public SendMailWithMultipartSupplierOperation(String toAddress, String subject,
            SerializableMultipartSupplier multipartSupplier) {
        this.toAddress = toAddress;
        this.subject = subject;
        this.multipartSupplier = multipartSupplier;
    }

    @Override
    public Void internalApplyTo(ReplicableMailService toState) throws Exception {
        toState.internalSendMail(toAddress, subject, multipartSupplier);
        return null;
    }
}
