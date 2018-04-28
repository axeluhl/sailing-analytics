package com.sap.sailing.media;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.UserBox;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.mp4parser.tools.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MediaFileParser {
    public static void main(String[] args) throws MalformedURLException, ParserConfigurationException, SAXException, IOException {
        new MediaFileParser().checkMetadata(new File(args[0]));
    }
    
    public void checkMetadata(File file) throws IOException, ParserConfigurationException, SAXException {
        boolean spherical = false;
        long durationInMillis = -1;
        Date recordStartedTimer = null;
        try (IsoFile isof = new IsoFile(file)) {
            recordStartedTimer = determineRecordingStart(isof);
            spherical = determine360(isof);
            durationInMillis = determineDurationInMillis(isof);
            System.out.println("Start: "+recordStartedTimer.toString()+", duration: "+durationInMillis+"ms; spherical: "+spherical);
        }
    }

    private boolean determine360(IsoFile isof) throws ParserConfigurationException, SAXException, IOException {
        boolean spherical = false;
        UserBox uuidBox = Path.getPath(isof, "moov[0]/trak[0]/uuid");
        if (uuidBox != null) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(uuidBox.getData()));

            NodeList childs = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child.getNodeName().toLowerCase().contains(":spherical")) {
                    spherical = true;
                }
            }
        }
        return spherical;
    }

    private long determineDurationInMillis(IsoFile isof) {
        long duration = -1;
        MovieBox mbox = isof.getMovieBox();
        if (mbox != null) {
            MovieHeaderBox mhb = mbox.getMovieHeaderBox();
            if (mhb != null) {
                duration = mhb.getDuration()*1000 / mhb.getTimescale();
            }
        }
        return duration;
    }
    
    private Date determineRecordingStart(IsoFile isof) {
        Date creationTime = null;
        MovieBox mbox = isof.getMovieBox();
        if (mbox != null) {
            MovieHeaderBox mhb = mbox.getMovieHeaderBox();
            if (mhb != null) {
                creationTime = mhb.getCreationTime();
            }
        }
        return creationTime;
    }
}
