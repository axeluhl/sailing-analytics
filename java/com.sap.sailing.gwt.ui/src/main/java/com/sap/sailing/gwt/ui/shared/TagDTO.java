package com.sap.sailing.gwt.ui.shared;

import com.sap.sse.common.TimePoint;
import com.sap.sse.gwt.client.media.ImageDTO;

public class TagDTO {
    String tag;
    String comment;
    ImageDTO image;
    String username;
    TimePoint createdAt;
    TimePoint raceTimepoint;
    
    public TagDTO(String tag, String comment, ImageDTO image, String username, TimePoint createdAt, TimePoint raceTimepoint) {
        this.tag = tag;
        this.comment = comment;
        this.image = image;
        this.username = username;
        this.createdAt = createdAt;
        this.raceTimepoint = raceTimepoint;
    }
    
    // Missing comment as it is optional
    public TagDTO(String tag, ImageDTO image, String username, TimePoint createdAt, TimePoint raceTimepoint) {
        this(tag, null, image, username, createdAt, raceTimepoint);
    }
    
    // Missing image as it is optional
    public TagDTO(String tag, String comment, String username, TimePoint createdAt, TimePoint raceTimepoint) {
        this(tag, comment, null, username, createdAt, raceTimepoint);
    }
    
    // Missing image & comment as they are optional
    public TagDTO(String tag, String username, TimePoint createdAt, TimePoint raceTimepoint) {
        this(tag, null, null, username, createdAt, raceTimepoint);
    }
    
    public String getTag() {
        return tag;
    }
    
    public String getComment() {
        return comment;
    }
    
    public ImageDTO getImage() {
        return image;
    }
    
    public String getUsername() {
        return username;
    }
    
    public TimePoint getCreatedAt() {
        return createdAt;
    }
    
    public TimePoint getRaceTimepoint() {
        return raceTimepoint;
    }
}
