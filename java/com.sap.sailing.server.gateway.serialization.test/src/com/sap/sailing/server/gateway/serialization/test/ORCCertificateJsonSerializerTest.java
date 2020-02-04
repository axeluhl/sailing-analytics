package com.sap.sailing.server.gateway.serialization.test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCCertificatesCollection;
import com.sap.sailing.domain.orc.ORCCertificatesImporter;
import com.sap.sailing.server.gateway.serialization.racelog.impl.ORCCertificateJsonSerializer;

public class ORCCertificateJsonSerializerTest {
    private ORCCertificateJsonSerializer serializer;
    private static final String RESOURCES = "resources/rms/";
    @Before
    public void setUp() {
        serializer = new ORCCertificateJsonSerializer();
    }
    
    @Test
    public void testOrcCertificateNewFormatSerializer() throws IOException, ParseException {
        File fileGER = new File(RESOURCES + "newFormatCertificate.json");
        ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(fileGER));
        Iterator<ORCCertificate> iterator = importer.getCertificates().iterator();
        assertTrue(iterator.hasNext());
        ORCCertificate certificate = iterator.next();
        JSONObject serializedCertificate =serializer.serialize(certificate);
        assertNotNull(serializedCertificate);
    }
    @Test
    public void testOrcCertificateOldFormatSerializer() throws IOException, ParseException {
        File fileGER = new File(RESOURCES + "oldFormatCertificate.json");
        ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(fileGER));
        Iterator<ORCCertificate> iterator = importer.getCertificates().iterator();
        assertTrue(iterator.hasNext());
        ORCCertificate certificate = iterator.next();
        JSONObject serializedCertificate =serializer.serialize(certificate);
        assertNotNull(serializedCertificate);
    }
}
