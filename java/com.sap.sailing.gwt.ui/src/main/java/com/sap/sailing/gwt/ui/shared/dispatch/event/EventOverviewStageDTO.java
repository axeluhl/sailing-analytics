package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class EventOverviewStageDTO implements DTO {

    private String eventMessage;
    private EventOverviewStageContentDTO stageContent;

    private List<NewsEntryDTO> news = new ArrayList<>();

    @SuppressWarnings("unused")
    private EventOverviewStageDTO() {
    }

    public EventOverviewStageDTO(String eventMessage, EventOverviewStageContentDTO stageContent) {
        super();
        this.eventMessage = eventMessage;
        this.stageContent = stageContent;
    }

    public String getEventMessage() {
        return eventMessage;
    }
    
    public EventOverviewStageContentDTO getStageContent() {
        return stageContent;
    }
    
    public List<NewsEntryDTO> getNews() {
        return news;
    }
    
    public void addNews(NewsEntryDTO newsEntry) {
        news.add(newsEntry);
    }
}
