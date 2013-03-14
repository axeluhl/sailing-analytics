package com.sap.sailing.xrr.resultimport.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME_LASER = "laser.xml";
    private static final String SAMPLE_INPUT_NAME_STAR = "star.xml";
    private static final String RESOURCES = "resources/";

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    @Test
    public void testSimpleParsingSomeLaserDocument() throws JAXBException, IOException {
        RegattaResults o = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_LASER)).parse();
        assertNotNull(o);
    }

    @Test
    public void testSimpleParsingSomeStarDocument() throws JAXBException, IOException {
        RegattaResults o = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_STAR)).parse();
        assertNotNull(o);
    }
}
