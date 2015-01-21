package com.sapsailing.xrr.structureimport.eventimport;

public class RegattaJSON {

    private String id = "";
    private String name = "";
    private String gender = "";
    private String boatClass = "";
    private String xrrEntriesUrl = "";
    private String xrrPreliminaryUrl = "";
    private String xrrFinalUrl = "";
    private String htmlUrl = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getBoatClass() {
        return boatClass;
    }

    public void setBoatClass(String boatClass) {
        this.boatClass = boatClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getXrrEntriesUrl() {
        return xrrEntriesUrl;
    }

    public void setXrrEntriesUrl(String xrrEntriesUrl) {
        this.xrrEntriesUrl = xrrEntriesUrl;
    }

    public String getXrrPreliminaryUrl() {
        return xrrPreliminaryUrl;
    }

    public void setXrrPreliminaryUrl(String xrrPreliminaryUrl) {
        this.xrrPreliminaryUrl = xrrPreliminaryUrl;
    }

    public String getXrrFinalUrl() {
        return xrrFinalUrl;
    }

    public void setXrrFinalUrl(String xrrFinalUrl) {
        this.xrrFinalUrl = xrrFinalUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

}
