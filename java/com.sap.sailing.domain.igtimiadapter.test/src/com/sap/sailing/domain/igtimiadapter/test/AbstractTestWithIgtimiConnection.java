package com.sap.sailing.domain.igtimiadapter.test;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.impl.Activator;

public class AbstractTestWithIgtimiConnection {
    protected IgtimiConnection connection;
    
    @Before
    public void setUp() throws ClientProtocolException, IOException, org.json.simple.parser.ParseException {
        final IgtimiConnectionFactory connectionFactory = Activator.getInstance().getConnectionFactory();
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("9fded995cf21c8ed91ddaec13b220e8d5e44c65808d22ec2b1b7c32261121f26");
        connection = connectionFactory.connect(account);
    }

}
