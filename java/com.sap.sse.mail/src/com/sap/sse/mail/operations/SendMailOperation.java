package com.sap.sse.mail.operations;

import com.sap.sse.mail.impl.ReplicableMailService;

public class SendMailOperation implements MailServiceOperation<Void> {
    private static final long serialVersionUID = 3410841670705846547L;
    protected final String toAddress;
    protected final String subject;
    protected final String body;

    public SendMailOperation(String toAddress, String subject, String body) {
        this.toAddress = toAddress;
        this.subject = subject;
        this.body = body;
    }

    @Override
    public Void internalApplyTo(ReplicableMailService toState) throws Exception {
        toState.internalSendMail(toAddress, subject, body);
        return null;
    }
}
