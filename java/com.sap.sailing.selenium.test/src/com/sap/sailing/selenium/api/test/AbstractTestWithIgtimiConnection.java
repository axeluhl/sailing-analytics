package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SHARED_SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.Authenticator;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class AbstractTestWithIgtimiConnection extends AbstractSeleniumTest {
    protected RiotServer riot;
    protected IgtimiConnection connection;
    
    @Rule public Timeout AbstractTestWithIgtimiConnectionTimeout = Timeout.millis(2 * 60 * 1000);
    
    @Rule public Timeout AbstractTracTracLiveTestTimeout = Timeout.millis(2 * 60 * 1000);
    
    protected ApiContext ctx;

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
        super.setUp();
        ctx = createAdminApiContext(getContextRoot(), SHARED_SERVER_CONTEXT);
        Authenticator authenticator = new Authenticator(getContextRoot());
        String token = authenticator.authForToken(ApiContext.ADMIN_USERNAME, ApiContext.ADMIN_PASSWORD);
        try {
            connection = IgtimiConnectionFactory.create(new URL(getContextRoot()), token).getOrCreateConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
