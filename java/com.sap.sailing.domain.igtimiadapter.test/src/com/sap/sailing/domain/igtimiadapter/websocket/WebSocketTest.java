package com.sap.sailing.domain.igtimiadapter.websocket;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.impl.ClientImpl;
import com.sap.sailing.domain.igtimiadapter.impl.IgtimiConnectionFactoryImpl;

public class WebSocketTest {
    @Test
    public void testWebSocketConnect() throws Exception {
        final Client client = new ClientImpl("7fcdd217e0aa16090edb4ad55b09ec43b2021090e209541fc9b7003c2a2b70c6",
                "aa569cf4909bdc7b0e04b11873f3c4ea20687421e010fcc25b771cca9e6f3f9a", "http://127.0.0.1:8888/igtimi/oauth/v1/authorizationcallback");
        final IgtimiConnectionFactory connectionFactory = new IgtimiConnectionFactoryImpl(client);
        Account account = connectionFactory.registerAccountForWhichClientIsAuthorized("3b6cbd0522423bb1ac274ddb9e7e579c4b3be6667622271086c4fdbf30634ba9");
        WebSocketConnectionManager manager = new WebSocketConnectionManager(connectionFactory, Collections.singleton("GA-EN-AAEJ"), account);
        assertNotNull(manager);
        manager.disconnect();
    }
}
