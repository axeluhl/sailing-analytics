package com.sap.see.pairinglist.test;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

public class PairingListTemplateFactoryTest<Flight, Group, Competitor> {
    private PairingListTemplateFactoryImpl<Flight, Group, Competitor> aFactoryImpl;

    @Before
    public void init() {
        aFactoryImpl = new PairingListTemplateFactoryImpl<>();
    }

   
}
