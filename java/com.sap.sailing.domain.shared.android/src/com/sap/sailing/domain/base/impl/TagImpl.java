package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Tag;
import com.sap.sse.common.TimePoint;

public class TagImpl implements Tag {

    String tag;
    String comment;
    String imageURL;
    String username;
    TimePoint raceTimepoint;

    public TagImpl(String tag, String comment, String imageURL, String username, TimePoint raceTimepoint) {
        this.tag = tag;
        this.comment = comment;
        this.imageURL = imageURL;
        this.username = username;
        this.raceTimepoint = raceTimepoint;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getImageURL() {
        return imageURL;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public TimePoint getRaceTimepoint() {
        return raceTimepoint;
    }
}
