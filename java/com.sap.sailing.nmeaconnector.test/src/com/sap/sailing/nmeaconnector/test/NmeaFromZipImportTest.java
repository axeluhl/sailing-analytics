package com.sap.sailing.nmeaconnector.test;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.nmeaconnector.NmeaFactory;
import com.sap.sse.common.Util;

public class NmeaFromZipImportTest {
    private ZipInputStream zipInputStream;

    @Before
    public void setUp() throws FileNotFoundException {
        NmeaFactory.INSTANCE.getUtil().registerAdditionalParsers();
        zipInputStream = new ZipInputStream(new FileInputStream("resources/LogSS.txt.zip"));
    }
    
    @Test
    public void testReadOneZipEntry() throws IOException, InterruptedException {
        ZipEntry entry;
        while ((entry=zipInputStream.getNextEntry()) != null) {
            if (entry.getName().toLowerCase().endsWith(".txt")) {
                Iterable<Wind> windFixes = NmeaFactory.INSTANCE.readWind(zipInputStream);
                assertTrue(!Util.isEmpty(windFixes));
            }
        }
    }
}
