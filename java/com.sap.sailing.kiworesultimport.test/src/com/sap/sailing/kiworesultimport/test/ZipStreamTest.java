package com.sap.sailing.kiworesultimport.test;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

public class ZipStreamTest {
    private final static String ZIP_EXAMPLE_FILE = "RESOURCES/Kieler_Woche_2011_Export.zip";
    
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
}
