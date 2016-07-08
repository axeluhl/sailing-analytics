package com.sap.sailing.android.tracking.app.valueobjects;

import com.sap.sailing.domain.base.Mark;

public class MarkUrlData extends UrlData {

    private String markId;
    private String markUrl;
    private Mark mark;

    public MarkUrlData(String server, int port) {
        super(server, port);
    }

    public String getMarkId() {
        return markId;
    }

    public void setMarkId(String markId) {
        this.markId = markId;
    }

    public String getMarkUrl() {
        return markUrl;
    }

    public void setMarkUrl(String markUrl) {
        this.markUrl = markUrl;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(Mark mark) {
        this.mark = mark;
    }

}
