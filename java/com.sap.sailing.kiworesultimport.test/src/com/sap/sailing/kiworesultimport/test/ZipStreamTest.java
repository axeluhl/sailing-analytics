package com.sap.sailing.kiworesultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.kiworesultimport.ParserFactory;
import com.sap.sailing.kiworesultimport.RegattaSummary;
import com.sap.sailing.kiworesultimport.ZipFile;

public class ZipStreamTest {
    private final static String ZIP_EXAMPLE_FILE = "resources/Kieler_Woche_2011_Export.zip";
    
    @Test
    public void testOpenZip() throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(ZIP_EXAMPLE_FILE));
        boolean foundStartberichteDir = false;
        ZipEntry nextEntry = zis.getNextEntry();
        while (nextEntry != null) {
            if (nextEntry.getName().startsWith("Startberichte\\")) {
                foundStartberichteDir = true;
            }
            nextEntry = zis.getNextEntry();
        }
        zis.close();
        assertTrue(foundStartberichteDir);
    }
    
    @Test
    public void testZipParser() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
        ZipFile zipFile = ParserFactory.INSTANCE.createZipFileParser().parse(new FileInputStream(ZIP_EXAMPLE_FILE));
        assertNotNull(zipFile);
        Util.contains(zipFile.getBoatClassNames(), "Laser");
        RegattaSummary laser = zipFile.getRegattaSummary("Laser");
        assertEquals(9, Util.size(laser.getRaces()));
    }
}
