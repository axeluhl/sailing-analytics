package com.sap.sailing.domain.test.markpassing;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PlotTest {

    @Test
    public void test() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            
            Element meas = doc.createElement("measurement");
            meas.setAttribute("name", "Testing Plotting");
            meas.setAttribute("value", "0.8");
            doc.appendChild(meas);
            
            //String outputPath1 = System.getProperty("user.dir") + File.separator + "report-" + new Date().getTime() + ".xml";
            String outputPath2 = System.getProperty("user.dir") + File.separator + "TEST-plot" + ".xml";
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result =  new StreamResult(new File(outputPath2));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
