package com.sap.sse.mail;


public interface MailServiceResolver {
    /**
     * @return {@code null} if this service is not known
     */
    MailService getMailService();
}
