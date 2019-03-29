package com.sap.sailing.selenium.api.core;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.junit.runner.RunWith;

import com.sap.sailing.selenium.core.Managed;
import com.sap.sailing.selenium.core.SeleniumRunner;
import com.sap.sailing.selenium.core.TestEnvironment;

@RunWith(SeleniumRunner.class)
public abstract class AbstractApiTest {

    private static final Logger logger = Logger.getLogger(AbstractApiTest.class.getName());

    protected static final String SERVER_CONTEXT = "sailingserver"; //$NON-NLS-1$
    protected static final String SECURITY_CONTEXT = "security"; //$NON-NLS-1$
    private static final String CLEAR_STATE_URL = SERVER_CONTEXT + "/test-support/clearState"; //$NON-NLS-1$
    private static final int CLEAR_STATE_SUCCESFUL_STATUS_CODE = 204;

    @Managed
    protected TestEnvironment environment;

    protected void clearState(String contextRoot) {
        logger.info("clearing server state");
        try {
            URL url = new URL(contextRoot + CLEAR_STATE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            if (connection.getResponseCode() != CLEAR_STATE_SUCCESFUL_STATUS_CODE) {
                throw new RuntimeException(connection.getResponseMessage());
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected String getContextRoot() {
        return this.environment.getContextRoot();
    }

    public enum AuthType {
        COOKIE, BEARER
    }
}
