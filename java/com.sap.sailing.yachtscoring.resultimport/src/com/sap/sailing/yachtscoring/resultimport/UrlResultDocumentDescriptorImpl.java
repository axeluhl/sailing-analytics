package com.sap.sailing.yachtscoring.resultimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.sap.sailing.domain.common.CompetitorGenderType;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sse.common.TimePoint;

public class UrlResultDocumentDescriptorImpl implements ResultDocumentDescriptor {
    private URL documentURL;  
    private String documentName;
    private TimePoint lastModified;
    private String eventName;
    private String regattaName;
    private String boatClass;
    private CompetitorGenderType competitorGenderType;

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
        try {
            URLConnection eventResultConn = documentURL.openConnection();
            InputStream inputStream = (InputStream) eventResultConn.getContent();
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDocumentName() {
        return documentName;
    }

    public CompetitorGenderType getCompetitorGenderType() {
        return competitorGenderType;
    }
}
