package com.sap.sailing.media.mp4;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.mp4parser.AbstractBoxParser;
import org.mp4parser.IsoFile;
import org.mp4parser.PropertyBoxParserImpl;
import org.xml.sax.SAXException;

public class MediaFileParser {
    public static void main(String[] args)
            throws MalformedURLException, ParserConfigurationException, SAXException, IOException {
        new MediaFileParser().checkMetadata(new File(args[0]));
    }

    public void checkMetadata(File file) throws IOException, ParserConfigurationException, SAXException {
        boolean spherical = false;
        long durationInMillis = -1;
        Date recordStartedTimer = null;
        AbstractBoxParser boxParserImpl = new PropertyBoxParserImpl();
        boxParserImpl.skippingBoxes(new String[] { "mdat" });
        try (IsoFile isof = new IsoFile(Files.newByteChannel(file.toPath()), boxParserImpl)) {
            recordStartedTimer = MP4MediaParser.determineRecordingStart(isof);
            spherical = MP4MediaParser.determine360(isof);
            durationInMillis = MP4MediaParser.determineDurationInMillis(isof);
            System.out.println("Start: " + recordStartedTimer.toString() + ", duration: " + durationInMillis
                    + "ms; spherical: " + spherical);
        }
    }
}
