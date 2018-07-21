package com.sap.sailing.manage2sail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sap.sailing.domain.common.CompetitorGenderType;

public class RegattaResultDescriptor {
    private String id;
    private String isafId;
    private String externalId;
    private String name;
    private String className;
    private CompetitorGenderType competitorGenderType;
    private URL pdfUrl;
    private URL xrrPreliminaryUrl;
    private URL xrrFinalUrl;
    private URL xrrEntriesUrl;
    private URL htmlUrl;
    private Date publishedAt;
    private Boolean isFinal;
    private List<RaceResultDescriptor> raceResults;

    public RegattaResultDescriptor() {
        raceResults = new ArrayList<RaceResultDescriptor>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsafId() {
        return isafId;
    }

    public void setIsafId(String isafId) {
        this.isafId = isafId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public URL getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(URL pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public URL getXrrPreliminaryUrl() {
        return xrrPreliminaryUrl;
    }

    public void setXrrPreliminaryUrl(URL xrrPreliminaryUrl) {
        this.xrrPreliminaryUrl = xrrPreliminaryUrl;
    }

    public URL getXrrFinalUrl() {
        return xrrFinalUrl;
    }

    public void setXrrFinalUrl(URL xrrFinalUrl) {
        this.xrrFinalUrl = xrrFinalUrl;
    }

    public URL getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(URL htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }

    public CompetitorGenderType getCompetitorGenderType() {
        return competitorGenderType;
    }

    public void setCompetitorGenderType(CompetitorGenderType competitorGenderType) {
        this.competitorGenderType = competitorGenderType;
    }

    public List<RaceResultDescriptor> getRaceResults() {
        return raceResults;
    }

    public void setRaceResults(List<RaceResultDescriptor> raceResults) {
        this.raceResults = raceResults;
    }

    public URL getXrrEntriesUrl() {
        return xrrEntriesUrl;
    }

    public void setXrrEntriesUrl(URL xrrEntriesUrl) {
        this.xrrEntriesUrl = xrrEntriesUrl;
    }
}
