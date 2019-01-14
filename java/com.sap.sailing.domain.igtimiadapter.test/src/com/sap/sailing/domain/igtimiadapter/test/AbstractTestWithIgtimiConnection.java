package com.sap.sailing.domain.igtimiadapter.test;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.impl.Activator;

public class AbstractTestWithIgtimiConnection {
    protected IgtimiConnection connection;
    
    @Rule public Timeout AbstractTestWithIgtimiConnectionTimeout = Timeout.millis(2 * 60 * 1000);
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = Timeout.millis(2 * 60 * 1000);

    @Before
    public void setUp() throws ClientProtocolException, IOException, org.json.simple.parser.ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("9fded995cf21c8ed91ddaec13b220e8d5e44c65808d22ec2b1b7c32261121f26");
        connection = connectionFactory.connect(account);
    }

}
