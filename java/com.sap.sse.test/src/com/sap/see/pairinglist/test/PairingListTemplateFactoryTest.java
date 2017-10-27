package com.sap.see.pairinglist.test;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

public class PairingListTemplateFactoryTest extends PairingListTemplateFactoryImpl {
    
    @Test
    public void testGeneratePairingListTemplate() {
        PairingListTemplate testTemplate = generatePairingList(new PairingFrameProviderTest(15, 3, 18));
        assertNotNull(testTemplate);
        System.out.println(Arrays.deepToString(testTemplate.getPairingListTemplate()));
        System.out.println(testTemplate.getQuality());
    }

}
