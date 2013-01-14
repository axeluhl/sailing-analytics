package com.sap.sailing.domain.tractracadapter.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP;

public abstract class AbstractClientParamsPHPTest {
    protected ClientParamsPHP clientParams;
    
    public void setUp(String resourceName) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourceName);
        Reader r = new InputStreamReader(is);
        clientParams = new ClientParamsPHP(r);
    }
}
