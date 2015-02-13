package com.sap.sse.mail.replication.testsupport;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.mail.MailService;
import com.sap.sse.mail.impl.MailServiceImpl;
import com.sap.sse.replication.testsupport.AbstractServerReplicationTestSetUp;
import com.sap.sse.replication.testsupport.AbstractServerWithSingleServiceReplicationTest;

public class AbstractMailReplicationTest extends AbstractServerWithSingleServiceReplicationTest<MailService, MailServiceImpl> {
    public AbstractMailReplicationTest() {
        super(new MailServerReplicationTestSetUp());
    }

    public static final Map<MailServiceImpl, Integer> numberOfMailsSent = new HashMap<>();
    
    @Before
    public void setUp() {
        numberOfMailsSent.clear();
    }
    
    public static MailServiceImpl createMailCountingService(final boolean canSendMail) {
        return new MailServiceImpl(null) {
            @Override
            protected void internalSendMail(String toAddress, String subject, ContentSetter contentSetter)
                    throws MailException {
                if (canSendMail) {
                    Integer old = numberOfMailsSent.get(this);
                    numberOfMailsSent.put(this, old == null ? 1 : old + 1);
                }
            }
        };
    }

    public static class MailServerReplicationTestSetUp extends AbstractServerReplicationTestSetUp<MailService, MailServiceImpl> {
        public MailServerReplicationTestSetUp() {
            super();
        }
        
        public MailServerReplicationTestSetUp(int servletPort) {
            super(servletPort);
        }
            
        @Override
        protected MailServiceImpl createNewMaster() throws MailException {
            return createMailCountingService(true);
        }

        @Override
        protected MailServiceImpl createNewReplica() throws MailException {
            return createMailCountingService(false);
        }
    }

}
