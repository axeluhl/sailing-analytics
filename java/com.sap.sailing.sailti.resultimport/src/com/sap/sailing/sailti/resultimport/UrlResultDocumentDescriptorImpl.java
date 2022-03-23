package com.sap.sailing.sailti.resultimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.CompetitorGenderType;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.HttpUrlConnectionHelper;

public class UrlResultDocumentDescriptorImpl implements ResultDocumentDescriptor {
    private static final Logger logger = Logger.getLogger(UrlResultDocumentDescriptorImpl.class.getName());

    private final URL documentURL;  
    private final String documentName;
    private final TimePoint lastModified;
    private final String eventName;
    private final String regattaName;
    private final String boatClass;
    private final CompetitorGenderType competitorGenderType;

    public UrlResultDocumentDescriptorImpl(URL documentURL, String documentName, TimePoint lastModified) {
        this(documentURL, documentName, lastModified, null, null, null, null);
    }

    public UrlResultDocumentDescriptorImpl(URL documentURL, String documentName, TimePoint lastModified,
            String eventName) {
        this(documentURL, documentName, lastModified, eventName, null, null, null);
    }

    public UrlResultDocumentDescriptorImpl(URL documentURL, String documentName, TimePoint lastModified,
            String eventName, String regattaName) {
        this(documentURL, documentName, lastModified, eventName, regattaName, null, null);
    }

    public UrlResultDocumentDescriptorImpl(URL documentURL, String documentName, TimePoint lastModified,
            String eventName, String regattaName, String boatClass) {
        this(documentURL, documentName, lastModified, eventName, regattaName, boatClass, null);
    }

    public UrlResultDocumentDescriptorImpl(URL documentURL, String documentName, TimePoint lastModified,
            String eventName, String regattaName, String boatClass, CompetitorGenderType competitorGenderType) {
        this.lastModified = lastModified;
        this.documentName = documentName;
        this.documentURL = documentURL;
        this.eventName = eventName;
        this.regattaName = regattaName;
        this.boatClass = boatClass;
        this.competitorGenderType = competitorGenderType;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public String getRegattaName() {
        return regattaName;
    }

    @Override
    public String getBoatClass() {
        return boatClass;
    }

    @Override
    public TimePoint getLastModified() {
        return lastModified;
    }
    
    @Override
    public InputStream getInputStream() {
        InputStream result;
        try {
            final URLConnection eventResultConn = HttpUrlConnectionHelper.redirectConnection(documentURL);
            final InputStream inputStream = (InputStream) eventResultConn.getContent();
            result = inputStream;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error trying to read Sailti document from "+documentURL);
            result = null;
        }
        return result;
    }

    @Override
    public String getDocumentName() {
        return documentName;
    }

    public CompetitorGenderType getCompetitorGenderType() {
        return competitorGenderType;
    }
}
