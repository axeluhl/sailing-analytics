package com.sap.sailing.media.mp4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

public class MP4MediaParser {
    /**
     * The relevant size at the start and end of a file, that most likely will contain all metadata
     */
    public static final int REQUIRED_SIZE_IN_BYTES = 10000000;

    public static boolean determine360(IsoFile isof) throws ParserConfigurationException, SAXException, IOException {
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

    public static long determineDurationInMillis(IsoFile isof) {
        long duration = -1;
        MovieBox mbox = isof.getMovieBox();
        if (mbox != null) {
            MovieHeaderBox mhb = mbox.getMovieHeaderBox();
            if (mhb != null) {
                duration = mhb.getDuration() * 1000 / mhb.getTimescale();
            }
        }
        return duration;
    }

    public static Date determineRecordingStart(IsoFile isof) {
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
