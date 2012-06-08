package com.sap.sailing.kiworesultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.Crewmember;
import com.sap.sailing.kiworesultimport.Race;
import com.sap.sailing.kiworesultimport.Races;
import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sailing.kiworesultimport.ResultListParser;
import com.sap.sailing.kiworesultimport.ResultListParserFactory;
import com.sap.sailing.kiworesultimport.Skipper;
import com.sap.sailing.kiworesultimport.Verteilung;

public class ParserTest {
    private static final String RESOURCES = "resources/";

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    @Test
    public void testLoadingSampleXML() throws SAXException, IOException, ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(getSampleInputStream());
        assertNotNull(doc);
        Node resultList = doc.getElementsByTagName("ResultList").item(0);
        assertNotNull(resultList);
    }

    private InputStream getSampleInputStream() throws FileNotFoundException, IOException {
        return getInputStream("2011-06-18_49er_Wettfahrt_2_Extra.xml");
    }
    
    @Test
    public void testObtainingResultList() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        ResultListParser parser = ResultListParserFactory.INSTANCE.createResultListParser();
        ResultList resultList = parser.parse(getSampleInputStream());
        assertNotNull(resultList);
        assertEquals("D:\\Programme\\KWSailing\\eventlogos\\KielerWoche_Ergebnislistenkopf_2011.jpg", resultList.getImagePfad());
        assertEquals(new String(new byte[] { (byte) 160  /* non-breaking space */}), resultList.getLegende());
        final Verteilung verteilung = resultList.getVerteilung();
        assertNotNull(verteilung);
        Iterable<Boat> boats = verteilung.getBoats();
        assertFalse(Util.isEmpty(boats));
        assertEquals(48, Util.size(boats));
        Boat DEN9 = verteilung.getBoatBySailID("DEN 9");
        assertNotNull(DEN9);
        assertEquals(7, (int) DEN9.getPosition());
        Skipper DEN9Skipper = DEN9.getCrew().getSkipper();
        assertEquals("Norregaard, Allan (1981) Kolding", DEN9Skipper.getName());
        assertEquals(new URL("http://www.sailing.org/biog.php?id=DENAN1"), DEN9Skipper.getIsaf());
        Iterable<Crewmember> DEN9Crewmembers = DEN9.getCrew().getCrewmembers();
        assertEquals(1, Util.size(DEN9Crewmembers));
        Crewmember DEN9Crewmember = DEN9Crewmembers.iterator().next();
        assertEquals("Lang, Peter (1989) Kolding Sejlklub", DEN9Crewmember.getName());
        Races DEN9Races = DEN9.getRaces();
        assertNotNull(DEN9Races);
        assertEquals(2, Util.size(DEN9Races.getRaces()));
        Iterator<Race> i = DEN9Races.getRaces().iterator();
        Race r1 = i.next();
        assertEquals(9.00, r1.getPoints(), 0.0000000001);
        assertEquals(1, (int) r1.getNumber());
        Race r2 = i.next();
        assertEquals(1.00, r2.getPoints(), 0.0000000001);
        assertEquals(2, (int) r2.getNumber());
    }
}
