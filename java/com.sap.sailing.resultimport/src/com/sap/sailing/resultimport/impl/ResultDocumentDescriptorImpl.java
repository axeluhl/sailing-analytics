package com.sap.sailing.resultimport.impl;

import java.io.InputStream;

import com.sap.sailing.domain.common.CompetitorGenderType;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sse.common.TimePoint;

public class ResultDocumentDescriptorImpl implements ResultDocumentDescriptor {
    private InputStream inputStream;
    private String documentName;
    private TimePoint lastModified;
    private String eventName;
    private String regattaName;
    private String boatClass;
    private CompetitorGenderType competitorGenderType;

    public ResultDocumentDescriptorImpl(InputStream inputStream, String documentName, TimePoint lastModified) {
        this(inputStream, documentName, lastModified, null, null, null, null);
    }

    public ResultDocumentDescriptorImpl(InputStream inputStream, String documentName, TimePoint lastModified,
            String eventName) {
        this(inputStream, documentName, lastModified, eventName, null, null, null);
    }

    public ResultDocumentDescriptorImpl(InputStream inputStream, String documentName, TimePoint lastModified,
            String eventName, String regattaName) {
        this(inputStream, documentName, lastModified, eventName, regattaName, null, null);
    }

    public ResultDocumentDescriptorImpl(InputStream inputStream, String documentName, TimePoint lastModified,
            String eventName, String regattaName, String boatClass) {
        this(inputStream, documentName, lastModified, eventName, regattaName, boatClass, null);
    }

    public ResultDocumentDescriptorImpl(InputStream inputStream, String documentName, TimePoint lastModified,
            String eventName, String regattaName, String boatClass, CompetitorGenderType competitorGenderType) {
        this.inputStream = inputStream;
        this.lastModified = lastModified;
        this.documentName = documentName;
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
        return inputStream;
    }

    @Override
    public String getDocumentName() {
        return documentName;
    }

    public CompetitorGenderType getCompetitorGenderType() {
        return competitorGenderType;
    }
}
