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
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        connection = connectionFactory.connect(account);
    }

}
